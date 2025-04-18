package dev.vku.livesnap.domain.model

import java.util.Date

data class Snap(
    val id: String,
    val userId: String,
    val caption: String,
    val image: String,
    val createdAt: Date
)