package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import java.time.LocalDate
import java.util.Date

@Dao
interface ChronoDailyRecordDAO {
    @Query("SELECT * FROM daily_data")
    fun getAllDailyData(): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE packageName = :packageName and dateNumber = :date LIMIT 1")
    fun getDailyData(packageName: String, date: LocalDate): ChronoDailyRecordEntity?

    @Query("SELECT * FROM daily_data WHERE packageName = :packageName")
    fun getAllDailyDataByPackageName(packageName: String): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE dateNumber = :date")
    fun getAllDailyDataByDate(date: LocalDate): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE dateNumber BETWEEN :startDate AND :endDate")
    fun getAllDailyDataByDateRange(startDate: LocalDate, endDate: LocalDate): List<ChronoDailyRecordEntity>

    @Query("SELECT * FROM daily_data WHERE dateNumber BETWEEN :startDate AND :endDate AND packageName = :packageName")
    fun getAllDailyDataByDateRangeAndPackageName(
        startDate: LocalDate,
        endDate: LocalDate,
        packageName: String
    ): List<ChronoDailyRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDailyData(dailyData: ChronoDailyRecordEntity)
}