package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.TypeConverters
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
    val dateNumber: LocalDate,
    var notificationCount: Int = 0,
    var usageTime: Long = 0L,
    var startupCount: Int = 0
)