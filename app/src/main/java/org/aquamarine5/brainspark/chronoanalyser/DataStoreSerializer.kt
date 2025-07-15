package org.aquamarine5.brainspark.chronoanalyser

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import org.aquamarine5.brainspark.chronoanalyser.datastore.ChronoAnalyserDatastore
import java.io.InputStream
import java.io.OutputStream

object DataStoreSerializer : Serializer<ChronoAnalyserDatastore> {

    val Context.datastore: DataStore<ChronoAnalyserDatastore> by dataStore(
        "chrono.pb",
        DataStoreSerializer
    )

    override val defaultValue: ChronoAnalyserDatastore
        get() = ChronoAnalyserDatastore.newBuilder()
            .setAllUsageTime(0L)
            .setLastUpdateDate(0)
            .setLastUpdateTime(0L)
            .build()

    override suspend fun readFrom(input: InputStream): ChronoAnalyserDatastore {
        return try {
            ChronoAnalyserDatastore.parseFrom(input)
        } catch (e: Exception) {
            Log.w("DataStoreSerializer", "Failed to read from input stream", e)
            // If the input stream is empty or cannot be parsed, return the default value
            defaultValue
        }
    }

    override suspend fun writeTo(t: ChronoAnalyserDatastore, output: OutputStream) {
        t.writeTo(output)
    }
}
