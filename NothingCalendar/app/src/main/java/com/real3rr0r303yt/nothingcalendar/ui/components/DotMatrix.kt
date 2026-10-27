package com.real3rr0r303yt.nothingcalendar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.Normalizer

/** 5×7 dot-matrix font in the spirit of the Nothing OS "Ndot" look. */
object DotMatrixFont {
    const val WIDTH = 5
    const val HEIGHT = 7

    private val BLANK = List(HEIGHT) { "....." }

    private val glyphs: Map<Char, List<String>> = mapOf(
        '0' to listOf(".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###."),
        '1' to listOf("..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###."),
        '2' to listOf(".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####"),
        '3' to listOf("#####", "...#.", "..#..", "...#.", "....#", "#...#", ".###."),
        '4' to listOf("...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#."),
        '5' to listOf("#####", "#....", "####.", "....#", "....#", "#...#", ".###."),
        '6' to listOf("..##.", ".#...", "#....", "####.", "#...#", "#...#", ".###."),
        '7' to listOf("#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#..."),
        '8' to listOf(".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###."),
        '9' to listOf(".###.", "#...#", "#...#", ".####", "....#", "...#.", ".##.."),
        'A' to listOf(".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"),
        'B' to listOf("####.", "#...#", "#...#", "####.", "#...#", "#...#", "####."),
        'C' to listOf(".###.", "#...#", "#....", "#....", "#....", "#...#", ".###."),
        'D' to listOf("###..", "#..#.", "#...#", "#...#", "#...#", "#..#.", "###.."),
        'E' to listOf("#####", "#....", "#....", "####.", "#....", "#....", "#####"),
        'F' to listOf("#####", "#....", "#....", "####.", "#....", "#....", "#...."),
        'G' to listOf(".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".####"),
        'H' to listOf("#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"),
        'I' to listOf(".###.", "..#..", "..#..", "..#..", "..#..", "..#..", ".###."),
        'J' to listOf("..###", "...#.", "...#.", "...#.", "...#.", "#..#.", ".##.."),
        'K' to listOf("#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#"),
        'L' to listOf("#....", "#....", "#....", "#....", "#....", "#....", "#####"),
        'M' to listOf("#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#"),
        'N' to listOf("#...#", "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#"),
        'O' to listOf(".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."),
        'P' to listOf("####.", "#...#", "#...#", "####.", "#....", "#....", "#...."),
        'Q' to listOf(".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#"),
        'R' to listOf("####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#"),
        'S' to listOf(".####", "#....", "#....", ".###.", "....#", "....#", "####."),
        'T' to listOf("#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.."),
        'U' to listOf("#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."),
        'V' to listOf("#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#.."),
        'W' to listOf("#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#."),
        'X' to listOf("#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#"),
        'Y' to listOf("#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#.."),
        'Z' to listOf("#####", "....#", "...#.", "..#..", ".#...", "#....", "#####"),
        'Ä' to listOf("#...#", ".###.", "#...#", "#...#", "#####", "#...#", "#...#"),
        'Ö' to listOf("#...#", ".###.", "#...#", "#...#", "#...#", "#...#", ".###."),
        'Ü' to listOf("#...#", ".....", "#...#", "#...#", "#...#", "#...#", ".###."),
        ':' to listOf(".....", "..#..", "..#..", ".....", "..#..", "..#..", "....."),
        '.' to listOf(".....", ".....", ".....", ".....", ".....", ".##..", ".##.."),
        '-' to listOf(".....", ".....", ".....", ".###.", ".....", ".....", "....."),
        '/' to listOf(".....", "....#", "...#.", "..#..", ".#...", "#....", "....."),
        ' ' to BLANK,
    )

    /** Returns the glyph rows for [char], stripping unknown accents (É → E). */
    fun glyph(char: Char): List<String> {
        val upper = char.uppercaseChar()
        glyphs[upper]?.let { return it }
        val base = Normalizer.normalize(upper.toString(), Normalizer.Form.NFD).firstOrNull()
        return base?.let { glyphs[it] } ?: BLANK
    }
}

/**
 * Renders [text] as a dot-matrix display. The dot pitch shrinks automatically
 * so the text always fits into the available width.
 *
 * @param dotPitch maximum distance between two dot centres.
 * @param offDotColor colour of unlit dots; leave unspecified to draw only lit dots.
 */
@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    dotPitch: Dp = 6.dp,
    offDotColor: Color = Color.Unspecified,
) {
    val glyphs = remember(text) { text.map(DotMatrixFont::glyph) }
    val columns = (glyphs.size * (DotMatrixFont.WIDTH + 1) - 1).coerceAtLeast(1)

    BoxWithConstraints(modifier) {
        val pitch = minOf(dotPitch, maxWidth / columns)
        Canvas(
            Modifier
                .size(pitch * columns, pitch * DotMatrixFont.HEIGHT)
                .semantics { contentDescription = text },
        ) {
            val p = pitch.toPx()
            val radius = p * 0.36f
            glyphs.forEachIndexed { index, rows ->
                val x0 = index * (DotMatrixFont.WIDTH + 1)
                for (row in 0 until DotMatrixFont.HEIGHT) {
                    for (col in 0 until DotMatrixFont.WIDTH) {
                        val dotColor = if (rows[row][col] == '#') color else offDotColor
                        if (dotColor.isSpecified) {
                            drawCircle(
                                color = dotColor,
                                radius = radius,
                                center = Offset((x0 + col) * p + p / 2, row * p + p / 2),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Subtle dot grid background, like the Nothing OS wallpapers and widgets. */
fun Modifier.dotGridBackground(
    color: Color = Color.White.copy(alpha = 0.06f),
    spacing: Dp = 18.dp,
    radius: Dp = 1.dp,
): Modifier = drawWithCache {
    val step = spacing.toPx()
    val r = radius.toPx()
    val path = Path()
    var y = step / 2
    while (y < size.height) {
        var x = step / 2
        while (x < size.width) {
            path.addOval(Rect(center = Offset(x, y), radius = r))
            x += step
        }
        y += step
    }
    onDrawBehind { drawPath(path, color) }
}
