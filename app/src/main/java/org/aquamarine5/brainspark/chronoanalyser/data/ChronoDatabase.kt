package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.aquamarine5.brainspark.chronoanalyser.DateConverter
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoAppUsageDAO
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoDailyReportDAO
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoDailyUsageDAO
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppUsageEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyReportEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyUsageEntity

@Database(
    entities = [
        ChronoDailyUsageEntity::class,
        ChronoAppUsageEntity::class,
        ChronoDailyReportEntity::class
    ],
    version = 1
)
@TypeConverters(DateConverter::class)
abstract class ChronoDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: ChronoDatabase? = null
        fun getInstance(context: Context): ChronoDatabase {
            if (INSTANCE == null) {
                synchronized(ChronoDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ChronoDatabase::class.java,
                        "chrono_v2.db"
                    )
                        .build()
                }
            }
            return INSTANCE!!
        }
    }

    abstract fun chronoAppUsageDAO(): ChronoAppUsageDAO
    abstract fun chronoDailyUsageDAO(): ChronoDailyUsageDAO
    abstract fun chronoDailyReportDAO(): ChronoDailyReportDAO
}