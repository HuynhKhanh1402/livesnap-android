package dev.vku.livesnap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SnapDTO(
    @SerializedName("_id")
    val id: String,
    val userId: String,
    val caption: String,
    val image: String,
    val createdAt: String
)