package org.aquamarine5.brainspark.chronoanalyser.component

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aquamarine5.brainspark.chronoanalyser.ProgressedFlow
import org.aquamarine5.brainspark.chronoanalyser.ProgressedFlowResult

@Composable
fun FlowLinearProgressIndicator(
    progressFlow: ProgressedFlow,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    gapSize: Dp = (-1).dp,
    onFinished: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(progress)
    LaunchedEffect(progressFlow) {
        scope.launch {
            withContext(Dispatchers.Main) {
                progressFlow.collect { (value, result) ->
                    progress = value
                    if (result) {
                        onFinished()
                    }
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

@Composable
fun <T> FlowLinearProgressIndicator(
    progressFlow: ProgressedFlowResult<T>,
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
            progressFlow.collect { (value, isFinished, result) ->
                progress = value
                if (isFinished) {
                    onFinished(result!!)
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