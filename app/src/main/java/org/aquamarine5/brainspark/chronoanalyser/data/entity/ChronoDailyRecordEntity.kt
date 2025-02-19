package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import org.aquamarine5.brainspark.chronoanalyser.DateConverter
import java.time.LocalDate
import java.util.Date

@Entity(
    tableName = "daily_data", primaryKeys = [
        "packageName", "dateNumber"
    ]
)
data class ChronoDailyRecordEntity(
    val packageName: String,
    val dateNumber: Int,
    var notificationCount: Int = 0,
    var usageTime: Long = 0L,
    var startupCount: Int = 0
) {
    constructor(
        packageName: String,
        date: LocalDate,
        notificationCount: Int = 0,
        usageTime: Long = 0L,
        startupCount: Int = 0
    ) : this(
        packageName,
        DateConverter.toDateNumber(date),
        notificationCount,
        usageTime,
        startupCount
    )
}