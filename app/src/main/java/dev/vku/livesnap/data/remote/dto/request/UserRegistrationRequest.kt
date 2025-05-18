package dev.vku.livesnap.data.remote.dto.request

data class UserRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val username: String,
    val otp: String
)