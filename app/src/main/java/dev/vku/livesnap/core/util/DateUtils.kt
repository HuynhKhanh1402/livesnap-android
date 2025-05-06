package dev.vku.livesnap.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    fun String.convertToDate(): Date {
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.parse(this)
    }

    fun Date.convertToString(): String {
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(this)
    }
}