package com.real3rr0r303yt.nothingcalendar.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.real3rr0r303yt.nothingcalendar.data.AppSettings
import com.real3rr0r303yt.nothingcalendar.data.CalendarEvent
import com.real3rr0r303yt.nothingcalendar.data.EventRepository
import com.real3rr0r303yt.nothingcalendar.data.SettingsRepository
import com.real3rr0r303yt.nothingcalendar.util.plusHourClamped
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

enum class Screen { Month, Day }

data class EditorRequest(val event: CalendarEvent, val isNew: Boolean)

class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val eventRepository = EventRepository(app)
    private val settingsRepository = SettingsRepository(app)

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    val eventsByDate: StateFlow<Map<LocalDate, List<CalendarEvent>>> = eventRepository.events
        .map(::groupByDate)
        .stateIn(viewModelScope, SharingStarted.Eagerly, groupByDate(eventRepository.events.value))

    var screen by mutableStateOf(Screen.Month)
        private set

    var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
        private set

    var editor by mutableStateOf<EditorRequest?>(null)
        private set

    var settingsOpen by mutableStateOf(false)
        private set

    fun selectDate(date: LocalDate) {
        selectedDate = date
    }

    fun openDay(date: LocalDate) {
        selectedDate = date
        screen = Screen.Day
    }

    fun closeDay() {
        screen = Screen.Month
    }

    /** Opens the editor for a new event, optionally pre-filled with a start [hour]. */
    fun newEvent(date: LocalDate, hour: Int? = null) {
        val start = LocalTime.of(hour ?: DEFAULT_START_HOUR, 0)
        editor = EditorRequest(
            event = CalendarEvent(title = "", date = date, start = start, end = start.plusHourClamped()),
            isNew = true,
        )
    }

    fun editEvent(event: CalendarEvent) {
        editor = EditorRequest(event, isNew = false)
    }

    fun dismissEditor() {
        editor = null
    }

    fun saveEvent(event: CalendarEvent) {
        eventRepository.upsert(event)
        selectedDate = event.date
        editor = null
    }

    fun deleteEvent(event: CalendarEvent) {
        eventRepository.delete(event.id)
        editor = null
    }

    fun showSettings(show: Boolean) {
        settingsOpen = show
    }

    fun setHapticsEnabled(enabled: Boolean) = settingsRepository.setHapticsEnabled(enabled)

    fun setWeekStart(day: DayOfWeek) = settingsRepository.setWeekStart(day)

    private fun groupByDate(events: List<CalendarEvent>): Map<LocalDate, List<CalendarEvent>> =
        events.groupBy { it.date }.mapValues { (_, list) -> list.sortedWith(CalendarEvent.ORDER) }

    private companion object {
        const val DEFAULT_START_HOUR = 9
    }
}
