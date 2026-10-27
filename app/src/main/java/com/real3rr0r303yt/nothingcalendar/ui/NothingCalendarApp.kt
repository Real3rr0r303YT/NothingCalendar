package com.real3rr0r303yt.nothingcalendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.real3rr0r303yt.nothingcalendar.ui.screens.DayScreen
import com.real3rr0r303yt.nothingcalendar.ui.screens.EventEditorSheet
import com.real3rr0r303yt.nothingcalendar.ui.screens.MonthScreen
import com.real3rr0r303yt.nothingcalendar.ui.screens.SettingsSheet
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingColors
import com.real3rr0r303yt.nothingcalendar.util.Haptics
import com.real3rr0r303yt.nothingcalendar.util.LocalHaptics

@Composable
fun NothingCalendarApp(vm: CalendarViewModel = viewModel()) {
    val settings by vm.settings.collectAsState()
    val view = LocalView.current
    val haptics = remember(view) { Haptics(view) }
    SideEffect { haptics.enabled = settings.hapticsEnabled }

    CompositionLocalProvider(LocalHaptics provides haptics) {
        Surface(color = NothingColors.Black, modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = vm.screen,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(140)) },
                label = "screen",
            ) { screen ->
                when (screen) {
                    Screen.Month -> MonthScreen(vm)
                    Screen.Day -> DayScreen(vm)
                }
            }
        }

        vm.editor?.let { request ->
            EventEditorSheet(
                request = request,
                onDismiss = vm::dismissEditor,
                onSave = vm::saveEvent,
                onDelete = vm::deleteEvent,
            )
        }

        if (vm.settingsOpen) {
            SettingsSheet(
                settings = settings,
                onDismiss = { vm.showSettings(false) },
                onHapticsChange = vm::setHapticsEnabled,
                onWeekStartChange = vm::setWeekStart,
            )
        }
    }
}
