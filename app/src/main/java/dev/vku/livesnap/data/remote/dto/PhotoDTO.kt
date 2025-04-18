package dev.vku.livesnap.data.remote.dto

data class PhotoDTO(
    val photoId: String,
    val userId: String,
    val caption: String,
    val image: String,
    val createdAt: Long
)