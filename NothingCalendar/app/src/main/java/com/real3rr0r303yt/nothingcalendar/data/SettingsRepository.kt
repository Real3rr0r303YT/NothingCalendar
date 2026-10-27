package com.real3rr0r303yt.nothingcalendar.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val weekStart: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek,
)

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setHapticsEnabled(enabled: Boolean) = save(_settings.value.copy(hapticsEnabled = enabled))

    fun setWeekStart(day: DayOfWeek) = save(_settings.value.copy(weekStart = day))

    private fun load(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, defaults.hapticsEnabled),
            weekStart = prefs.getInt(KEY_WEEK_START, 0)
                .takeIf { it in 1..7 }
                ?.let(DayOfWeek::of)
                ?: defaults.weekStart,
        )
    }

    private fun save(settings: AppSettings) {
        _settings.value = settings
        prefs.edit()
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putInt(KEY_WEEK_START, settings.weekStart.value)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "nothing_calendar_settings"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_WEEK_START = "week_start"
    }
}
