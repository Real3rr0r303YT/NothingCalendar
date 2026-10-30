@file:OptIn(ExperimentalMaterial3Api::class)

package com.real3rr0r303yt.nothingcalendar.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingcalendar.R
import com.real3rr0r303yt.nothingcalendar.data.CalendarEvent
import com.real3rr0r303yt.nothingcalendar.ui.EditorRequest
import com.real3rr0r303yt.nothingcalendar.ui.components.PickerField
import com.real3rr0r303yt.nothingcalendar.ui.components.PillButton
import com.real3rr0r303yt.nothingcalendar.ui.components.PillStyle
import com.real3rr0r303yt.nothingcalendar.ui.components.SettingSwitchRow
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingColors
import com.real3rr0r303yt.nothingcalendar.util.LocalHaptics
import com.real3rr0r303yt.nothingcalendar.util.currentLocale
import com.real3rr0r303yt.nothingcalendar.util.plusHourClamped
import com.real3rr0r303yt.nothingcalendar.util.rememberTimeFormatter
import com.real3rr0r303yt.nothingcalendar.util.toUtcMillis
import com.real3rr0r303yt.nothingcalendar.util.utcMillisToLocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class Picker { Date, Start, End }

@Composable
fun EventEditorSheet(
    request: EditorRequest,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: (CalendarEvent) -> Unit,
) {
    val haptics = LocalHaptics.current
    val locale = currentLocale()
    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val original = request.event
    var title by remember(request) { mutableStateOf(original.title) }
    var note by remember(request) { mutableStateOf(original.note) }
    var date by remember(request) { mutableStateOf(original.date) }
    var allDay by remember(request) { mutableStateOf(original.isAllDay) }
    var start by remember(request) { mutableStateOf(original.start ?: LocalTime.of(9, 0)) }
    var end by remember(request) { mutableStateOf(original.end ?: (original.start ?: LocalTime.of(9, 0)).plusHourClamped()) }
    var errorRes by remember(request) { mutableStateOf<Int?>(null) }
    var confirmDelete by remember(request) { mutableStateOf(false) }
    var picker by remember { mutableStateOf<Picker?>(null) }

    fun save() {
        when {
            title.isBlank() -> {
                errorRes = R.string.title_required
                haptics.reject()
            }
            !allDay && !end.isAfter(start) -> {
                errorRes = R.string.end_before_start
                haptics.reject()
            }
            else -> {
                haptics.confirm()
                onSave(
                    original.copy(
                        title = title.trim(),
                        note = note.trim(),
                        date = date,
                        start = if (allDay) null else start,
                        end = if (allDay) null else end,
                    ),
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NothingColors.Grey900,
        contentColor = NothingColors.White,
        scrimColor = Color.Black.copy(alpha = 0.7f),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(if (request.isNew) R.string.new_event else R.string.edit_event).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NothingColors.Red,
            )

            NothingTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorRes = null
                },
                placeholder = stringResource(R.string.title_hint),
                singleLine = true,
                isError = errorRes == R.string.title_required,
            )

            PickerField(
                label = stringResource(R.string.date),
                value = date.format(dateFormatter),
                onClick = {
                    haptics.click()
                    picker = Picker.Date
                },
                modifier = Modifier.fillMaxWidth(),
            )

            SettingSwitchRow(
                title = stringResource(R.string.all_day),
                checked = allDay,
                onCheckedChange = {
                    haptics.tick()
                    allDay = it
                    errorRes = null
                },
            )

            if (!allDay) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField(
                        label = stringResource(R.string.start),
                        value = start.format(timeFormatter),
                        onClick = {
                            haptics.click()
                            picker = Picker.Start
                        },
                        modifier = Modifier.weight(1f),
                    )
                    PickerField(
                        label = stringResource(R.string.end),
                        value = end.format(timeFormatter),
                        onClick = {
                            haptics.click()
                            picker = Picker.End
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            NothingTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = stringResource(R.string.note_hint),
                singleLine = false,
                minLines = 3,
            )

            errorRes?.let {
                Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = NothingColors.Red)
            }

            Row(Modifier.fillMaxWidth()) {
                if (!request.isNew) {
                    PillButton(
                        text = stringResource(if (confirmDelete) R.string.delete_confirm else R.string.delete),
                        style = PillStyle.Danger,
                        onClick = {
                            if (confirmDelete) {
                                haptics.reject()
                                onDelete(original)
                            } else {
                                haptics.longPress()
                                confirmDelete = true
                            }
                        },
                    )
                }
                Spacer(Modifier.weight(1f))
                PillButton(stringResource(R.string.save), onClick = { save() }, style = PillStyle.Filled)
            }
        }
    }

    when (picker) {
        Picker.Date -> {
            val state = rememberDatePickerState(initialSelectedDateMillis = date.toUtcMillis())
            DatePickerDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        haptics.click()
                        state.selectedDateMillis?.let { date = it.utcMillisToLocalDate() }
                        picker = null
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { picker = null }) { Text(stringResource(R.string.cancel)) }
                },
            ) {
                DatePicker(state = state)
            }
        }
        Picker.Start -> TimePickerDialog(
            initial = start,
            onDismiss = { picker = null },
            onConfirm = {
                haptics.click()
                start = it
                if (!end.isAfter(it)) end = it.plusHourClamped()
                errorRes = null
                picker = null
            },
        )
        Picker.End -> TimePickerDialog(
            initial = end,
            onDismiss = { picker = null },
            onConfirm = {
                haptics.click()
                end = it
                errorRes = null
                picker = null
            },
        )
        null -> Unit
    }
}

@Composable
private fun TimePickerDialog(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NothingColors.Grey900,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        text = { TimePicker(state = state) },
    )
}

@Composable
private fun NothingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minLines: Int = 1,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = NothingColors.Grey500) },
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NothingColors.White,
            unfocusedBorderColor = NothingColors.Grey700,
            errorBorderColor = NothingColors.Red,
            cursorColor = NothingColors.Red,
            focusedTextColor = NothingColors.White,
            unfocusedTextColor = NothingColors.White,
        ),
    )
}
