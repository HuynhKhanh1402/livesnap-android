package dev.vku.livesnap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDTO(
    @SerializedName("_id")
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String?
)