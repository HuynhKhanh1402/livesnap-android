package dev.vku.livesnap.domain.model

data class Reaction(
    val id: String,
    val emoji: String,
    val user: User
)
