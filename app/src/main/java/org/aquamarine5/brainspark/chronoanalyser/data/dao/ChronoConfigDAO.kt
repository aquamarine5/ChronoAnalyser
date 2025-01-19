package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoConfigEntity

@Dao
interface ChronoConfigDAO {
    @Query("SELECT * FROM config LIMIT 1")
    fun getConfig(): ChronoConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDefaultConfig(config: ChronoConfigEntity)
}