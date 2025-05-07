package dev.vku.livesnap.data.remote.dto

data class ReactionDTO(
    val id: String,
    val emoji: String,
    val user: UserDTO
)
