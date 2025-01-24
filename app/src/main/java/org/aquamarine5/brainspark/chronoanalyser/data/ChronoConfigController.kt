package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import android.content.SharedPreferences

object ChronoConfigController {
    private const val SHARED_PREFERENCES_NAME = "ChronoConfig"
    private val classTag = this::class.java.name


    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun lastUpdateTime(context: Context) =
        ChronoConfigProxy(
            getSharedPreferences(context),
            "lastUpdateTime",
            0L
        )
}