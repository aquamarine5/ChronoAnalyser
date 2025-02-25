package org.aquamarine5.brainspark.chronoanalyser

import androidx.compose.runtime.Stable
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDate.ofEpochDay
import java.time.ZoneId
import java.time.ZoneOffset

object DateConverter {
    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?):Int?{
        return date?.toDateNumber()
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDate(dateNumber: Int?):LocalDate?{
        return dateNumber?.let { return@let toLocalDate(it) }
    }


    fun toLocalDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        val rules = zone.rules
        val offset = rules.getOffset(instant)
        return toLocalDate(instant, offset)
    }

    fun toLocalDate(instant: Instant,zoneOffset: ZoneOffset):LocalDate{
        val localSecond = instant.epochSecond + zoneOffset.totalSeconds
        val localEpochDay = Math.floorDiv(localSecond, 60 * 60 * 24)
        return ofEpochDay(localEpochDay)
    }

    @Stable
    fun toLocalDate(dateNumber: Int): LocalDate {
        val year = dateNumber / 10000
        val month = (dateNumber % 10000) / 100
        val day = dateNumber % 100
        return LocalDate.of(year, month, day)
    }

    fun toLocalDate(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return toLocalDate(Instant.ofEpochMilli(timestamp), zone)
    }

    fun toLocalDateUTC(timestamp: Long): LocalDate {
        return toLocalDate(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    }

    @Stable
    fun toDateNumber(localDate: LocalDate): Int {
        return localDate.year * 10000 + localDate.monthValue * 100 + localDate.dayOfMonth
    }

    fun toDateNumber(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): Int {
        return toDateNumber(toLocalDate(timestamp, zone))
    }

    @Stable
    fun toDateString(localDate: LocalDate): String {
        return localDate.toString()
    }

    fun toTimestamp(localDate: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        return localDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun toTimestamp(dateNumber: Int, zone: ZoneId = ZoneId.systemDefault()): Long {
        return toTimestamp(toLocalDate(dateNumber), zone)
    }

    fun toTimestamp(dateNumber: Int,zoneOffset: ZoneOffset):Long{
        return toTimestamp(toLocalDate(dateNumber),zoneOffset)
    }

    @Stable
    fun toTimestampUTC(localDate: LocalDate): Long {
        return toTimestamp(localDate, ZoneOffset.UTC)
    }

    @Stable
    fun toTimestampUTC(dateNumber: Int): Long {
        return toTimestamp(dateNumber, ZoneOffset.UTC)
    }

    fun toDateNumberUTC(endTime: Long): Int {
        return toDateNumber(toLocalDate(endTime, ZoneOffset.UTC))
    }
}

@Stable
fun LocalDate.toTimestampUTC(): Long {
    return DateConverter.toTimestampUTC(this)
}

fun LocalDate.toTimestamp(zone: ZoneId = ZoneId.systemDefault()): Long {
    return DateConverter.toTimestamp(this, zone)
}

@Stable
fun LocalDate.toDateNumber():Int{
    return DateConverter.toDateNumber(this)
}

fun Instant.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate {
    return DateConverter.toLocalDate(this, zone)
}