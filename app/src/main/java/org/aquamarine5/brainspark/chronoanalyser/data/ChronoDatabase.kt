package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import android.provider.Settings
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.aquamarine5.brainspark.chronoanalyser.MainActivity
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoAppDAO
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import java.sql.Timestamp

@Database(
    entities = [
        ChronoAppEntity::class],
    version = 1)
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
}