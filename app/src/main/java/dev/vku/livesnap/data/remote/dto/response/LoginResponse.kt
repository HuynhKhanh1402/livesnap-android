package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.data.remote.dto.UserDTO

data class LoginResponse(
    val code: Int,
    val message: String,
    val data: Data
)

data class Data(
    val token: String,
    val user: UserDTO
)
