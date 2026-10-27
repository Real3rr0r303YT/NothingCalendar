package com.real3rr0r303yt.nothingcalendar.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingcalendar.R
import com.real3rr0r303yt.nothingcalendar.data.CalendarEvent
import com.real3rr0r303yt.nothingcalendar.ui.CalendarViewModel
import com.real3rr0r303yt.nothingcalendar.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingcalendar.ui.components.DottedDivider
import com.real3rr0r303yt.nothingcalendar.ui.components.EventDots
import com.real3rr0r303yt.nothingcalendar.ui.components.NothingFab
import com.real3rr0r303yt.nothingcalendar.ui.components.PillButton
import com.real3rr0r303yt.nothingcalendar.ui.components.dotGridBackground
import com.real3rr0r303yt.nothingcalendar.ui.theme.MonoFamily
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingColors
import com.real3rr0r303yt.nothingcalendar.util.LocalHaptics
import com.real3rr0r303yt.nothingcalendar.util.currentLocale
import com.real3rr0r303yt.nothingcalendar.util.rememberTimeFormatter
import com.real3rr0r303yt.nothingcalendar.util.rememberToday
import com.real3rr0r303yt.nothingcalendar.util.timeRange
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

private const val MONTH_PAGES = 2401 // ±100 years
private const val MONTH_CENTER = MONTH_PAGES / 2
private val GridHeight = (6 * 56).dp // 6 weeks × 56dp per row

@Composable
fun MonthScreen(vm: CalendarViewModel) {
    val haptics = LocalHaptics.current
    val settings by vm.settings.collectAsState()
    val eventsByDate by vm.eventsByDate.collectAsState()
    val today = rememberToday()
    val scope = rememberCoroutineScope()

    val baseMonth = remember { YearMonth.now() }
    fun monthFor(page: Int): YearMonth = baseMonth.plusMonths((page - MONTH_CENTER).toLong())
    fun pageFor(month: YearMonth): Int =
        MONTH_CENTER + ChronoUnit.MONTHS.between(baseMonth.atDay(1), month.atDay(1)).toInt()

    val pagerState = rememberPagerState(initialPage = pageFor(YearMonth.from(vm.selectedDate))) { MONTH_PAGES }
    val visibleMonth = monthFor(pagerState.currentPage)

    // Tick on every month change while swiping.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.drop(1).collect { haptics.tick() }
    }
    // Keep the selection inside the visible month once the swipe settles.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.drop(1).collect { page ->
            val month = monthFor(page)
            if (YearMonth.from(vm.selectedDate) != month) {
                vm.selectDate(if (month == YearMonth.from(today)) today else month.atDay(1))
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .dotGridBackground()
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Column(Modifier.fillMaxSize()) {
            MonthHeader(
                month = visibleMonth,
                showToday = visibleMonth != YearMonth.from(today) || vm.selectedDate != today,
                onToday = {
                    haptics.click()
                    vm.selectDate(today)
                    scope.launch { pagerState.animateScrollToPage(pageFor(YearMonth.from(today))) }
                },
                onSettings = {
                    haptics.click()
                    vm.showSettings(true)
                },
            )
            WeekdayRow(settings.weekStart)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(GridHeight),
                beyondViewportPageCount = 1,
            ) { page ->
                MonthGrid(
                    month = monthFor(page),
                    weekStart = settings.weekStart,
                    today = today,
                    selected = vm.selectedDate,
                    eventsByDate = eventsByDate,
                    onClick = { date ->
                        if (date == vm.selectedDate) {
                            haptics.click()
                            vm.openDay(date)
                        } else {
                            haptics.tick()
                            vm.selectDate(date)
                            if (YearMonth.from(date) != monthFor(page)) {
                                scope.launch { pagerState.animateScrollToPage(pageFor(YearMonth.from(date))) }
                            }
                        }
                    },
                    onLongClick = { date ->
                        haptics.longPress()
                        vm.selectDate(date)
                        vm.newEvent(date)
                    },
                )
            }
            DottedDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            Agenda(
                date = vm.selectedDate,
                events = eventsByDate[vm.selectedDate].orEmpty(),
                onOpenDay = {
                    haptics.click()
                    vm.openDay(vm.selectedDate)
                },
                onEdit = { event ->
                    haptics.click()
                    vm.editEvent(event)
                },
                modifier = Modifier.weight(1f),
            )
        }

        NothingFab(
            onClick = {
                haptics.click()
                vm.newEvent(vm.selectedDate)
            },
            contentDescription = stringResource(R.string.add_event),
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    showToday: Boolean,
    onToday: () -> Unit,
    onSettings: () -> Unit,
) {
    val locale = currentLocale()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            DotMatrixText(
                text = month.month.getDisplayName(TextStyle.FULL, locale).uppercase(locale),
                dotPitch = 7.dp,
                offDotColor = NothingColors.White.copy(alpha = 0.06f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = month.year.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = NothingColors.Grey300,
            )
        }
        Spacer(Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showToday) {
                PillButton(stringResource(R.string.today), onClick = onToday)
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = NothingColors.Grey300,
                )
            }
        }
    }
}

@Composable
private fun WeekdayRow(weekStart: DayOfWeek) {
    val locale = currentLocale()
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        repeat(7) { i ->
            val day = weekStart.plus(i.toLong())
            Text(
                text = day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale).take(2),
                style = MaterialTheme.typography.labelSmall,
                color = if (day == DayOfWeek.SUNDAY) NothingColors.Red else NothingColors.Grey500,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    weekStart: DayOfWeek,
    today: LocalDate,
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onClick: (LocalDate) -> Unit,
    onLongClick: (LocalDate) -> Unit,
) {
    val firstCell = remember(month, weekStart) {
        month.atDay(1).with(TemporalAdjusters.previousOrSame(weekStart))
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        repeat(6) { week ->
            Row(Modifier.weight(1f).fillMaxWidth()) {
                repeat(7) { dayIndex ->
                    val date = firstCell.plusDays((week * 7 + dayIndex).toLong())
                    DayCell(
                        date = date,
                        inMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        isSelected = date == selected,
                        eventCount = eventsByDate[date]?.size ?: 0,
                        onClick = { onClick(date) },
                        onLongClick = { onLongClick(date) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    eventCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circle = when {
        isSelected && isToday -> NothingColors.Red
        isSelected -> NothingColors.White
        else -> NothingColors.Black.copy(alpha = 0f)
    }
    val numberColor = when {
        isSelected && isToday -> NothingColors.White
        isSelected -> NothingColors.Black
        isToday -> NothingColors.Red
        !inMonth -> NothingColors.Grey500
        else -> NothingColors.White
    }
    val dotColor = when {
        isToday -> NothingColors.Red
        !inMonth -> NothingColors.Grey500
        else -> NothingColors.White
    }

    Box(
        modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(circle, CircleShape)
                    .then(
                        if (isToday && !isSelected) Modifier.border(1.dp, NothingColors.Red, CircleShape)
                        else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MonoFamily,
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = numberColor,
                )
            }
            Spacer(Modifier.height(4.dp))
            EventDots(count = eventCount, color = dotColor)
        }
    }
}

@Composable
private fun Agenda(
    date: LocalDate,
    events: List<CalendarEvent>,
    onOpenDay: () -> Unit,
    onEdit: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale()
    val pattern = stringResource(R.string.pattern_agenda)
    val dateFormatter = remember(pattern, locale) { DateTimeFormatter.ofPattern(pattern, locale) }

    Column(modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = date.format(dateFormatter).uppercase(locale),
                style = MaterialTheme.typography.labelLarge,
                color = NothingColors.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            PillButton(stringResource(R.string.open_day) + " →", onClick = onOpenDay)
        }
        if (events.isEmpty()) {
            EmptyAgenda(Modifier.fillMaxWidth().weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(event, onClick = { onEdit(event) })
                }
            }
        }
    }
}

@Composable
private fun EmptyAgenda(modifier: Modifier = Modifier) {
    Column(
        modifier.padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DotMatrixText("0", dotPitch = 5.dp, color = NothingColors.Grey700)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.no_events).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = NothingColors.Grey300,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.no_events_hint),
            style = MaterialTheme.typography.bodySmall,
            color = NothingColors.Grey500,
        )
    }
}

@Composable
fun EventCard(event: CalendarEvent, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val timeFormatter = rememberTimeFormatter()
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NothingColors.Grey900)
            .border(1.dp, NothingColors.Grey800, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(if (event.isAllDay) NothingColors.Red else NothingColors.White, CircleShape),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                color = NothingColors.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.note.isNotBlank()) {
                Text(
                    event.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = NothingColors.Grey300,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = if (event.isAllDay) stringResource(R.string.all_day).uppercase() else event.timeRange(timeFormatter),
            style = MaterialTheme.typography.labelMedium,
            color = NothingColors.Grey300,
        )
    }
}
