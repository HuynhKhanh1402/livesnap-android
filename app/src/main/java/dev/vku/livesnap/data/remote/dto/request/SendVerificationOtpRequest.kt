package dev.vku.livesnap.data.remote.dto.request

data class SendVerificationOtpRequest(
    val email: String,
    val username: String
) 