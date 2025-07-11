package de.tillhub.paymentengine.zvt.helper

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal fun String.toInstant(
    dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
): Instant {
    return try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        when (dateTimeFormatter) {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ISO_OFFSET_DATE,
            DateTimeFormatter.ISO_ORDINAL_DATE,
            DateTimeFormatter.ISO_WEEK_DATE -> LocalDate.parse(this, dateTimeFormatter).atStartOfDay()
                .toInstant(ZoneOffset.UTC)

            else -> LocalDateTime.parse(this, dateTimeFormatter).toInstant(ZoneOffset.UTC)
        }
    }
}

internal fun Instant.toISOString(
    dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT
): String {
    return dateTimeFormatter.withZone(ZoneId.from(ZoneOffset.UTC)).format(this)
}
