package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import java.time.Month
import java.time.Year

@Entity(tableName = "hour_records")
data class ChronoHourRecordEntity(
    val packageName:String,
    val usingTime:Long,
    val year: Int,
    val month: Int,
    val day:Int,
    val hour:Int
)
