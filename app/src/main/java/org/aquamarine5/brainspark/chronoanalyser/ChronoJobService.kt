package org.aquamarine5.brainspark.chronoanalyser

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStatsManager
import android.content.Context

class ChronoJobService:JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        updateChronoData()
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    private fun updateChronoData(){
        ChronoUsageAnalyser.updateUsageByEventFlow(this)
    }
}