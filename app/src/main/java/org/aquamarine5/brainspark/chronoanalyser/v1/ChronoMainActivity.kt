package org.aquamarine5.brainspark.chronoanalyser.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.aquamarine5.brainspark.chronoanalyser.DrawMainContent
import org.aquamarine5.brainspark.chronoanalyser.ui.theme.ChronoAnalyserTheme

class ChronoMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronoAnalyserTheme {
                DrawMainContent()
            }
        }
    }
}