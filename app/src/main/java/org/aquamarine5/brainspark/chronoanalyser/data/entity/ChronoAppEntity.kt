package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "apps")
data class ChronoAppEntity(
    @PrimaryKey
    val packageName:String,
    val packageLabel:String,
    val usageTime:Long
)