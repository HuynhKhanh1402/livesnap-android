package dev.vku.livesnap.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.FirebaseMessageRepository
import dev.vku.livesnap.domain.model.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: FirebaseMessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentChatId: String? = null
    private var currentLimit = 20L

    fun loadMessages(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        currentLimit = 20L

        viewModelScope.launch {
            messageRepository.getMessages(chatId, currentLimit)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { messages ->
                    _messages.value = messages
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
} 