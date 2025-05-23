package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.data.remote.dto.FriendDTO

data class FriendListResponse(
    val code: Int,
    val message: String,
    val data: List<FriendDTO>
)
