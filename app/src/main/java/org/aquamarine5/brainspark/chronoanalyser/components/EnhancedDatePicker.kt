package org.aquamarine5.brainspark.chronoanalyser.components

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import org.aquamarine5.brainspark.chronoanalyser.DateConverter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDatePicker(
    allowedTimeRange: LongRange,
    currentDateState: MutableState<LocalDate>
) {
    var currentDate by currentDateState
    val logTag = "EnhancedDatePicker"
    val selectedDateState = rememberDatePickerState(
        initialSelectedDateMillis = DateConverter.toTimestampUTC(currentDate),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return allowedTimeRange.contains(
                    Instant.ofEpochMilli(utcTimeMillis).atZone(
                        ZoneId.systemDefault()
                    ).toInstant().toEpochMilli()
                )
            }
        }
    )
    selectedDateState.selectedDateMillis = DateConverter.toTimestampUTC(currentDate)
    var isShowDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = {
            selectedDateState.selectedDateMillis?.let {
                selectedDateState.selectedDateMillis = it - TimeUnit.DAYS.toMillis(1)
                currentDate = currentDate.minusDays(1)
            }
        }) {
            Text("-")
        }
        Button(onClick = {
            isShowDialog = true
        }) {
            Text(currentDate.toString())
        }

        Button(onClick = {
            selectedDateState.selectedDateMillis?.let {
                selectedDateState.selectedDateMillis = it + TimeUnit.DAYS.toMillis(1)
                currentDate = currentDate.plusDays(1)
            }
        }) {
            Text("+")
        }
    }
    if (isShowDialog) {
        DatePickerDialog(
            onDismissRequest = { isShowDialog = false },
            confirmButton = {
                Button(onClick = {
                    selectedDateState.selectedDateMillis?.let {
                        currentDate = DateConverter.toLocalDate(it, ZoneId.of("UTC"))
                    }
                    isShowDialog = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = selectedDateState
            )
        }
    }
}
