package com.real3rr0r303yt.nothingcalendar.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object NothingColors {
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val Red = Color(0xFFD71921)
    val Grey900 = Color(0xFF0F0F0F)
    val Grey800 = Color(0xFF1A1A1A)
    val Grey700 = Color(0xFF2B2B2B)
    val Grey500 = Color(0xFF5C5C5C)
    val Grey300 = Color(0xFF9A9A9A)
}

val MonoFamily: FontFamily = FontFamily.Monospace

private val NothingColorScheme = darkColorScheme(
    primary = NothingColors.Red,
    onPrimary = NothingColors.White,
    primaryContainer = Color(0xFF3D0709),
    onPrimaryContainer = NothingColors.White,
    secondary = NothingColors.White,
    onSecondary = NothingColors.Black,
    secondaryContainer = NothingColors.Grey700,
    onSecondaryContainer = NothingColors.White,
    tertiary = NothingColors.White,
    onTertiary = NothingColors.Black,
    tertiaryContainer = Color(0xFF3D0709),
    onTertiaryContainer = NothingColors.White,
    background = NothingColors.Black,
    onBackground = NothingColors.White,
    surface = NothingColors.Black,
    onSurface = NothingColors.White,
    surfaceVariant = NothingColors.Grey800,
    onSurfaceVariant = NothingColors.Grey300,
    surfaceContainerLowest = NothingColors.Black,
    surfaceContainerLow = NothingColors.Grey900,
    surfaceContainer = NothingColors.Grey900,
    surfaceContainerHigh = NothingColors.Grey900,
    surfaceContainerHighest = NothingColors.Grey800,
    outline = NothingColors.Grey700,
    outlineVariant = NothingColors.Grey800,
    error = NothingColors.Red,
    onError = NothingColors.White,
)

private val NothingTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 56.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Light, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = MonoFamily, fontSize = 14.sp, letterSpacing = 1.5.sp),
    labelMedium = TextStyle(fontFamily = MonoFamily, fontSize = 12.sp, letterSpacing = 1.5.sp),
    labelSmall = TextStyle(fontFamily = MonoFamily, fontSize = 10.sp, letterSpacing = 1.2.sp),
)

private val NothingShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun NothingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NothingColorScheme,
        typography = NothingTypography,
        shapes = NothingShapes,
        content = content,
    )
}
