package org.aquamarine5.brainspark.chronoanalyser.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import org.aquamarine5.brainspark.chronoanalyser.formatTime
import org.aquamarine5.brainspark.chronoanalyser.getAppIcon
import org.aquamarine5.brainspark.chronoanalyser.getAppName

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
