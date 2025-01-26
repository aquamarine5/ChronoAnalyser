package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import android.content.SharedPreferences

object ChronoConfigController {
    private const val SHARED_PREFERENCES_NAME = "ChronoConfig"

    const val DEFAULT_LAST_UPDATE_RECORD_DATE=0
    const val DEFAULT_LAST_UPDATE_TIME=1172981711250L

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
            DEFAULT_LAST_UPDATE_RECORD_DATE
        )
}