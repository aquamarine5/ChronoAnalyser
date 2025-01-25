package org.aquamarine5.brainspark.chronoanalyser

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class ChronoMainCompose(
    private val context: Context
) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DrawMainContent() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chrono Analyser") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                StartupPage()
            }
        }
    }


    @Composable
    fun StartupPage() {
        var isInstalled by remember { mutableStateOf(false) }
        var loadUsageData by remember { mutableStateOf<Map<String, Long>?>(null) }

        if (isInstalled) {
            AnalysisPage(loadUsageData!!)
            Text("11")
        } else {
            FlowLinearProgressIndicator(
                ChronoUsageAnalyser.loadUsageByEventFlow(context)
            ) { result ->
                loadUsageData = result
                Log.i("ChronoMainCompose", "Usage data loaded: ${result}")
                isInstalled = true
            }
            Text("22")
        }
    }

    @Composable
    fun AnalysisPage(loadUsageData: Map<String, Long>) {
        val maxUsageTime = loadUsageData.values.maxOrNull() ?: 0L
        val sortedUsageData = loadUsageData.toList().sortedByDescending { it.second }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            itemsIndexed(sortedUsageData) { index,(packageName, usageTime) ->
                val appName = getAppName(context, packageName)
                val appIcon = getAppIcon(context, packageName)
                key(index){
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
                                text = "Usage time: ${formatTime(usageTime)}",
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
                else -> ImageBitmap(1, 1) // Return a default empty bitmap in case of an unsupported drawable type
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ImageBitmap(1, 1) // Return a default empty bitmap in case of an error
        }
    }
}

