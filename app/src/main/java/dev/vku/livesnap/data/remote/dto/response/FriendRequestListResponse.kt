package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.data.remote.dto.FriendRequestDTO

data class FriendRequestListResponse(
    val code: Int,
    val message: String,
    val data: Data
) {
    data class Data(
        val requests: List<FriendRequestDTO>
    )
}
