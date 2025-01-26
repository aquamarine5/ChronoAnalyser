package org.aquamarine5.brainspark.chronoanalyser

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

typealias FlowResult<T> = Flow<Pair<Float, T?>>

object FlowResultUtil {
    fun <T> resolve(result: T): Pair<Float, T> {
        return Pair(1.0f, result)
    }

    fun <T> progress(v: Float): Pair<Float, T?> {
        return Pair(v, null)
    }
}