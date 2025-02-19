package org.aquamarine5.brainspark.chronoanalyser.data

import androidx.room.TypeConverter
import org.aquamarine5.brainspark.chronoanalyser.DateConverter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDate.ofEpochDay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneRules
import java.util.Date
import java.util.Locale

@Deprecated("Please use DateConverter", replaceWith = ReplaceWith("DateConverter and LocalDate"), level = DeprecationLevel.ERROR)
object DateSQLConverter {
    private val dateFormatUTC = lazy { SimpleDateFormat("yyyyMMdd", Locale.ROOT) }
    private val dateFormat = lazy { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }
    private val dateFormatterUTC = lazy { DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT) }
    private val dateFormatter =
        lazy { DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault()) }

    @TypeConverter
    @JvmStatic
    fun toDate(dateNumber: Int): Date {
        return dateFormat.value.parse(dateNumber.toString()) ?: Date()
    }

    @TypeConverter
    @JvmStatic
    fun toDateNumber(date: Date): Int {
        return dateFormat.value.format(date).toInt()
    }

    fun toDate(timestamp: Long): Date {
        return Date.from(Instant.ofEpochMilli(timestamp))
    }

    fun toDateNumber(timestamp: Long): Int {
        return dateFormat.value.format(Date.from(Instant.ofEpochMilli(timestamp))).toInt()
    }

    fun toDateNumberUTC(timestamp: Long): Int {
        return dateFormatUTC.value.format(Date(timestamp)).toInt()
    }

    fun toTimestampUTC(dateNumber: Int): Long {
        return LocalDate.parse(dateNumber.toString(), dateFormatterUTC.value)
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    fun toTimestamp(dateNumber: Int): Long {
        return LocalDate.parse(dateNumber.toString(), dateFormatter.value).atStartOfDay()
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}