package dev.vku.livesnap.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?
)