package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity

@Entity(tableName = "day_records")
data class ChronoDayRecordEntity(
    val packageName:String,
    val usingTime:Long,
    val year: Int,
    val month: Int,
    val day:Int,
)
