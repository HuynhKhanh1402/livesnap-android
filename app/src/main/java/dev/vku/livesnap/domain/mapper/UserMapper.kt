package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.UserDTO
import dev.vku.livesnap.domain.model.User

fun UserDTO.toDomain() = User(
    id = this.id,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    avatar = this.avatar,
    isGold = this.isGold,
    isVisible = this.isVisible
)

fun User.toDTO() = UserDTO(
    id = this.id,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    avatar = this.avatar,
    isGold = this.isGold,
    isVisible = this.isVisible
)

fun List<UserDTO>.toDomain(): List<User> = this.map { it.toDomain() }

fun List<User>.toDTO(): List<UserDTO> = this.map { it.toDTO() }