package org.aquamarine5.brainspark.chronoanalyser

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStatsManager
import android.content.Context

class ChronoJobService:JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    fun updateChronoData(){
        val usageStatsManager=getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    }
}