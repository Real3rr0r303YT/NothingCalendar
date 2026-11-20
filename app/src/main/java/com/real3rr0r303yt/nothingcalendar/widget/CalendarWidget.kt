// NothingCalendar – Copyright (C) 2026 Real3rr0r303YT – Made by 3rr0r303YT on TikTok.
// Licensed under the GNU General Public License v3 (see LICENSE).

package com.real3rr0r303yt.nothingcalendar.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.real3rr0r303yt.nothingcalendar.MainActivity
import com.real3rr0r303yt.nothingcalendar.R
import com.real3rr0r303yt.nothingcalendar.data.CalendarEvent
import com.real3rr0r303yt.nothingcalendar.data.EventRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Today in dot-matrix (weekday + day) and the next three events. Redrawn whenever an event is
 * saved or deleted, and once after midnight by a non-wakeup alarm.
 */
class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        render(context, manager, ids)
        scheduleMidnight(context)
    }

    override fun onEnabled(context: Context) = scheduleMidnight(context)

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(midnightIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_MIDNIGHT, Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_LOCALE_CHANGED, Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> updateAll(context)
        }
    }

    companion object {
        private const val ACTION_MIDNIGHT = "com.real3rr0r303yt.nothingcalendar.widget.MIDNIGHT"
        private const val ROWS = 3

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CalendarWidget::class.java))
            if (ids.isEmpty()) return
            render(context, manager, ids)
            scheduleMidnight(context)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val density = context.resources.displayMetrics.density
            val locale = context.resources.configuration.locales[0]
            val today = LocalDate.now()
            val now = LocalTime.now()
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)

            val day = DateTimeFormatter.ofPattern("EEE d", locale).format(today).replace(".", "").uppercase(locale)
            views.setImageViewBitmap(
                R.id.widget_day,
                DotRender.text(day, 5f * density) { i, _ -> if (i < day.indexOf(' ')) DotRender.RED else DotRender.WHITE },
            )
            views.setContentDescription(R.id.widget_day, day)
            views.setTextViewText(
                R.id.widget_month,
                DateTimeFormatter.ofPattern("MMMM yyyy", locale).format(today).uppercase(locale),
            )

            val upcoming = EventRepository(context).events.value
                .filter { e ->
                    e.date.isAfter(today) ||
                        (e.date == today && (e.isAllDay || e.endMinute > now.toSecondOfDay() / 60))
                }
                .sortedWith(compareBy<CalendarEvent> { it.date }.then(CalendarEvent.ORDER))
                .take(ROWS)

            val rowIds = intArrayOf(R.id.widget_row1, R.id.widget_row2, R.id.widget_row3)
            val whenIds = intArrayOf(R.id.widget_when1, R.id.widget_when2, R.id.widget_when3)
            val titleIds = intArrayOf(R.id.widget_title1, R.id.widget_title2, R.id.widget_title3)
            val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
            for (i in 0 until ROWS) {
                val e = upcoming.getOrNull(i)
                if (e == null) {
                    views.setViewVisibility(rowIds[i], View.GONE)
                    continue
                }
                views.setViewVisibility(rowIds[i], View.VISIBLE)
                val time = e.start?.let { DateTimeFormatter.ofPattern(timePattern, locale).format(it) }
                    ?: context.getString(R.string.widget_all_day)
                val label = if (e.date == today) time else
                    DateTimeFormatter.ofPattern("EEE ", locale).format(e.date).replace(".", "") + time
                views.setTextViewText(whenIds[i], label.uppercase(locale))
                views.setTextViewText(titleIds[i], e.title)
            }
            views.setViewVisibility(R.id.widget_empty, if (upcoming.isEmpty()) View.VISIBLE else View.GONE)

            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_root, open)
            manager.updateAppWidget(ids, views)
        }

        private fun midnightIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, CalendarWidget::class.java).setAction(ACTION_MIDNIGHT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        private fun scheduleMidnight(context: Context) {
            val at = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0, 5))
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // RTC (not RTC_WAKEUP): redraw when the phone is next used after midnight.
            context.getSystemService(AlarmManager::class.java)?.set(AlarmManager.RTC, at, midnightIntent(context))
        }
    }
}
