package dev.vku.snaplive.domain.model

import java.util.Date

data class Photo(
    val photoId: String,
    val senderId: String,
    val receiverId: String,
    val url: String,
    val timestamp: Date
)