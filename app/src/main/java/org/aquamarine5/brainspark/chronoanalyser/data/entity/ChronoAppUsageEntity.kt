package org.aquamarine5.brainspark.chronoanalyser.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("usage_apps")
data class ChronoAppUsageEntity(
    @PrimaryKey
    val packageName:String,
    val packageLabel:String,
    var usageTime:Long=0L,
    var launchCount:Int=0,
    var notificationCount:Int=0
)
