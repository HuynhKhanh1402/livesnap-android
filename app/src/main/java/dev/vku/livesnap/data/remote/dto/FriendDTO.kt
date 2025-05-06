package dev.vku.livesnap.data.remote.dto

data class FriendDTO(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String?,
    val friendSince: String
)