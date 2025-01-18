package org.aquamarine5.brainspark.chronoanalyser.data

import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoAppDAO
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoConfigDAO
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoDayRecordDAO
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoHourRecordDAO
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoConfigEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoHourRecordEntity

@Database(
    entities = [ChronoHourRecordEntity::class, ChronoConfigEntity::class, ChronoAppEntity::class],
    version = 1)
abstract class ChronoDatabase:RoomDatabase() {
    override fun clearAllTables() {
        TODO("Not yet implemented")
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
        TODO("Not yet implemented")
    }

    abstract fun chronoDayRecordDAO(): ChronoDayRecordDAO
    abstract fun chronoHourRecordDAO(): ChronoHourRecordDAO
    abstract fun chronoConfigDAO(): ChronoConfigDAO
    abstract fun chronoAppDAO(): ChronoAppDAO
}