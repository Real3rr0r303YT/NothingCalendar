@file:OptIn(ExperimentalMaterial3Api::class)

package com.real3rr0r303yt.nothingcalendar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingcalendar.BuildConfig
import com.real3rr0r303yt.nothingcalendar.R
import com.real3rr0r303yt.nothingcalendar.data.AppSettings
import com.real3rr0r303yt.nothingcalendar.ui.components.DotMatrixText
import com.real3rr0r303yt.nothingcalendar.ui.components.DottedDivider
import com.real3rr0r303yt.nothingcalendar.ui.components.PillButton
import com.real3rr0r303yt.nothingcalendar.ui.components.PillStyle
import com.real3rr0r303yt.nothingcalendar.ui.components.SettingSwitchRow
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingColors
import com.real3rr0r303yt.nothingcalendar.util.LocalHaptics
import com.real3rr0r303yt.nothingcalendar.util.currentLocale
import java.time.DayOfWeek
import java.time.format.TextStyle

private const val DONATION_URL = "https://www.buymeacoffee.com/3rr0r303yt"

@Composable
fun SettingsSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onWeekStartChange: (DayOfWeek) -> Unit,
) {
    val haptics = LocalHaptics.current
    val locale = currentLocale()
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NothingColors.Grey900,
        contentColor = NothingColors.White,
        scrimColor = Color.Black.copy(alpha = 0.7f),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.settings).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NothingColors.Red,
            )

            SettingSwitchRow(
                title = stringResource(R.string.haptics),
                description = stringResource(R.string.haptics_desc),
                checked = settings.hapticsEnabled,
                onCheckedChange = { enabled ->
                    onHapticsChange(enabled)
                    // Give feedback with the new setting so enabling it is felt immediately.
                    haptics.enabled = enabled
                    haptics.tick()
                },
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.week_start),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY).forEach { day ->
                        PillButton(
                            text = day.getDisplayName(TextStyle.SHORT, locale),
                            style = if (settings.weekStart == day) PillStyle.Inverted else PillStyle.Outlined,
                            onClick = {
                                haptics.tick()
                                onWeekStartChange(day)
                            },
                        )
                    }
                }
            }

            DottedDivider()

            PillButton(
                text = "☕ " + stringResource(R.string.support),
                style = PillStyle.Filled,
                onClick = {
                    haptics.click()
                    uriHandler.openUri(DONATION_URL)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                DotMatrixText("CALENDAR", dotPitch = 3.dp, color = NothingColors.Grey500)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingColors.Grey500,
                )
            }
        }
    }
}
