package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.PhotoDTO
import dev.vku.snaplive.domain.model.Photo
import java.util.Date

fun PhotoDTO.toDomain() = Photo(
    photoId = this.photoId,
    senderId = this.senderId,
    receiverId = this.receiverId,
    url = this.url,
    timestamp = Date(this.timestamp)
)

fun Photo.toDTO() = PhotoDTO(
    photoId = this.photoId,
    senderId = this.senderId,
    receiverId = this.receiverId,
    url = this.url,
    timestamp = this.timestamp.time
)
