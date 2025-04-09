package dev.vku.livesnap.data.remote.dto

data class UserRegistrationDTO(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val username: String
)