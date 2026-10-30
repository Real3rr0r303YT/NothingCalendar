package com.real3rr0r303yt.nothingcalendar.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/** Stores events locally on the device as JSON in SharedPreferences. */
class EventRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _events = MutableStateFlow(load())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    fun upsert(event: CalendarEvent) {
        _events.update { list -> list.filterNot { it.id == event.id } + event }
        persist()
    }

    fun delete(id: String) {
        _events.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    private fun load(): List<CalendarEvent> {
        val raw = prefs.getString(KEY_EVENTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { i -> array.getJSONObject(i).toEvent() }
        }.getOrDefault(emptyList())
    }

    private fun persist() {
        val array = JSONArray()
        _events.value.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_EVENTS, array.toString()).apply()
    }

    private fun CalendarEvent.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("date", date.toString())
        start?.let { put("start", it.toString()) }
        end?.let { put("end", it.toString()) }
        put("note", note)
    }

    private fun JSONObject.toEvent() = CalendarEvent(
        id = getString("id"),
        title = getString("title"),
        date = LocalDate.parse(getString("date")),
        start = optString("start").takeIf { it.isNotEmpty() }?.let(LocalTime::parse),
        end = optString("end").takeIf { it.isNotEmpty() }?.let(LocalTime::parse),
        note = optString("note"),
    )

    private companion object {
        const val PREFS_NAME = "nothing_calendar_events"
        const val KEY_EVENTS = "events"
    }
}
