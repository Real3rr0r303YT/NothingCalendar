package dev.corebreach.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import dev.corebreach.CoreBreachGame
import dev.corebreach.Haptics
import dev.corebreach.Save
import dev.corebreach.Sfx
import dev.corebreach.level.Levels
import dev.corebreach.ui.Controls
import dev.corebreach.ui.DotFont
import dev.corebreach.ui.Palette
import dev.corebreach.world.GameWorld
import dev.corebreach.world.Phys
import dev.corebreach.world.WorldEvents
import dev.corebreach.world.WorldRenderer

class PlayScreen(game: CoreBreachGame, private val levelIndex: Int) : CyberScreen(game), WorldEvents {

    private val level = Levels.all[levelIndex]
    private var world = GameWorld(level, this)
    private val cam = OrthographicCamera()
    private val view = ExtendViewport(18f, 10f, cam)
    private val renderer = WorldRenderer(game.shapes)
    private val controls = Controls()

    private var doneTime = -1f
    private var segfaultTime = -1f
    private var shake = 0f

    init {
        val p = world.core.position
        cam.position.set(p.x, p.y, 0f)
        glitch = 0.7f
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        view.update(width, height, false)
    }

    override fun update(dt: Float) {
        controls.poll()
        if (controls.pausePressed || backPressed()) {
            switchTo(SelectScreen(game, levelIndex))
            return
        }

        when {
            doneTime >= 0f -> {
                doneTime += dt
                if (doneTime > 2.0f) {
                    if (levelIndex + 1 < Levels.all.size) switchTo(PlayScreen(game, levelIndex + 1))
                    else switchTo(SelectScreen(game, levelIndex))
                }
            }
            segfaultTime >= 0f -> {
                segfaultTime += dt
                if (segfaultTime > 1.6f) restart()
            }
        }
        world.update(dt, controls)

        followCamera(dt)
        shake = (shake - dt * 3f).coerceAtLeast(0f)
    }

    private fun restart() {
        world.dispose()
        world = GameWorld(level, this)
        segfaultTime = -1f
        glitch = 1f
    }

    private fun followCamera(dt: Float) {
        val p = world.core.position
        val v = world.core.linearVelocity
        val tx = p.x + v.x * 0.25f
        val ty = p.y + v.y * 0.08f
        val k = 1f - Math.pow(0.002, dt.toDouble()).toFloat()
        cam.position.x += (tx - cam.position.x) * k
        cam.position.y += (ty - cam.position.y) * k

        val halfW = view.worldWidth / 2f
        val halfH = view.worldHeight / 2f
        cam.position.x = if (level.width <= halfW * 2) level.width / 2f
        else cam.position.x.coerceIn(halfW, level.width - halfW)
        cam.position.y = if (level.height <= halfH * 2) level.height / 2f
        else cam.position.y.coerceIn(halfH, level.height - halfH)

        if (shake > 0f) {
            cam.position.x += MathUtils.random(-1f, 1f) * shake * 0.25f
            cam.position.y += MathUtils.random(-1f, 1f) * shake * 0.25f
        }
        cam.update()
    }

    override fun draw() {
        view.apply()
        renderer.render(world, cam)
        drawHud()
    }

    private fun drawHud() {
        val sr = beginHud()
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val u = unit
        val small = u * 0.42f
        val top = h - u * 6f

        // Integrity (oben links)
        DotFont.draw(sr, "INTEGRITY", u * 4f, top, small, Palette.a(Palette.SIGNAL, 0.6f))
        for (i in 0 until Phys.MAX_INTEGRITY) {
            val on = i < world.integrity
            sr.color = if (on) Palette.CORE else Palette.DIM
            sr.circle(u * 5f + i * u * 3.2f, top - u * 3f, if (on) u * 1.1f else u * 0.5f, 12)
        }

        // Level (oben Mitte)
        DotFont.draw(sr, level.label + " // " + level.name, w / 2f, top, small, Palette.SIGNAL, DotFont.CENTER)
        if (world.gravSign < 0f) {
            if ((time * 3f).toInt() % 2 == 0) {
                DotFont.draw(sr, "GRAVITY INVERTED", w / 2f, top - u * 4f, small * 0.8f, Palette.CORE, DotFont.CENTER)
            }
        }

        // Shards (oben rechts, links vom Pause-Button)
        val shardText = "SHARD " + world.shardsCollected + "/3"
        DotFont.draw(sr, shardText, w - u * 16f, top, small, Palette.a(Palette.SIGNAL, 0.6f), DotFont.RIGHT)
        for (i in 0 until 3) {
            val on = world.shardMask and (1 shl i) != 0
            sr.color = if (on) Palette.SIGNAL else Palette.DIM
            val cx = w - u * 16f - i * u * 3f - u
            sr.circle(cx, top - u * 3f, if (on) u * 0.9f else u * 0.45f, 4)
        }

        controls.draw(sr)

        // Intro-Titel
        if (world.time < 1.8f && doneTime < 0f) {
            val a = 1f - (world.time / 1.8f)
            DotFont.draw(sr, level.name, w / 2f, h * 0.55f, u * 1.2f, Palette.a(Palette.SIGNAL, a), DotFont.CENTER)
            DotFont.draw(sr, "> BREACH " + level.label, w / 2f, h * 0.55f - u * 6f, small, Palette.a(Palette.CORE, a), DotFont.CENTER)
        }

        if (doneTime >= 0f) overlay(sr, w, h, u, "ACCESS GRANTED", "SHARDS " + world.shardsCollected + "/3", Palette.SIGNAL)
        if (segfaultTime >= 0f) overlay(sr, w, h, u, "SEGFAULT", "REBOOTING CORE-0...", Palette.CORE)
        sr.end()
    }

    private fun overlay(
        sr: com.badlogic.gdx.graphics.glutils.ShapeRenderer, w: Float, h: Float, u: Float,
        title: String, sub: String, color: com.badlogic.gdx.graphics.Color,
    ) {
        sr.color = Palette.a(Palette.VOID, 0.75f)
        sr.rect(0f, h * 0.35f, w, h * 0.3f)
        sr.color = Palette.CORE
        sr.rect(0f, h * 0.65f - u * 0.4f, w, u * 0.4f)
        sr.rect(0f, h * 0.35f, w, u * 0.4f)
        DotFont.draw(sr, title, w / 2f, h * 0.5f, u * 1.3f, color, DotFont.CENTER)
        DotFont.draw(sr, sub, w / 2f, h * 0.41f, u * 0.5f, Palette.a(Palette.SIGNAL, 0.8f), DotFont.CENTER)
    }

    // ------------------------------------------------------------------ WorldEvents

    override fun onJump() = Sfx.play("jump", 0.5f, MathUtils.random(0.95f, 1.05f))

    override fun onStomp() {
        Sfx.play("stomp", 0.8f)
        Haptics.pulse(35)
        glitch = 0.35f
        shake = 0.6f
    }

    override fun onHit() {
        Sfx.play("hit", 0.8f)
        Haptics.pulse(140)
        glitch = 1f
        shake = 1f
    }

    override fun onShard() {
        Sfx.play("shard", 0.7f)
        Haptics.pulse(15)
    }

    override fun onGravity() {
        Sfx.play("gravity", 0.7f)
        Haptics.pulse(45)
        glitch = 0.55f
    }

    override fun onTerminal() {
        Sfx.play("terminal", 0.8f)
        Haptics.pulse(60)
        glitch = 0.7f
        shake = 0.5f
    }

    override fun onExit() {
        Sfx.play("win", 0.8f)
        Haptics.pulse(70)
        glitch = 0.6f
        doneTime = 0f
        Save.completeLevel(level.id, levelIndex, world.shardMask)
    }

    override fun onSegfault() {
        Sfx.play("segfault", 0.8f)
        Haptics.pulse(300)
        glitch = 1f
        segfaultTime = 0f
    }

    override fun dispose() {
        world.dispose()
    }
}
