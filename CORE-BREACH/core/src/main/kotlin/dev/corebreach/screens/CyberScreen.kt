package dev.corebreach.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dev.corebreach.CoreBreachGame
import kotlin.math.max
import kotlin.math.min

/**
 * Basis fuer alle Screens: OLED-Clear, Post-FX, HUD-Projektion in logischen Pixeln
 * und sicherer Screen-Wechsel am Frame-Ende.
 */
abstract class CyberScreen(protected val game: CoreBreachGame) : ScreenAdapter() {
    protected val hud = ScreenViewport()
    protected var time = 0f
    protected val sr: ShapeRenderer get() = game.shapes

    /** Glitch-Pegel dieses Screens (wird pro Frame abgebaut). */
    protected var glitch = 0f
    private var randomSpike = 0f

    private var next: Screen? = null

    protected fun switchTo(screen: Screen) {
        if (next == null) next = screen
    }

    /** HUD-Masseinheit: 1/100 der kurzen Bildschirmkante. */
    protected val unit: Float get() = min(Gdx.graphics.width, Gdx.graphics.height) / 100f

    abstract fun update(dt: Float)
    abstract fun draw()

    override fun render(delta: Float) {
        val dt = min(delta, 1f / 20f)
        time += dt
        update(dt)
        if (next != null) {
            game.go(next!!)
            return
        }

        glitch = max(0f, glitch - dt * 2.2f)
        randomSpike = max(0f, randomSpike - dt * 4f)
        if (MathUtils.random() < 0.006f) randomSpike = MathUtils.random(0.25f, 0.6f)
        game.fx.intensity = max(glitch, randomSpike) + 0.03f

        val usingFx = game.fx.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        draw()
        if (usingFx) game.fx.end(time)
    }

    protected fun beginHud(): ShapeRenderer {
        hud.apply()
        sr.projectionMatrix = hud.camera.combined
        sr.begin(ShapeRenderer.ShapeType.Filled)
        return sr
    }

    override fun resize(width: Int, height: Int) {
        hud.update(width, height, true)
    }

    /** true, wenn gerade ein Tap/Klick/Enter/Space ausgeloest wurde. */
    protected fun confirmPressed(): Boolean =
        Gdx.input.justTouched() ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)

    protected fun backPressed(): Boolean =
        Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)
}
