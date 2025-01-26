package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoAppDAO
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoDailyRecordDAO
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity

@Database(
    entities = [
        ChronoAppEntity::class,
        ChronoDailyRecordEntity::class
               ],
    version = 1)
@TypeConverters(
    DateSQLConverter::class
)
abstract class ChronoDatabase:RoomDatabase() {
    companion object{
        @Volatile
        private var INSTANCE: ChronoDatabase? = null
        fun getInstance(context: Context): ChronoDatabase {
            if (INSTANCE == null) {
                synchronized(ChronoDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ChronoDatabase::class.java,
                        "chrono.db"
                    )
                        .build()
                }
            }
            return INSTANCE!!
        }
    }

    abstract fun chronoAppDAO(): ChronoAppDAO
    abstract fun chronoDailyDataDAO():ChronoDailyRecordDAO
}