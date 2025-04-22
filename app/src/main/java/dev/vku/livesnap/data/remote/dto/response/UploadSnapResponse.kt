package dev.vku.livesnap.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import dev.vku.livesnap.data.remote.dto.SnapDTO

data class UploadSnapResponse(
    val code: Int,
    val message: String,
    @SerializedName("data")
    val snap: SnapDTO
)