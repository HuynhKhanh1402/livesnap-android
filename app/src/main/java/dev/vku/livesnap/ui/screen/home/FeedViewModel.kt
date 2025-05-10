package dev.vku.livesnap.ui.screen.home

import android.icu.text.BreakIterator
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.SnapRepository
import dev.vku.livesnap.data.repository.FirebaseMessageRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.mapper.toSnapList
import dev.vku.livesnap.domain.model.Snap
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoadSnapResult {
    data object Success : LoadSnapResult()
    data class Error(val message: String) : LoadSnapResult()
    data object Idle : LoadSnapResult()
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    val snapRepository: SnapRepository,
    private val firebaseMessageRepository: FirebaseMessageRepository
) : ViewModel() {
    private var _loadSnapResult = MutableStateFlow<LoadSnapResult>(LoadSnapResult.Idle)
    var loadSnapResult: StateFlow<LoadSnapResult> = _loadSnapResult

    private var _reactSnapResult = MutableStateFlow<LoadingResult<String>>(LoadingResult.Idle)
    var reactSnapResult: StateFlow<LoadingResult<String>> = _reactSnapResult

    private var _sendMessageResult = MutableStateFlow<LoadingResult<String>>(LoadingResult.Idle)
    var sendMessageResult: StateFlow<LoadingResult<String>> = _sendMessageResult

    var isFirstLoad = true

    var snaps by mutableStateOf<List<Snap>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var currentPage = 1
    private val pageSize = 2
    var hasNextPage = true

    var currentSnap by mutableStateOf<Snap?>(null)
        private set

    private var _isFetchingCurrentSnap = MutableStateFlow<Boolean>(false)
    var isFetchingCurrentSnap: StateFlow<Boolean> = _isFetchingCurrentSnap

    fun loadSnaps() {
        if (isLoading || !hasNextPage) return

        viewModelScope.launch {
            isLoading = true
            _loadSnapResult.value = LoadSnapResult.Idle

            try {
                val response = snapRepository.getSnaps(currentPage, pageSize)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.i("FeedViewModel", "Loaded snaps: ${it.data.snaps}")
                        snaps += it.data.snaps.toSnapList()
                        hasNextPage = currentPage < it.data.pagination.totalPages
                        currentPage++

                        _loadSnapResult.value = LoadSnapResult.Success
                    }
                } else {
                    _loadSnapResult.value = LoadSnapResult.Error(response.message() ?: "Unknown error")
                    Log.e("FeedViewModel", "Failed to load snaps: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FeedViewMode", "Exception occurred: ${e.message}", e)
                _loadSnapResult.value = LoadSnapResult.Error("An error occurred: ${e.message}")
            } finally {
                isLoading = false
                isFirstLoad = false
            }
        }
    }

    fun resetLoadSnapResult() {
        _loadSnapResult.value = LoadSnapResult.Idle
    }

    fun deleteSnap(snapId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true

            try {
                val response = snapRepository.deleteSnap(snapId)
                if (response.isSuccessful) {
                    snaps = snaps.filter { it.id != snapId }
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Exception occurred: ${e.message}", e)
                onError("An error occurred: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun reactSnap(snap: Snap, emoji: String) {
        viewModelScope.launch {
            isLoading = true

            try {
                require(isSingleEmojiICU(emoji)) {
                    "Invalid emoji: $emoji"
                }

                val response = snapRepository.reactSnap(snap.id, emoji)

                if (response.isSuccessful && response.body()?.code == 200) {
                    _reactSnapResult.value = LoadingResult.Success(emoji)
                } else {
                    _reactSnapResult.value = LoadingResult.Error(response.message() ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Exception occurred: ${e.message}", e)
                _reactSnapResult.value = LoadingResult.Error("An error occurred: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun resetReactSnapResult() {
        _reactSnapResult.value = LoadingResult.Idle
    }

    fun updateCurrentSnap(newSnap: Snap) {
        if (newSnap == currentSnap) {
            return
        }

        currentSnap = newSnap
        fetchingCurrentSnap(newSnap.id)
    }

    fun fetchingCurrentSnap(snapId: String) {
        viewModelScope.launch {
            _isFetchingCurrentSnap.value = true

            try {
                val response = snapRepository.fetchSnap(snapId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val snap = response.body()!!.data.toDomain()
                    if (snap.id == currentSnap?.id) {
                        currentSnap = snap
                    }

                    snaps = snaps.map { if (it.id == snap.id) snap else it }

                    Log.d("FeedViewModel", "Updated snap: $snap")
                } else {
                    Log.e("FeedViewModel", "Failed to fetch snap detail: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Exception occurred while fetching snap detail: ${e.message}", e)
            } finally {
                _isFetchingCurrentSnap.value = false
            }
        }
    }

    fun sendMessage(snap: Snap, message: String) {
        viewModelScope.launch {
            isLoading = true
            _sendMessageResult.value = LoadingResult.Loading

            try {
                // Validate message
                if (message.isBlank()) {
                    _sendMessageResult.value = LoadingResult.Error("Message cannot be empty")
                    return@launch
                }

                if (message.length > 500) {
                    _sendMessageResult.value = LoadingResult.Error("Message is too long (max 500 characters)")
                    return@launch
                }

                // Get or create chat
                val chatResult = firebaseMessageRepository.getOrCreateChat(snap.user.id)
                if (chatResult.isFailure) {
                    _sendMessageResult.value = LoadingResult.Error("Failed to get or create chat: ${chatResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val chat = chatResult.getOrNull()!!
                
                // Send message kèm snapId
                val messageResult = firebaseMessageRepository.sendMessage(
                    chat.id,
                    message,
                    snapId = snap.id
                )
                if (messageResult.isSuccess) {
                    _sendMessageResult.value = LoadingResult.Success("Message sent successfully")
                } else {
                    _sendMessageResult.value = LoadingResult.Error("Failed to send message: ${messageResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Exception occurred while sending message: ${e.message}", e)
                _sendMessageResult.value = LoadingResult.Error("An error occurred: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun resetSendMessageResult() {
        _sendMessageResult.value = LoadingResult.Idle
    }

    private fun isSingleEmojiICU(text: String): Boolean {
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text)

        var count = 0
        var start = iterator.first()
        var end = iterator.next()

        while (end != BreakIterator.DONE) {
            count++
            if (count > 1) return false
            start = end
            end = iterator.next()
        }

        return count == 1
    }
}