package org.aquamarine5.brainspark.chronoanalyser.v1.data.dao

import androidx.room.Dao
import androidx.room.Upsert
import org.aquamarine5.brainspark.chronoanalyser.v1.data.entity.ChronoDailyUsageEntity

@Dao
interface ChronoDailyUsageDAO {
    @Upsert
    fun upsertDailyData(dailyUsageEntity: ChronoDailyUsageEntity)
}