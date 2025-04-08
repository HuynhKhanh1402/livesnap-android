package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.UserDTO
import dev.vku.livesnap.domain.model.User

fun UserDTO.toDomain() = User(
    id = this.id,
    name = this.name,
    email = this.email,
    avatarUrl = this.avatarUrl
)

fun User.toDTO() = UserDTO(
    id = this.id,
    name = this.name,
    email = this.email,
    avatarUrl = this.avatarUrl
)
