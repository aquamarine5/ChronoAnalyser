package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyUsageEntity
import java.time.LocalDate

@Dao
interface ChronoDailyUsageDAO {
    @Upsert
    fun upsertDailyData(dailyUsageEntity: ChronoDailyUsageEntity)

    @Query("SELECT * FROM usage_daily WHERE dateNumber = :date")
    fun getDailyUsage(date: LocalDate): ChronoDailyUsageEntity?
}