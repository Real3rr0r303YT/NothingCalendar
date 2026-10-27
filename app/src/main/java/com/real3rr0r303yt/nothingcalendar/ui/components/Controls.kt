package com.real3rr0r303yt.nothingcalendar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.real3rr0r303yt.nothingcalendar.ui.theme.NothingColors

enum class PillStyle { Outlined, Filled, Inverted, Danger }

/** Uppercase monospace pill button, the signature Nothing OS control. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Outlined,
) {
    val (background, foreground, border) = when (style) {
        PillStyle.Outlined -> Triple(Color.Transparent, NothingColors.White, NothingColors.Grey700)
        PillStyle.Filled -> Triple(NothingColors.Red, NothingColors.White, NothingColors.Red)
        PillStyle.Inverted -> Triple(NothingColors.White, NothingColors.Black, NothingColors.White)
        PillStyle.Danger -> Triple(Color.Transparent, NothingColors.Red, NothingColors.Red)
    }
    Box(
        modifier
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = foreground)
    }
}

/** Red circular "+" action button. */
@Composable
fun NothingFab(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(NothingColors.Red)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Add, contentDescription = contentDescription, tint = NothingColors.White)
    }
}

/** Horizontal line made of dots. */
@Composable
fun DottedDivider(modifier: Modifier = Modifier, color: Color = NothingColors.Grey700) {
    Canvas(modifier.fillMaxWidth().height(2.dp)) {
        val step = 6.dp.toPx()
        val r = 1.dp.toPx()
        var x = r
        while (x < size.width) {
            drawCircle(color, r, Offset(x, size.height / 2))
            x += step
        }
    }
}

/** Small row of dots indicating how many events a day has (max 3). */
@Composable
fun EventDots(count: Int, color: Color, modifier: Modifier = Modifier) {
    Row(modifier.height(4.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(count.coerceAtMost(3)) {
            Box(Modifier.size(4.dp).background(color, CircleShape))
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = NothingColors.White)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = NothingColors.Grey300)
            }
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NothingColors.White,
                checkedTrackColor = NothingColors.Red,
                checkedBorderColor = NothingColors.Red,
                uncheckedThumbColor = NothingColors.Grey300,
                uncheckedTrackColor = NothingColors.Black,
                uncheckedBorderColor = NothingColors.Grey700,
            ),
        )
    }
}

/** Label + value field that opens a picker when tapped. */
@Composable
fun PickerField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, NothingColors.Grey700, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = NothingColors.Grey300)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = NothingColors.White)
    }
}
