package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.ReactionDTO
import dev.vku.livesnap.domain.model.Reaction

fun ReactionDTO.toDomain(): Reaction {
    return Reaction(
        id = this.id,
        emoji = this.emoji,
        user = this.user.toDomain()
    )
}

fun Reaction.toDTO(): ReactionDTO {
    return ReactionDTO(
        id = this.id,
        emoji = this.emoji,
        user = this.user.toDTO()
    )
}

fun List<ReactionDTO>.toDomain(): List<Reaction> {
    return map { it.toDomain() }
}

fun List<Reaction>.toDTO(): List<ReactionDTO> {
    return map { it.toDTO() }
}