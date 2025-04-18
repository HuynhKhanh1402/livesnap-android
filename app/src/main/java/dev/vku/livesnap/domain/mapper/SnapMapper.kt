package dev.vku.livesnap.domain.mapper

import dev.vku.livesnap.data.remote.dto.SnapDTO
import dev.vku.livesnap.domain.model.Snap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun SnapDTO.toDomain(): Snap {
    return Snap(
        id = this.id,
        caption = this.caption,
        image = this.image,
        user = this.user.toDomain(),
        isOwner = this.isOwner,
        createdAt = convertStringToDate(this.createdAt)
    )
}

fun Snap.toSnapDTO(): SnapDTO {
    return SnapDTO(
        id = this.id,
        caption = this.caption,
        image = this.image,
        user = this.user.toDTO(),
        isOwner = this.isOwner,
        createdAt = convertDateToString(this.createdAt)
    )
}


private fun convertStringToDate(dateString: String): Date {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.parse(dateString)
}

private fun convertDateToString(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(date)
}


fun List<SnapDTO>.toSnapList(): List<Snap> {
    return this.map { it.toDomain() }
}

fun List<Snap>.toSnapDTOList(): List<SnapDTO> {
    return this.map { it.toSnapDTO() }
}