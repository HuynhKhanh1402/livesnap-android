package dev.vku.livesnap.data.remote.dto

data class PhotoDTO(
    val photoId: String,
    val senderId: String,
    val receiverId: String,
    val url: String,
    val timestamp: Long
)