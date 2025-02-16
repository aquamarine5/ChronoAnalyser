package org.aquamarine5.brainspark.chronoanalyser

import android.util.Log
import android.util.TimeUtils
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
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.aquamarine5.brainspark.chronoanalyser.components.EnhancedDatePicker
import org.aquamarine5.brainspark.chronoanalyser.components.FlowLinearProgressIndicator
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoConfigController
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.DateSQLConverter
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import org.aquamarine5.brainspark.stackbricks.StackbricksComponent
import org.aquamarine5.brainspark.stackbricks.StackbricksStateService
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuConfiguration
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuMessageProvider
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuPackageProvider
import org.aquamarine5.brainspark.stackbricks.rememberStackbricksStatus
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawMainContent(viewModel: ChronoViewModel = viewModel()) {
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
            val okHttpClient=OkHttpClient.Builder()
                .callTimeout(20,TimeUnit.MINUTES)
                .readTimeout(20,TimeUnit.MINUTES)
                .writeTimeout(20,TimeUnit.MINUTES)
                .build()
            val qiniuConfiguration =
                QiniuConfiguration("cdn.aquamarine5.fun",
                    referer = "http://cdn.aquamarine5.fun/",
                    okHttpClient = okHttpClient)
            val stackbricksState = rememberStackbricksStatus()
            StackbricksComponent(
                StackbricksStateService(
                    LocalContext.current,
                    QiniuMessageProvider(qiniuConfiguration),
                    QiniuPackageProvider(qiniuConfiguration),
                    stackbricksState
                )
            )
        },
        bottomBar = {
            Button(onClick = {
                viewModel.analysisType = !viewModel.analysisType
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
fun DebugValuePage() {
    val context = LocalContext.current
    val lastUpdateAppProxy = ChronoConfigController.lastUpdateTime(context)
    val lastUpdateRecordProxy = ChronoConfigController.lastUpdateDailyRecordDate(context)
    Text("Last update app: ${lastUpdateAppProxy.getValue()}")
    Text("Last update record: ${lastUpdateRecordProxy.getValue()}")
}

@Composable
fun StartupPage(viewModel: ChronoViewModel = viewModel()) {
    val TAG = "StartupPage"
    val context = LocalContext.current
    var isUsageInstalled by remember { mutableStateOf(false) }
    var isRecordInstalled by remember { mutableStateOf(false) }

    if (isUsageInstalled and isRecordInstalled) {
        if (viewModel.analysisType) {
            DailyRecordAnalysisPage()
        } else {
            AnalysisPage()
        }
    } else {
        val updateRecordHandler = remember { ChronoUsageAnalyser.updateRecordFlow(context) }
        val updateUsageHandler =
            remember { ChronoUsageAnalyser.updateUsageByEventFlow(context) }
        Text(isUsageInstalled.toString())
        Text(isRecordInstalled.toString())
        FlowLinearProgressIndicator(
            updateUsageHandler
        ) { result ->
            Log.i(TAG, "Usage data loaded: $result")
            Log.i(TAG, "Asia us")
            isUsageInstalled = true
        }
        FlowLinearProgressIndicator(
            updateRecordHandler
        ) {
            isRecordInstalled = true
            Log.i(TAG, "Asia usx")
        }
        Text("22")
    }
}

@Composable
fun DailyRecordAnalysisPage() {
    val scope = rememberCoroutineScope()
    var loadUsageData by remember {
        mutableStateOf<Map<Int, List<ChronoDailyRecordEntity>>>(
            mutableMapOf()
        )
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        scope.launch {
            loadUsageData = withContext(Dispatchers.IO) {
                ChronoDatabase.getInstance(context).chronoDailyDataDAO().getAllDailyData()
                    .groupBy { it.dateNumber }
            }
        }
    }
    val dateRange = LongRange(
        DateSQLConverter.toTimestamp(loadUsageData.minOfOrNull { it.key } ?: 20070304),
        DateSQLConverter.toTimestamp(loadUsageData.maxOfOrNull { it.key } ?: 20170615)
    )
    var currentDateState by remember {
        mutableIntStateOf(loadUsageData.maxOfOrNull { it.key } ?: 1)
    }
    val sortedUsageData = loadUsageData[currentDateState]?.sortedByDescending {
        it.usageTime
    } ?: emptyList()
    val maxUsageTime = sortedUsageData.getOrNull(0)?.usageTime ?: 0
    Column {
        EnhancedDatePicker(dateRange) {
            currentDateState = it
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

}

@Composable
fun AnalysisPage() {
    val scope = rememberCoroutineScope()
    var loadUsageData by remember { mutableStateOf<List<ChronoAppEntity>>(emptyList()) }
    val context = LocalContext.current
    UMHelper.init(context)
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
fun AppUsageCard(recordEntity: ChronoDailyRecordEntity, maxUsageTime: Long) {
    with(recordEntity) {
        val appName = getAppName(LocalContext.current, packageName)
        val appIcon = getAppIcon(LocalContext.current, packageName)
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
        val appName = getAppName(LocalContext.current, packageName)
        val appIcon = getAppIcon(LocalContext.current, packageName)
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

