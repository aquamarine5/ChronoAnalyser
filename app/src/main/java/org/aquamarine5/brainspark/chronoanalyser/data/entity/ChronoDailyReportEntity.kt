package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity("usage_daily_report")
data class ChronoDailyReportEntity(
    @PrimaryKey
    val dateNumber:LocalDate,
    var allUsageTime:Long=0L,
    var allLaunchCount:Int=0,
    var allNotificationCount:Int=0
)
