package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.UserDTO
import dev.vku.livesnap.domain.model.User

fun UserDTO.toDomain() = User(
    id = this.id,
    username = this.username,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    avatar = this.avatar
)

fun User.toDTO() = UserDTO(
    id = this.id,
    username = this.username,
    email = this.email,
    firstName = this.firstName,
    lastName = this.lastName,
    avatar = this.avatar
)
