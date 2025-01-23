package org.aquamarine5.brainspark.chronoanalyser

import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val scope= rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(progressFlow) {
        scope.launch {
            progressFlow.collect { (value, result) ->
                progress = value
                if (result != null) {
                    onFinished(result)
                }
            }
        }
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier,
        color,
        trackColor,
        strokeCap,
        gapSize,
        drawStopIndicator = {}
    )
}