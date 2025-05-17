package dev.vku.livesnap.domain.model

import java.util.Date

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val snapId: String? = null,
    val timestamp: Date? = null,
    val isRead: Boolean = false
)