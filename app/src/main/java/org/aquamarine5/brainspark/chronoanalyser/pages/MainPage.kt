package org.aquamarine5.brainspark.chronoanalyser.pages

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.aquamarine5.brainspark.chronoanalyser.BuildConfig
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.ChronoUsageAnalyser
import org.aquamarine5.brainspark.chronoanalyser.DataStoreSerializer.datastore
import org.aquamarine5.brainspark.chronoanalyser.R
import org.aquamarine5.brainspark.chronoanalyser.component.AppUsageCard
import org.aquamarine5.brainspark.chronoanalyser.component.FlowLinearProgressIndicator
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppUsageEntity
import org.aquamarine5.brainspark.stackbricks.StackbricksComponent
import org.aquamarine5.brainspark.stackbricks.StackbricksPolicy
import org.aquamarine5.brainspark.stackbricks.StackbricksService
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuConfiguration
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuMessageProvider
import org.aquamarine5.brainspark.stackbricks.providers.qiniu.QiniuPackageProvider
import org.aquamarine5.brainspark.stackbricks.rememberStackbricksStatus
import java.util.concurrent.TimeUnit

@Composable
fun MainPage() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            var isLoading by remember { mutableStateOf(true) }


            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), context.packageName
                )
            }
            val isPermissionAllowed = if (mode == AppOpsManager.MODE_DEFAULT) {
                (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
            } else {
                (mode == AppOpsManager.MODE_ALLOWED)
            }


            val progressHandler = remember { ChronoUsageAnalyser.updateUsageData(context) }
            Crossfade(isPermissionAllowed) {
                if (it) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_circle_alert),
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("请在系统设置中允许应用访问使用情况数据")
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                data=Uri.fromParts("package", context.packageName, null)
                            })
                        }) {
                            Text("前往设置")
                        }
                    }
                } else {
                    Crossfade(isLoading) {
                        if (it) {
                            FlowLinearProgressIndicator(
                                progressHandler,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                isLoading = false
                            }
                        } else {
                            var usageData by remember {
                                mutableStateOf<List<ChronoAppUsageEntity>>(
                                    emptyList()
                                )
                            }
                            LaunchedEffect(Unit) {
                                coroutineScope.launch {
                                    usageData = withContext(Dispatchers.IO) {
                                        ChronoDatabase.getInstance(context).chronoAppUsageDAO()
                                            .getAllApps()
                                    }
                                }
                            }

                            if (usageData.isNotEmpty()) {
                                val maxUsageTime = usageData.maxOf { it.usageTime }
                                val qiniuConfiguration = QiniuConfiguration(
                                    "cdn.aquamarine5.fun",
                                    referer = "http://cdn.aquamarine5.fun/",
                                    configFilePath = "chaoxingsignfaker_stackbricks_v2_manifest.json",
                                    okHttpClient = OkHttpClient().newBuilder()
                                        .callTimeout(20, TimeUnit.MINUTES)
                                        .readTimeout(20, TimeUnit.MINUTES)
                                        .writeTimeout(20, TimeUnit.MINUTES)
                                        .build()
                                )
                                StackbricksComponent(
                                    StackbricksService(
                                    LocalContext.current,
                                    QiniuMessageProvider(qiniuConfiguration),
                                    QiniuPackageProvider(qiniuConfiguration),
                                    rememberStackbricksStatus(),
                                    stackbricksPolicy = StackbricksPolicy(
                                        versionName = BuildConfig.VERSION_NAME,
                                        isAllowedToDisableCheckUpdateOnLaunch = false,
                                        isForceInstallValueCallback = false,
                                        versionCode = null
                                    ),
                                )
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    items(usageData.sortedByDescending { it.usageTime }) { usage ->
                                        key(usage.packageName) { AppUsageCard(usage, maxUsageTime) }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}