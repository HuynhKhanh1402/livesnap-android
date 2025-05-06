package dev.vku.livesnap.domain.model

import java.util.Date

data class Friend(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String?,
    val friendSince: Date
)