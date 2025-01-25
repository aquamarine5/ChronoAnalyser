package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import java.util.Date

@Entity(tableName = "hourly_data", primaryKeys = [
    "packageName","date"
])
data class ChronoDailyDataEntity(
    val packageName:String,
    val date:Date,
    val notificationCount:Int=0,
    val usageTime:Long=0L,
    val startupCount:Int=0
)