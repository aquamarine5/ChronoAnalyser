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
import org.aquamarine5.brainspark.chronoanalyser.ui.theme.ChronoAnalyserTheme

class MainActivity : ComponentActivity() {
    private val permissionController=PermissionController(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronoAnalyserTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        if(!permissionController.hasUsageStatsPermission()){
            permissionController.requestUsageStatsPermission()
        }else{
            startScheduledJob(this)
        }
    }
}

fun startScheduledJob(context: Context){
    val componentName = ComponentName(context, ChronoJobService::class.java)
    val jobInfo = JobInfo.Builder(1403, componentName)
        .setPeriodic(3600000) // 1 hour in milliseconds
        .setOverrideDeadline(3700000)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .build()

    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    jobScheduler.schedule(jobInfo)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChronoAnalyserTheme {
        Greeting("Android")
    }
}