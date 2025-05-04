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
    val snapRepository: SnapRepository
) : ViewModel() {
    private var _loadSnapResult = MutableStateFlow<LoadSnapResult>(LoadSnapResult.Idle)
    var loadSnapResult: StateFlow<LoadSnapResult> = _loadSnapResult

    private var _reactSnapResult = MutableStateFlow<LoadingResult<String>>(LoadingResult.Idle)
    var reactSnapResult: StateFlow<LoadingResult<String>> = _reactSnapResult

    var isFirstLoad = true

    var snaps by mutableStateOf<List<Snap>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var currentPage = 1
    private val pageSize = 2
    var hasNextPage = true

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