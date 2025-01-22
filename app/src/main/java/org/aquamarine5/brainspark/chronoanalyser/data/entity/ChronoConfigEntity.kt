package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "config")
data class ChronoConfigEntity(
    @PrimaryKey
    val userid:String,
    val lastUpdateTime: Long,
    val isInstalled:Boolean
)