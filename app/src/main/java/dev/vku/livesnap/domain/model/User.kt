package dev.vku.livesnap.domain.model

data class User(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String?
)