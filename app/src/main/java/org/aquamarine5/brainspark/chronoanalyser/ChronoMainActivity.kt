package org.aquamarine5.brainspark.chronoanalyser

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.umeng.analytics.MobclickAgent
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.ui.theme.ChronoAnalyserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UMengController.init(this)
        enableEdgeToEdge()
        setContent {
            ChronoAnalyserTheme {
                ChronoMainCompose(this)
                    .DrawMainContent()
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