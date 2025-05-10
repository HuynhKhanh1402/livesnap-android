package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.FriendRequestDTO
import dev.vku.livesnap.domain.model.FriendRequest

fun FriendRequestDTO.toDomain(): FriendRequest {
    return FriendRequest(
        id = this.id,
        user = this.user.toDomain()
    )
}

fun FriendRequest.toDTO(): FriendRequestDTO {
    return FriendRequestDTO(
        id = this.id,
        user = this.user.toDTO()
    )
}

fun List<FriendRequestDTO>.toDomain(): List<FriendRequest> {
    return this.map { it.toDomain() }
}

fun List<FriendRequest>.toDTO(): List<FriendRequestDTO> {
    return this.map { it.toDTO() }
}