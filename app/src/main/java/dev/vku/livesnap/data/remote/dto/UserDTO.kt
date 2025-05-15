package dev.vku.livesnap.data.remote.dto

data class UserDTO(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String?,
    val isGold: Boolean
)