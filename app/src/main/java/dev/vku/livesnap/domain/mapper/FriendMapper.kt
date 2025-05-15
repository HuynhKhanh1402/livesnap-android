package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.core.util.DateUtils.convertToDate
import dev.vku.livesnap.core.util.DateUtils.convertToString
import dev.vku.livesnap.data.remote.dto.FriendDTO
import dev.vku.livesnap.domain.model.Friend

fun FriendDTO.toDomain() = Friend(
    id = this.id,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    avatar = this.avatar,
    isGold = this.isGold,
    friendSince = this.friendSince.convertToDate()
)

fun Friend.toDTO() = FriendDTO(
    id = this.id,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    avatar = this.avatar,
    isGold = this.isGold,
    friendSince = this.friendSince.convertToString()
)

fun List<FriendDTO>.toDomain(): List<Friend> = this.map { it.toDomain() }

fun List<Friend>.toDTO(): List<FriendDTO> = this.map { it.toDTO() }