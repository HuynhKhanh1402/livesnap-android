package dev.vku.livesnap.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import dev.vku.livesnap.data.remote.dto.UserDTO

data class UserDetailResponse(
    val code: Int,
    val message: String,
    val data: Data
) {
    data class Data(
        @SerializedName("info")
        val user: UserDTO
    )
}
