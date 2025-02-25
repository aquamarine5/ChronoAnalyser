package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import android.content.SharedPreferences
import org.aquamarine5.brainspark.chronoanalyser.DateConverter
import java.time.LocalDate

object ChronoConfigController {
    private const val SHARED_PREFERENCES_NAME = "ChronoConfig"

    val DEFAULT_LAST_UPDATE_RECORD_DATE: LocalDate = LocalDate.of(2000,1,1)
    const val DEFAULT_LAST_UPDATE_TIME = 1172981711250L

    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun lastUpdateTime(context: Context) =
        ChronoConfigProxy(
            getSharedPreferences(context),
            "lastUpdateTime",
            DEFAULT_LAST_UPDATE_TIME
        )

    fun lastUpdateDailyRecordDate(context: Context) =
        ChronoConfigProxy(
            getSharedPreferences(context),
            "lastUpdateDailyRecordDate",
            DateConverter.toDateNumber(DEFAULT_LAST_UPDATE_RECORD_DATE)
        )
}