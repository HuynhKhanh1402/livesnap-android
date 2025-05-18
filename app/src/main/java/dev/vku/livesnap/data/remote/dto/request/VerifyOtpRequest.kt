package dev.vku.livesnap.data.remote.dto.request

data class VerifyOtpRequest(
    val email: String,
    val otp: String
) 