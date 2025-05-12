package dev.vku.livesnap.data.remote.dto.request

data class SendNotificationRequest(
    val userId: String,
    val title: String,
    val body: String,
    val type: String
) 