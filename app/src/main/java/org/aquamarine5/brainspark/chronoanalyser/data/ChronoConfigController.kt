package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.Context
import android.content.SharedPreferences

class ChronoConfigController(context: Context) {
    companion object{
        val SHARED_PREFERNECES_NAME="ChronoConfig"
        val classTag=this::class.java.name
    }

    private val sharedPreferences:SharedPreferences=
        context.getSharedPreferences(SHARED_PREFERNECES_NAME,Context.MODE_PRIVATE)

    var lastUpdateTime:Long by ChronoConfigDelegate(sharedPreferences,"LastUpdateTime",0L)
}