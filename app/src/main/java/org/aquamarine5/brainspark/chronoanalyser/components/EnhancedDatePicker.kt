package org.aquamarine5.brainspark.chronoanalyser.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.aquamarine5.brainspark.chronoanalyser.data.DateSQLConverter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDatePicker(
    allowedTimeRange: LongRange,
    onDateSelected: (Int) -> Unit
) {
    val logTag = "EnhancedDatePicker"
    val selectedDateState = rememberDatePickerState(
        initialSelectedDateMillis = allowedTimeRange.last,
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
    var isShowDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = {
            selectedDateState.selectedDateMillis?.let {
                selectedDateState.selectedDateMillis = it - TimeUnit.DAYS.toMillis(1)
                onDateSelected(DateSQLConverter.toDateNumber(it - TimeUnit.DAYS.toMillis(1)))
            }
        }) {
            Text("-")
        }
        Button(onClick = {
            isShowDialog = true
        }) {
            Text(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(selectedDateState.selectedDateMillis)
            )
        }

        Button(onClick = {
            selectedDateState.selectedDateMillis?.let {
                selectedDateState.selectedDateMillis = it + TimeUnit.DAYS.toMillis(1)
                onDateSelected(DateSQLConverter.toDateNumber(it + TimeUnit.DAYS.toMillis(1)))
            }
        }) {
            Text("+")
        }
    }
    if(isShowDialog){
        DatePickerDialog(
            onDismissRequest = { isShowDialog = false },
            confirmButton = {
                Button(onClick = {
                    selectedDateState.selectedDateMillis?.let {
                        onDateSelected(DateSQLConverter.toDateNumber(it))
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
