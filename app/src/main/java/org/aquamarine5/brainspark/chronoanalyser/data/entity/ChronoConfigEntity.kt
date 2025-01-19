package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import java.sql.Timestamp

@Entity(tableName = "config")
data class ChronoConfigEntity(
    val lastUpdateTime: Timestamp,
    val isInstalled:Boolean
)