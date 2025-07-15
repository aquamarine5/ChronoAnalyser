package org.aquamarine5.brainspark.chronoanalyser.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.ChronoUsageAnalyser
import org.aquamarine5.brainspark.chronoanalyser.component.AppUsageCard
import org.aquamarine5.brainspark.chronoanalyser.component.FlowLinearProgressIndicator
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppUsageEntity

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
            val progressHandler = remember { ChronoUsageAnalyser.updateUsageData(context) }
            if (isLoading) {
                FlowLinearProgressIndicator(
                    progressHandler,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    isLoading = false
                }
            } else {
                var usageData by remember { mutableStateOf<List<ChronoAppUsageEntity>>(emptyList()) }
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        usageData = withContext(Dispatchers.IO) {
                            ChronoDatabase.getInstance(context).chronoAppUsageDAO().getAllApps()
                        }
                    }
                }

                if (usageData.isNotEmpty()) {
                    val maxUsageTime = usageData.maxOf { it.usageTime }

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