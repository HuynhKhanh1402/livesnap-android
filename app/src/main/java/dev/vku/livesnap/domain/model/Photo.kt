package dev.vku.snaplive.domain.model

import java.util.Date

data class Photo(
    val photoId: String,
    val userId: String,
    val caption: String,
    val image: String,
    val createdAt: Date
)