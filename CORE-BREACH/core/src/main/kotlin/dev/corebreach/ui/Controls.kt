package dev.corebreach.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import dev.corebreach.world.ControlState

/**
 * Tastatur + Multitouch. Touch-Buttons im Nothing-Glyph-Stil (Dot-Ringe).
 * Koordinaten in logischen Pixeln (Gdx.graphics.width/height), Ursprung unten links.
 */
class Controls : ControlState {
    private var stLeft = false
    private var stRight = false
    private var stJumpHeld = false
    private var stJumpPressed = false
    private var stPause = false

    override val left: Boolean get() = stLeft
    override val right: Boolean get() = stRight
    override val jumpHeld: Boolean get() = stJumpHeld
    override val jumpPressed: Boolean get() = stJumpPressed
    val pausePressed: Boolean get() = stPause

    private var prevJump = false
    private var prevPause = false

    val showTouch: Boolean = Gdx.app.type == Application.ApplicationType.Android ||
        Gdx.app.type == Application.ApplicationType.iOS

    private var w = 1f
    private var h = 1f
    private val r get() = h * 0.085f
    private val leftX get() = r * 1.7f
    private val rightX get() = r * 4.4f
    private val moveY get() = r * 1.7f
    private val jumpX get() = w - r * 1.9f
    private val pauseX get() = w - r * 0.9f
    private val pauseY get() = h - r * 0.9f

    fun poll() {
        w = Gdx.graphics.width.toFloat()
        h = Gdx.graphics.height.toFloat()

        var l = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        var rr = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        var j = Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W) ||
            Gdx.input.isKeyPressed(Input.Keys.UP)
        var p = Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACK) ||
            Gdx.input.isKeyPressed(Input.Keys.P)

        for (i in 0 until 5) {
            if (!Gdx.input.isTouched(i)) continue
            val x = Gdx.input.getX(i).toFloat()
            val y = h - Gdx.input.getY(i).toFloat()
            if (dist(x, y, pauseX, pauseY) < r * 0.9f) {
                p = true
            } else if (showTouch) {
                if (x > w * 0.5f) {
                    j = true                                                // rechte Haelfte = Sprung
                } else if (y < h * 0.6f) {
                    if (x < (leftX + rightX) / 2f) l = true                 // linker Button
                    else if (x < rightX + r * 1.6f) rr = true               // rechter Button
                }
            }
        }

        stLeft = l
        stRight = rr
        stJumpHeld = j
        stJumpPressed = j && !prevJump
        prevJump = j
        stPause = p && !prevPause
        prevPause = p
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    /** Erwartet ShapeRenderer im Filled-Modus mit HUD-Projektion. */
    fun draw(sr: ShapeRenderer) {
        if (showTouch) {
            ring(sr, leftX, moveY, r, left)
            ring(sr, rightX, moveY, r, right)
            ring(sr, jumpX, moveY, r * 1.15f, jumpHeld)
            val s = r * 0.35f
            sr.color = if (left) Palette.CORE else Palette.SIGNAL
            sr.triangle(leftX - s, moveY, leftX + s * 0.6f, moveY + s, leftX + s * 0.6f, moveY - s)
            sr.color = if (right) Palette.CORE else Palette.SIGNAL
            sr.triangle(rightX + s, moveY, rightX - s * 0.6f, moveY + s, rightX - s * 0.6f, moveY - s)
            sr.color = if (jumpHeld) Palette.CORE else Palette.SIGNAL
            sr.triangle(jumpX, moveY + s * 1.1f, jumpX - s, moveY - s * 0.6f, jumpX + s, moveY - s * 0.6f)
        }
        // Pause-Button (auch Desktop, klickbar)
        val ps = r * 0.22f
        sr.color = Palette.a(Palette.SIGNAL, 0.8f)
        sr.rect(pauseX - ps, pauseY - ps, ps * 0.7f, ps * 2f)
        sr.rect(pauseX + ps * 0.3f, pauseY - ps, ps * 0.7f, ps * 2f)
    }

    private fun ring(sr: ShapeRenderer, cx: Float, cy: Float, radius: Float, on: Boolean) {
        val dots = 20
        sr.color = if (on) Palette.CORE else Palette.a(Palette.SIGNAL, 0.55f)
        for (i in 0 until dots) {
            val a = i * MathUtils.PI2 / dots
            sr.circle(cx + MathUtils.cos(a) * radius, cy + MathUtils.sin(a) * radius, radius * 0.045f, 6)
        }
        if (on) {
            sr.color = Palette.a(Palette.CORE, 0.18f)
            sr.circle(cx, cy, radius * 0.92f, 24)
        }
    }
}
