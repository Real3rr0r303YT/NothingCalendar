package com.real3rr0r303yt.nothingcalendar.data

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/** A single-day calendar entry. An event without [start] is an all-day event. */
data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: LocalDate,
    val start: LocalTime? = null,
    val end: LocalTime? = null,
    val note: String = "",
) {
    val isAllDay: Boolean get() = start == null

    /** Minutes since midnight at which the event starts (0 for all-day events). */
    val startMinute: Int get() = start?.let { it.toSecondOfDay() / 60 } ?: 0

    /** Minutes since midnight at which the event ends; always after [startMinute]. */
    val endMinute: Int
        get() {
            val s = startMinute
            val e = end?.let { it.toSecondOfDay() / 60 } ?: (s + 60)
            return (if (e <= s) s + 60 else e).coerceAtMost(MINUTES_PER_DAY)
        }

    companion object {
        const val MINUTES_PER_DAY = 24 * 60

        /** All-day events first, then by start time, then alphabetically. */
        val ORDER: Comparator<CalendarEvent> =
            compareBy<CalendarEvent>({ !it.isAllDay }, { it.startMinute }, { it.title.lowercase() })
    }
}
