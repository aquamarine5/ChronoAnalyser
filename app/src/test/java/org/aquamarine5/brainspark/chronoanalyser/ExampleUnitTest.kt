package org.aquamarine5.brainspark.chronoanalyser

import org.aquamarine5.brainspark.chronoanalyser.data.DateSQLConverter
import org.junit.Test

import org.junit.Assert.*
import java.time.Instant

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(DateSQLConverter.toDateNumber(Instant.now().toEpochMilli()),20250126)
    }
}