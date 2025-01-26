package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import java.util.Date

@Entity(tableName = "daily_data", primaryKeys = [
    "packageName","dateNumber"
])
data class ChronoDailyRecordEntity(
    val packageName:String,
    val dateNumber:Int,
    var notificationCount:Int=0,
    var usageTime:Long=0L,
    var startupCount:Int=0
)