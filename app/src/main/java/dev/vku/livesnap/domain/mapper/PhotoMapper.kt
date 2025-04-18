package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.PhotoDTO
import dev.vku.snaplive.domain.model.Photo
import java.util.Date

fun PhotoDTO.toDomain() = Photo(
    photoId = this.photoId,
    userId = this.userId,
    caption = this.caption,
    image = this.image,
    createdAt = Date(this.createdAt)
)

fun Photo.toDTO() = PhotoDTO(
    photoId = this.photoId,
    userId = this.userId,
    caption = this.caption,
    image = this.image,
    createdAt = this.createdAt.time
)
