package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDayRecordEntity

@Dao
interface ChronoDayRecordDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDayRecord(record: ChronoDayRecordEntity)
}