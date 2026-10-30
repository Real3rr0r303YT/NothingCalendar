package com.real3rr0r303yt.nothingcalendar.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.real3rr0r303yt.nothingcalendar.data.CalendarEvent
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** Time formatter honouring the system 12/24h setting. */
@Composable
fun rememberTimeFormatter(): DateTimeFormatter {
    val context = LocalContext.current
    val locale = currentLocale()
    val is24h = DateFormat.is24HourFormat(context)
    return remember(locale, is24h) {
        DateTimeFormatter.ofPattern(if (is24h) "HH:mm" else "h:mm a", locale)
    }
}

/** Today's date, refreshed every minute so the UI rolls over at midnight. */
@Composable
fun rememberToday(): LocalDate {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            today = LocalDate.now()
        }
    }
    return today
}

/** The current time, refreshed every 30 seconds. */
@Composable
fun rememberNow(): LocalTime {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalTime.now()
        }
    }
    return now
}

fun CalendarEvent.timeRange(formatter: DateTimeFormatter): String {
    val s = start ?: return ""
    val e = end ?: return s.format(formatter)
    return "${s.format(formatter)}–${e.format(formatter)}"
}

/** One hour later, but never past the end of the day. */
fun LocalTime.plusHourClamped(): LocalTime =
    if (hour >= 23) LocalTime.of(23, 59) else plusHours(1)

fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.utcMillisToLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
