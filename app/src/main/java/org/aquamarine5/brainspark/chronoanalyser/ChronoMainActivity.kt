package org.aquamarine5.brainspark.chronoanalyser

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.umeng.analytics.MobclickAgent
import org.aquamarine5.brainspark.chronoanalyser.ui.theme.ChronoAnalyserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UMHelper.preInit(this)
        enableEdgeToEdge()
        setContent {
            ChronoAnalyserTheme {
                DrawMainContent()
            }
        }
        if (!PermissionController.hasUsageStatsPermission(this)) {
            PermissionController.requestUsageStatsPermission(this)
        } else {
            //startScheduledJob()
        }
    }

    override fun onStop() {
        super.onStop()
        MobclickAgent.onKillProcess(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        MobclickAgent.onKillProcess(this)
    }

    private fun startScheduledJob() {
        val componentName = ComponentName(this, ChronoJobService::class.java)
        val jobInfo = JobInfo.Builder(1403, componentName)
            .setPeriodic(3600000) // 1 hour in milliseconds
            .setOverrideDeadline(3700000)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }
}