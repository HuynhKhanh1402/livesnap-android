package dev.vku.livesnap.data.remote.dto.response

data class CheckEmailExistResponse(
    val code: Int,
    val message: String,
    val exist: Boolean
)