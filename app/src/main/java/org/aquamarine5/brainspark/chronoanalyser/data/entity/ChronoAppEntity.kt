package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "apps")
data class ChronoAppEntity(
    @PrimaryKey
    val packageName: String,
    val packageLabel: String,
    var usageTime: Long = 0L,
    var notificationCount: Int = 0,
    var startupCount: Int = 0
)