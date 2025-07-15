package org.aquamarine5.brainspark.chronoanalyser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.aquamarine5.brainspark.chronoanalyser.ui.theme.ChronoAnalyserTheme
import org.aquamarine5.brainspark.chronoanalyser.pages.MainPage

class ChronoMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronoAnalyserTheme {
                MainPage()
            }
        }
    }
}