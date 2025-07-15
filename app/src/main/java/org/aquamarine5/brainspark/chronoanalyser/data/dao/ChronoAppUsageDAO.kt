package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppUsageEntity

@Dao
interface ChronoAppUsageDAO {
    @Upsert
    fun upsertApp(app: ChronoAppUsageEntity)

    @Query("UPDATE usage_apps SET usageTime = usageTime + :addUsageTime, notificationCount = notificationCount + :addNotificationCount, launchCount = launchCount + :addStartupCount WHERE packageName = :packageName")
    fun addAppCounts(
        packageName: String,
        addUsageTime: Long,
        addNotificationCount: Int,
        addStartupCount: Int
    )

    @Query("SELECT * FROM usage_apps")
    fun getAllApps(): List<ChronoAppUsageEntity>
}