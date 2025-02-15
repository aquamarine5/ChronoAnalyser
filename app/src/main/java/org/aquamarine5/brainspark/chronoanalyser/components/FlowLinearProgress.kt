package org.aquamarine5.brainspark.chronoanalyser.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.aquamarine5.brainspark.chronoanalyser.FlowResult

@Composable
fun <T> FlowLinearProgressIndicator(
    progressFlow: FlowResult<T>,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    gapSize: Dp = (-1).dp,
    onFinished: (result: T) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(progress)
    LaunchedEffect(progressFlow) {
        scope.launch {
            progressFlow.collect { (value, result) ->
                progress = value
                result?.let {
                    onFinished(result)
                }
            }
        }
    }
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier,
        color,
        trackColor,
        strokeCap,
        gapSize,
        drawStopIndicator = {}
    )
}