package dev.vku.livesnap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SnapDTO(
    @SerializedName("_id")
    val id: String,
    val caption: String,
    val image: String,
    val user: UserDTO,
    val isOwner: Boolean,
    val reactions: List<ReactionDTO>,
    val createdAt: String
)