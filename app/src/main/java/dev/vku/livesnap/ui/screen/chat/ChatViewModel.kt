package dev.vku.livesnap.ui.screen.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.repository.FirebaseMessageRepository
import dev.vku.livesnap.data.repository.SnapRepository
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.model.Message
import dev.vku.livesnap.domain.model.Snap
import dev.vku.livesnap.domain.model.User
import dev.vku.livesnap.domain.mapper.toDomain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: FirebaseMessageRepository,
    private val tokenManager: TokenManager,
    private val snapRepository: SnapRepository,
    private val userRepository: UsersRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser: StateFlow<User?> = _otherUser.asStateFlow()

    private var currentChatId: String? = null
    private var currentLimit = 20L

    private val _snapMap = MutableStateFlow<Map<String, Snap>>(emptyMap())
    val snapMap: StateFlow<Map<String, Snap>> = _snapMap.asStateFlow()

    fun loadMessages(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        currentLimit = 20L

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load chat details to get other user's ID
                val chat = messageRepository.getChat(chatId)
                val currentUserId = getCurrentUserId()
                val otherUserId = chat.participants.firstOrNull { it != currentUserId }
                
                // Load other user's details
                otherUserId?.let { userId ->
                    try {
                        val response = userRepository.getUserById(userId)
                        if (response.isSuccessful) {
                            _otherUser.value = response.body()?.data?.user?.toDomain()
                        }
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error loading user details: ${e.message}", e)
                    }
                }

                // Load messages
                messageRepository.getMessages(chatId, currentLimit)
                    .catch { e ->
                        _error.value = e.message
                        Log.e("ChatViewModel", "Error loading messages: ${e.message}", e)
                    }
                    .collect { messages ->
                        _messages.value = messages
                    }
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ChatViewModel", "Error loading chat: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreMessages() {
        val chatId = currentChatId ?: return
        currentLimit += 20

        viewModelScope.launch {
            _isLoading.value = true
            try {
                messageRepository.getMessages(chatId, currentLimit)
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { messages ->
                        _messages.value = messages
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        val chatId = currentChatId ?: return
        
        viewModelScope.launch {
            messageRepository.sendMessage(chatId, content)
                .onSuccess { message ->
                    // Message will be added to the list through the Flow
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(messageId)
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getCurrentUserId(): String {
        val token = runBlocking {
            tokenManager.getToken() ?: return@runBlocking ""
        }
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return ""

            val payload = parts[1]
            val decodedPayload = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val jsonString = String(decodedPayload)
            val jsonObject = org.json.JSONObject(jsonString)
            jsonObject.getString("userId")
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun fetchSnapIfNeeded(snapId: String?): Snap? {
        if (snapId == null) return null
        val current = _snapMap.value[snapId]
        if (current != null) return current
        return try {
            val response = snapRepository.fetchSnap(snapId)
            if (response.isSuccessful && response.body()?.data != null) {
                val snap = response.body()!!.data.toDomain()
                _snapMap.value = _snapMap.value + (snapId to snap)
                snap
            } else null
        } catch (e: Exception) {
            null
        }
    }
} 