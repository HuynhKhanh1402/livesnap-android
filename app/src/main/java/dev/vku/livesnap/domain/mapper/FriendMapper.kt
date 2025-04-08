package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.FriendDTO
import dev.vku.livesnap.domain.model.Friend

fun FriendDTO.toDomain() = Friend(
    id = this.id,
    name = this.name,
    avatarUrl = this.avatarUrl
)

fun Friend.toDTO() = FriendDTO(
    id = this.id,
    name = this.name,
    avatarUrl = this.avatarUrl
)