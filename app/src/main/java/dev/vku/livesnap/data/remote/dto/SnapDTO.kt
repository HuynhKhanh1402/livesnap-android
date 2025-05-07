package dev.vku.livesnap.data.remote.dto

data class SnapDTO(
    val id: String,
    val caption: String,
    val image: String,
    val user: UserDTO,
    val isOwner: Boolean,
    val reactions: List<ReactionDTO>,
    val createdAt: String
)