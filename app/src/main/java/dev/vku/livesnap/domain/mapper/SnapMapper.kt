package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.core.util.DateUtils.convertToDate
import dev.vku.livesnap.core.util.DateUtils.convertToString
import dev.vku.livesnap.data.remote.dto.SnapDTO
import dev.vku.livesnap.domain.model.Snap

fun SnapDTO.toDomain(): Snap {
    return Snap(
        id = this.id,
        caption = this.caption,
        image = this.image,
        user = this.user.toDomain(),
        isOwner = this.isOwner,
        reactions = this.reactions.toDomain(),
        createdAt = this.createdAt.convertToDate()
    )
}

fun Snap.toSnapDTO(): SnapDTO {
    return SnapDTO(
        id = this.id,
        caption = this.caption,
        image = this.image,
        user = this.user.toDTO(),
        isOwner = this.isOwner,
        reactions = this.reactions.toDTO(),
        createdAt = this.createdAt.convertToString()
    )
}


fun List<SnapDTO>.toSnapList(): List<Snap> {
    return this.map { it.toDomain() }
}

fun List<Snap>.toSnapDTOList(): List<SnapDTO> {
    return this.map { it.toSnapDTO() }
}