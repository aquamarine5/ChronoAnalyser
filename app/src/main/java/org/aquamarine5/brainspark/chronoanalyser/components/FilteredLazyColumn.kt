package org.aquamarine5.brainspark.chronoanalyser.components

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import java.util.concurrent.TimeUnit

private const val PAGE_REFRESH_ADD_COUNT = 30

@Composable
fun FilteredLazyColumn(loadUsageData: List<ChronoAppEntity>) {
    val listState = rememberLazyListState()

    val maxUsageTime = loadUsageData.maxOf { it.usageTime }
    val sortedUsageData = loadUsageData.sortedByDescending { it.usageTime }
    val displayedUsageData = remember {
            sortedUsageData.subList(
                0,
                if (sortedUsageData.size > PAGE_REFRESH_ADD_COUNT) PAGE_REFRESH_ADD_COUNT else sortedUsageData.size
            ).toMutableStateList()

    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collect {
            val displayedLength = displayedUsageData.size - 1
            if (it.visibleItemsInfo.lastOrNull()?.index == displayedLength) {
                val newUsageData = sortedUsageData.subList(
                    displayedLength + 1,
                    if (displayedLength + PAGE_REFRESH_ADD_COUNT + 1 < sortedUsageData.size) displayedLength + PAGE_REFRESH_ADD_COUNT + 1 else sortedUsageData.size
                )
                displayedUsageData.addAll(newUsageData)
            }
        }
    }
    if (loadUsageData.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            state = listState
        ) {
            items(displayedUsageData.toList()){
                key(it.packageName){
                    AppUsageCard(it, maxUsageTime)
                }
            }
        }
    }

}