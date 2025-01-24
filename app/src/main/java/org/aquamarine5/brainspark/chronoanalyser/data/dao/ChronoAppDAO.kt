package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity

@Dao
interface ChronoAppDAO{
    @Query("SELECT * FROM apps")
    fun getAllApps(): List<ChronoAppEntity>

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    fun getAppByPackageName(packageName:String): ChronoAppEntity?

    @Upsert
    fun upsertApp(app: ChronoAppEntity)

    @Query("UPDATE apps SET usageTime = usageTime + :addUsageTime, notificationCount = notificationCount + :addNotificationCount, startupCount = startupCount + :addStartupCount WHERE packageName = :packageName")
    fun addAppCounts(packageName: String, addUsageTime:Long,addNotificationCount:Int,addStartupCount:Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertApp(app:ChronoAppEntity)

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    fun deleteAppByPackageName(packageName: String)

    @Query("DELETE FROM apps")
    fun deleteAllApps()
}