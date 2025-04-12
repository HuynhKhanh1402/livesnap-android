package dev.vku.livesnap.data.remote.dto.response

data class CheckUsernameExistResponse(
    val code: Int,
    val message: String,
    val exist: Boolean
)