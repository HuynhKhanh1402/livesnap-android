package dev.vku.livesnap.domain.model

import java.util.Date

data class Snap(
    val id: String,
    val caption: String,
    val image: String,
    val user: User,
    val isOwner: Boolean,
    val createdAt: Date
)