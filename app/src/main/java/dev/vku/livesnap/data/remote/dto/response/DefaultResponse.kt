package dev.vku.livesnap.data.remote.dto.response

data class DefaultResponse(
    val code: Int,
    val isValid: Boolean? = null,
    val message: String
)
