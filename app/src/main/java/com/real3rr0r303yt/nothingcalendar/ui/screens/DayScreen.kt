package com.real3rr0r303yt.nothingcalendar.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingcalendar.R
import com.real3rr0r303yt.nothingcalendar.data.CalendarEvent
import com.real3rr0r303yt.nothingcalendar.ui.CalendarViewModel
import com.real3rr0r303yt.nothingcalendar.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingcalendar.ui.components.NothingFab
import com.real3rr0r303yt.nothingcalendar.ui.components.PillButton
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingColors
import com.real3rr0r303yt.nothingcalendar.util.LocalHaptics
import com.real3rr0r303yt.nothingcalendar.util.currentLocale
import com.real3rr0r303yt.nothingcalendar.util.rememberNow
import com.real3rr0r303yt.nothingcalendar.util.rememberTimeFormatter
import com.real3rr0r303yt.nothingcalendar.util.rememberToday
import com.real3rr0r303yt.nothingcalendar.util.timeRange
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

private const val DAY_PAGES = 20001
private const val DAY_CENTER = DAY_PAGES / 2
private val HourHeight = 64.dp
private val LabelWidth = 64.dp

@Composable
fun DayScreen(vm: CalendarViewModel) {
    val haptics = LocalHaptics.current
    val eventsByDate by vm.eventsByDate.collectAsState()
    val today = rememberToday()
    val scope = rememberCoroutineScope()

    val entryDate = remember { vm.selectedDate }
    fun dateFor(page: Int): LocalDate = entryDate.plusDays((page - DAY_CENTER).toLong())
    fun pageFor(date: LocalDate): Int = DAY_CENTER + ChronoUnit.DAYS.between(entryDate, date).toInt()

    val pagerState = rememberPagerState(initialPage = DAY_CENTER) { DAY_PAGES }
    val currentDate = dateFor(pagerState.currentPage)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.drop(1).collect { page ->
            haptics.tick()
            vm.selectDate(dateFor(page))
        }
    }

    BackHandler {
        haptics.click()
        vm.closeDay()
    }

    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        Column(Modifier.fillMaxSize()) {
            DayHeader(
                date = currentDate,
                isToday = currentDate == today,
                onBack = {
                    haptics.click()
                    vm.closeDay()
                },
                onToday = {
                    haptics.click()
                    scope.launch { pagerState.animateScrollToPage(pageFor(today)) }
                },
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                val date = dateFor(page)
                DayTimeline(
                    date = date,
                    isToday = date == today,
                    events = eventsByDate[date].orEmpty(),
                    onCreateAt = { hour ->
                        haptics.longPress()
                        vm.newEvent(date, hour)
                    },
                    onEdit = { event ->
                        haptics.click()
                        vm.editEvent(event)
                    },
                )
            }
        }

        NothingFab(
            onClick = {
                haptics.click()
                vm.newEvent(currentDate)
            },
            contentDescription = stringResource(R.string.add_event),
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
}

@Composable
private fun DayHeader(date: LocalDate, isToday: Boolean, onBack: () -> Unit, onToday: () -> Unit) {
    val locale = currentLocale()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = NothingColors.White,
            )
        }
        DotMatrixText(
            text = date.dayOfMonth.toString().padStart(2, '0'),
            dotPitch = 8.dp,
            color = if (isToday) NothingColors.Red else NothingColors.White,
            offDotColor = NothingColors.White.copy(alpha = 0.06f),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).uppercase(locale),
                style = MaterialTheme.typography.labelLarge,
                color = NothingColors.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${date.month.getDisplayName(TextStyle.FULL, locale).uppercase(locale)} ${date.year}",
                style = MaterialTheme.typography.labelMedium,
                color = NothingColors.Grey300,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!isToday) {
            PillButton(stringResource(R.string.today), onClick = onToday)
        }
    }
}

@Composable
private fun DayTimeline(
    date: LocalDate,
    isToday: Boolean,
    events: List<CalendarEvent>,
    onCreateAt: (Int) -> Unit,
    onEdit: (CalendarEvent) -> Unit,
) {
    val allDay = events.filter { it.isAllDay }
    val timed = events.filterNot { it.isAllDay }
    val scroll = rememberScrollState()
    val hourPx = with(LocalDensity.current) { HourHeight.toPx() }
    val timeFormatter = rememberTimeFormatter()

    LaunchedEffect(date) {
        val firstHour = if (isToday) LocalTime.now().hour - 1 else timed.minOfOrNull { it.startMinute / 60 } ?: 8
        scroll.scrollTo((firstHour.coerceIn(0, 23) * hourPx).toInt())
    }

    Column(Modifier.fillMaxSize()) {
        if (allDay.isNotEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                allDay.forEach { event -> EventCard(event, onClick = { onEdit(event) }) }
            }
        }

        Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(HourHeight * 24)
                    .drawBehind {
                        val start = LabelWidth.toPx()
                        val step = 6.dp.toPx()
                        val r = 1.dp.toPx()
                        for (h in 0 until 24) {
                            val y = h * hourPx
                            var x = start
                            while (x < size.width) {
                                drawCircle(NothingColors.Grey700, r, Offset(x, y))
                                x += step
                            }
                        }
                    }
                    .pointerInput(date) {
                        detectTapGestures { offset ->
                            onCreateAt((offset.y / hourPx).toInt().coerceIn(0, 23))
                        }
                    },
            ) {
                for (h in 0 until 24) {
                    Text(
                        text = LocalTime.of(h, 0).format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingColors.Grey500,
                        modifier = Modifier
                            .offset(y = (HourHeight * h - 6.dp).coerceAtLeast(0.dp))
                            .padding(start = 16.dp),
                    )
                }

                BoxWithConstraints(Modifier.fillMaxSize().padding(start = LabelWidth + 4.dp, end = 12.dp)) {
                    val placed = remember(timed) { layoutEvents(timed) }
                    placed.forEach { p ->
                        val laneWidth = maxWidth / p.lanes
                        val top = HourHeight * (p.event.startMinute / 60f)
                        val height = HourHeight * ((p.event.endMinute - p.event.startMinute) / 60f)
                        TimelineEvent(
                            event = p.event,
                            label = p.event.timeRange(timeFormatter),
                            onClick = { onEdit(p.event) },
                            modifier = Modifier
                                .offset(x = laneWidth * p.lane, y = top)
                                .width(laneWidth)
                                .height(maxOf(height, 28.dp))
                                .padding(2.dp),
                        )
                    }
                }

                if (isToday) {
                    val now = rememberNow()
                    val y = HourHeight * (now.toSecondOfDay() / 3600f)
                    Row(
                        Modifier
                            .padding(start = LabelWidth - 5.dp)
                            .offset(y = y - 5.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).background(NothingColors.Red, CircleShape))
                        Box(Modifier.weight(1f).height(2.dp).background(NothingColors.Red))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEvent(event: CalendarEvent, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
            .clip(shape)
            .background(NothingColors.Grey800)
            .border(1.dp, NothingColors.Grey700, shape)
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(NothingColors.Red))
        Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = NothingColors.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = NothingColors.Grey300, maxLines = 1)
        }
    }
}

private data class PlacedEvent(val event: CalendarEvent, val lane: Int, val lanes: Int)

/** Puts overlapping events side by side, using as few lanes as possible per cluster. */
private fun layoutEvents(events: List<CalendarEvent>): List<PlacedEvent> {
    val result = mutableListOf<PlacedEvent>()
    val cluster = mutableListOf<Pair<CalendarEvent, Int>>()
    val laneEnds = mutableListOf<Int>()
    var clusterEnd = -1

    fun flush() {
        cluster.forEach { (event, lane) -> result += PlacedEvent(event, lane, laneEnds.size) }
        cluster.clear()
        laneEnds.clear()
        clusterEnd = -1
    }

    for (event in events.sortedBy { it.startMinute }) {
        if (cluster.isNotEmpty() && event.startMinute >= clusterEnd) flush()
        var lane = laneEnds.indexOfFirst { it <= event.startMinute }
        if (lane == -1) {
            lane = laneEnds.size
            laneEnds += event.endMinute
        } else {
            laneEnds[lane] = event.endMinute
        }
        cluster += event to lane
        clusterEnd = maxOf(clusterEnd, event.endMinute)
    }
    flush()
    return result
}
