package org.aquamarine5.brainspark.chronoanalyser

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDate.ofEpochDay
import java.time.ZoneId

object DateConverter {
    fun toLocalDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        val rules = zone.rules
        val offset = rules.getOffset(instant)
        val localSecond = instant.epochSecond + offset.totalSeconds
        val localEpochDay = Math.floorDiv(localSecond, 60 * 60 * 24)
        return ofEpochDay(localEpochDay)
    }

    fun toLocalDate(dateNumber: Int): LocalDate {
        val year = dateNumber / 10000
        val month = (dateNumber % 10000) / 100
        val day = dateNumber % 100
        return LocalDate.of(year, month, day)
    }

    fun toLocalDate(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate {
        return toLocalDate(Instant.ofEpochMilli(timestamp), zone)
    }

    fun toDateNumber(localDate: LocalDate): Int {
        return localDate.year * 10000 + localDate.monthValue * 100 + localDate.dayOfMonth
    }

    fun toDateString(localDate: LocalDate): String {
        return localDate.toString()
    }

    fun toTimestamp(localDate: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        return localDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun toTimestampUTC(localDate: LocalDate): Long {
        return toTimestamp(localDate, ZoneId.of("UTC"))
    }
}