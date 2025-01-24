package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "hourly_data")
data class ChronoHourlyDataEntity(
    @PrimaryKey
    val packageName:String,
    @PrimaryKey
    val date:Date,
    val notificationCount:Int=0,
    val usageTime:Long=0L,
    val startupCount:Int=0
)