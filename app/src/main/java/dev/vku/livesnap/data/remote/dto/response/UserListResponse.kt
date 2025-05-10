package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.data.remote.dto.UserDTO

data class UserListResponse(
    val code: Int,
    val message: String,
    val data: List<UserDTO>
)
