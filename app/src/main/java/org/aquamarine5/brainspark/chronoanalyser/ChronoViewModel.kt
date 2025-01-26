package org.aquamarine5.brainspark.chronoanalyser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ChronoViewModel:ViewModel() {
    var analysisType by mutableStateOf(true)
}