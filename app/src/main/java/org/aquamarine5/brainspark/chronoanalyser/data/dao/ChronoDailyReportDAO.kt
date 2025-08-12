package org.aquamarine5.brainspark.chronoanalyser.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyReportEntity
import java.time.LocalDate

@Dao
interface ChronoDailyReportDAO {
    @Upsert
    fun upsertDailyReport(value: ChronoDailyReportEntity)

    @Query("SELECT * FROM usage_daily_report WHERE dateNumber = :date")
    fun getDailyReport(date: LocalDate): ChronoDailyReportEntity?
}