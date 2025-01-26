package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import java.util.Date

@Dao
interface ChronoDailyRecordDAO {
    @Query("SELECT * FROM daily_data")
    fun getAllDailyData(): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE packageName = :packageName and dateNumber = :date LIMIT 1")
    fun getDailyData(packageName: String,date: Date): ChronoDailyRecordEntity?

    @Query("SELECT * FROM daily_data WHERE packageName = :packageName")
    fun getAllDailyDataByPackageName(packageName: String): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE dateNumber = :date")
    fun getAllDailyDataByDate(date: Date): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE dateNumber BETWEEN :startDate AND :endDate")
    fun getAllDailyDataByDateRange(startDate: Date,endDate: Date): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE dateNumber BETWEEN :startDate AND :endDate AND packageName = :packageName")
    fun getAllDailyDataByDateRangeAndPackageName(startDate: Date,endDate: Date,packageName: String): List<ChronoDailyRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDailyData(dailyData: ChronoDailyRecordEntity)
}