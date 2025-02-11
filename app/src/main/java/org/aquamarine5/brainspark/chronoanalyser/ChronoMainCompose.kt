package org.aquamarine5.brainspark.chronoanalyser

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoConfigController
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import java.util.Locale

class ChronoMainCompose(
    private val context: Context
) {
    private val classTag = this::class.simpleName

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DrawMainContent(viewModel: ChronoViewModel= viewModel()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ChronoAnalyser") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            bottomBar = {
                Button(onClick = {
                    viewModel.analysisType=!viewModel.analysisType
                }) {

                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                StartupPage()
                DebugValuePage()
            }
        }
    }

    @Composable
    fun DebugValuePage(){
        val lastUpdateAppProxy=ChronoConfigController.lastUpdateTime(context)
        val lastUpdateRecordProxy=ChronoConfigController.lastUpdateDailyRecordDate(context)
        Text("Last update app: ${lastUpdateAppProxy.getValue()}")
        Text("Last update record: ${lastUpdateRecordProxy.getValue()}")
    }

    @Composable
    fun StartupPage(viewModel: ChronoViewModel=viewModel()) {
        var isUsageInstalled by remember { mutableStateOf(false)}
        var isRecordInstalled by remember { mutableStateOf(false)}

        Log.i(classTag,"Asia uaas")
        if (isUsageInstalled and isRecordInstalled) {
            Log.i(classTag,"Asia uszx")
            if(viewModel.analysisType){
                DailyRecordAnalysisPage()
            }else{
                AnalysisPage()
            }
        } else {
            val updateRecordHandler = remember { ChronoUsageAnalyser.updateRecordFlow(context) }
            val updateUsageHandler= remember{ChronoUsageAnalyser.updateUsageByEventFlow(context)}
            Text(isUsageInstalled.toString())
            Text(isRecordInstalled.toString())
            FlowLinearProgressIndicator(
                updateUsageHandler
            ) { result ->
                Log.i("ChronoMainCompose", "Usage data loaded: $result")
                Log.i(classTag,"Asia us")
                isUsageInstalled = true
            }
            FlowLinearProgressIndicator(
                updateRecordHandler
            ) {
                isRecordInstalled=true
                Log.i(classTag,"Asia usx")
            }
            Text("22")
        }
    }

    @Composable
    fun DailyRecordAnalysisPage(){
        val scope = rememberCoroutineScope()
        var loadUsageData by remember { mutableStateOf<Map<Int,ChronoDailyRecordEntity>>(
            mutableMapOf()
        ) }

        LaunchedEffect(Unit) {
            scope.launch {
                loadUsageData = withContext(Dispatchers.IO) {
                    ChronoDatabase.getInstance(context).chronoDailyDataDAO().getAllDailyData().associateBy { it.dateNumber }
                }
            }
        }
        val maxUsageTime = loadUsageData.maxOfOrNull { it.value.usageTime } ?: 1
        val sortedUsageData = loadUsageData.values.sortedByDescending {
            it.usageTime
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            itemsIndexed(sortedUsageData) { index, usageData ->
                key(index) {
                    AppUsageCard(usageData, maxUsageTime)
                }
            }
        }
    }

    @Composable
    fun AnalysisPage() {
        val scope = rememberCoroutineScope()
        var loadUsageData by remember { mutableStateOf<List<ChronoAppEntity>>(emptyList()) }

        LaunchedEffect(Unit) {
            scope.launch {
                loadUsageData = withContext(Dispatchers.IO) {
                    ChronoDatabase.getInstance(context).chronoAppDAO().getAllApps()
                }
            }
        }
        val maxUsageTime = loadUsageData.maxOfOrNull { it.usageTime } ?: 1
        val sortedUsageData = loadUsageData.sortedByDescending {
            it.usageTime
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            itemsIndexed(sortedUsageData) { index, usageData ->
                key(index) {
                    AppUsageCard(usageData, maxUsageTime)
                }
            }
        }
    }

    @Composable
    fun AppUsageCard(recordEntity: ChronoDailyRecordEntity,maxUsageTime: Long){
        with(recordEntity){
            val appName = getAppName(context,packageName)
            val appIcon = getAppIcon(context,packageName)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = appIcon,
                    contentDescription = appName,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Usage time: ${formatTime(usageTime)}; NC: $notificationCount; SC: $startupCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { usageTime / maxUsageTime.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        drawStopIndicator = {},
                        gapSize = (-1).dp
                    )
                }
            }
        }
    }

    @Composable
    fun AppUsageCard(appEntity: ChronoAppEntity, maxUsageTime: Long) {
        with(appEntity) {
            val appName = getAppName(context, packageName)
            val appIcon = getAppIcon(context, packageName)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = appIcon,
                    contentDescription = appName,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Usage time: ${formatTime(usageTime)}; NC: $notificationCount; SC: $startupCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = { usageTime / maxUsageTime.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        drawStopIndicator = {},
                        gapSize = (-1).dp
                    )
                }
            }

        }
    }

    private fun formatTime(milliseconds: Long): String {
        val hours = milliseconds / 3600000
        val minutes = (milliseconds % 3600000) / 60000
        val seconds = (milliseconds % 60000) / 1000
        return String.format(Locale.SIMPLIFIED_CHINESE, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun getAppName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            packageName
        }
    }

    private fun getAppIcon(context: Context, packageName: String): ImageBitmap {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            when (val drawable = packageManager.getApplicationIcon(applicationInfo)) {
                is BitmapDrawable -> drawable.bitmap.asImageBitmap()
                is AdaptiveIconDrawable -> {
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap.asImageBitmap()
                }

                else -> ImageBitmap(
                    1,
                    1
                ) // Return a default empty bitmap in case of an unsupported drawable type
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ImageBitmap(1, 1) // Return a default empty bitmap in case of an error
        }
    }
}
