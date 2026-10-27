package dev.corebreach.ui

import com.badlogic.gdx.graphics.Color

/** Drei Farben, null Gradients. Rot = Spieler, Gefahr, Interaktion. */
object Palette {
    val VOID: Color = Color.valueOf("000000FF")
    val SIGNAL: Color = Color.valueOf("FFFFFFFF")
    val CORE: Color = Color.valueOf("DA291CFF")
    val DAEMON: Color = Color.valueOf("8A8A8AFF")
    val DIM: Color = Color.valueOf("4A4A4AFF")
    val GRID: Color = Color.valueOf("1C1C1CFF")
    val RASTER: Color = Color.valueOf("2E2E2EFF")

    private val tmp = Color()

    /** Farbe mit Alpha, ohne die Konstanten zu veraendern. */
    fun a(c: Color, alpha: Float): Color = tmp.set(c.r, c.g, c.b, alpha.coerceIn(0f, 1f))
}
