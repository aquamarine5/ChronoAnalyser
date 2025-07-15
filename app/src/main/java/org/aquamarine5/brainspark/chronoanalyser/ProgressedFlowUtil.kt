package org.aquamarine5.brainspark.chronoanalyser

import kotlinx.coroutines.flow.Flow

typealias ProgressedFlow = Flow<Pair<Float, Boolean>>

typealias ProgressedFlowResult<T> = Flow<Triple<Float,Boolean,T?>>

object ProgressedFlowUtil {
    fun resolve(): Pair<Float, Boolean> = 1f to true
    fun progress(progress: Float): Pair<Float, Boolean> = progress to false

    fun <T> resolveResult(result: T): Triple<Float, Boolean, T> =
        Triple(1f, true, result)
    fun <T> progressResult(progress: Float): Triple<Float, Boolean, T?> =
        Triple(progress,false,null)

    suspend fun Pair<Float, Boolean>.whenProgress(callback:suspend (Float)->Unit):Pair<Float, Boolean>{
        if(this.second){
            return this
        }
        callback(this.first)
        return this
    }

    suspend fun Pair<Float, Boolean>.whenResolve(callback:suspend ()->Unit):Pair<Float, Boolean>{
        if(this.second)
            callback()
        return this
    }

    suspend fun <T> Triple<Float,Boolean,T?>.whenProgress(callback:suspend (Float)->Unit):Triple<Float,Boolean,T?>{
        if(this.second)
            return this
        callback(this.first)
        return this
    }

    suspend fun <T> Triple<Float,Boolean,T?>.whenResolve(callback:suspend (T)->Unit):Triple<Float,Boolean,T?>{
        if(this.second)
            callback(this.third!!)
        return this
    }
}