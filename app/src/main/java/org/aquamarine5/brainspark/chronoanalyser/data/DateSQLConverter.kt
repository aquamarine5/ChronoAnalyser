package org.aquamarine5.brainspark.chronoanalyser.data

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

object DateSQLConverter {
    private val dateFormat = lazy { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }

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
}