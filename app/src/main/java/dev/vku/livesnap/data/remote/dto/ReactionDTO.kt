package dev.vku.livesnap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReactionDTO(
    @SerializedName("_id")
    val id: String,
    val emoji: String,
    val user: UserDTO
)
