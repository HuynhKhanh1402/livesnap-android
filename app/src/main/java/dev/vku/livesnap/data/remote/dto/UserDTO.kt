package dev.vku.livesnap.data.remote.dto

data class UserDTO(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?
)