package dev.vku.livesnap.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.repository.FirebaseMessageRepository
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.model.Chat
import dev.vku.livesnap.domain.model.User
import dev.vku.livesnap.domain.mapper.toDomain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class ChatWithUser(
    val chat: Chat,
    val otherUser: User? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val messageRepository: FirebaseMessageRepository,
    private val userRepository: UsersRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatWithUser>>(emptyList())
    val chats: StateFlow<List<ChatWithUser>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            messageRepository.getChats()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { chats ->
                    val currentUserId = getCurrentUserId()
                    val chatsWithUsers = chats.map { chat ->
                        val otherUserId = chat.participants.firstOrNull { it != currentUserId }
                        val otherUser = otherUserId?.let { fetchUserDetails(it) }
                        ChatWithUser(chat, otherUser)
                    }
                    _chats.value = chatsWithUsers
                    _isLoading.value = false
                }
        }
    }

    private suspend fun fetchUserDetails(userId: String): User? {
        return try {
            val response = userRepository.getUserById(userId)
            if (response.isSuccessful) {
                response.body()?.data?.user?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentUserId(): String {
        val token = runBlocking {
            tokenManager.getToken() ?: return@runBlocking ""
        }
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return ""
            val payload = parts[1]
            val decodedPayload = android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
            val jsonString = String(decodedPayload)
            val jsonObject = org.json.JSONObject(jsonString)
            jsonObject.getString("sub")
        } catch (e: Exception) {
            ""
        }
    }

    fun clearError() {
        _error.value = null
    }
} 