package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import java.time.LocalDate

@Entity(
    "usage_daily", primaryKeys = [
        "packageName", "dateNumber"
    ]
)
data class ChronoDailyUsageEntity(
    val packageName: String,
    val dateNumber: LocalDate,
    var notificationCount: Int = 0,
    var usageTime: Long = 0L,
    var launchCount: Int = 0
)
