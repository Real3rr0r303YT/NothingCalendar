package dev.corebreach.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Rectangle
import dev.corebreach.CoreBreachGame
import dev.corebreach.Save
import dev.corebreach.Sfx
import dev.corebreach.level.Levels
import dev.corebreach.ui.DotFont
import dev.corebreach.ui.Palette
import kotlin.math.min

/** Zielauswahl: Level-Karten mit Shard-Status. */
class SelectScreen(game: CoreBreachGame, initial: Int = -1) : CyberScreen(game) {

    private val levels = Levels.all
    private var selected = if (initial >= 0) initial else (Save.unlocked - 1).coerceIn(0, levels.size - 1)
    private val cards = Array(levels.size) { Rectangle() }
    private val soundBtn = Rectangle()
    private val hapticBtn = Rectangle()

    private fun layout() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val n = levels.size
        val gap = w * 0.02f
        val cw = min((w * 0.88f - gap * (n - 1)) / n, h * 0.42f)
        val ch = cw * 1.3f
        val startX = (w - (cw * n + gap * (n - 1))) / 2f
        val y = h * 0.5f - ch / 2f
        for (i in 0 until n) cards[i].set(startX + i * (cw + gap), y, cw, ch)
        val u = unit
        soundBtn.set(u * 3f, u * 3f, u * 22f, u * 6f)
        hapticBtn.set(u * 27f, u * 3f, u * 22f, u * 6f)
    }

    private fun unlocked(i: Int) = i < Save.unlocked

    override fun update(dt: Float) {
        layout()
        if (backPressed()) {
            switchTo(BootScreen(game)); return
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selected = (selected - 1).coerceAtLeast(0); Sfx.play("tick")
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selected = (selected + 1).coerceAtMost(levels.size - 1); Sfx.play("tick")
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            launch(selected)
        }
        if (Gdx.input.justTouched()) {
            val x = Gdx.input.x.toFloat()
            val y = Gdx.graphics.height - Gdx.input.y.toFloat()
            when {
                soundBtn.contains(x, y) -> { Save.sound = !Save.sound; Sfx.play("tick") }
                hapticBtn.contains(x, y) -> Save.haptics = !Save.haptics
                else -> cards.forEachIndexed { i, r ->
                    if (r.contains(x, y)) {
                        selected = i
                        launch(i)
                    }
                }
            }
        }
    }

    private fun launch(i: Int) {
        if (!unlocked(i)) {
            glitch = 0.8f
            Sfx.play("hit", 0.4f)
            return
        }
        Sfx.play("terminal", 0.5f)
        switchTo(PlayScreen(game, i))
    }

    override fun draw() {
        layout()
        val sr = beginHud()
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val u = unit

        DotFont.draw(sr, "SELECT TARGET", w / 2f, h * 0.86f, u * 0.9f, Palette.SIGNAL, DotFont.CENTER)
        DotFont.draw(sr, "WORLD 1 // BOOTLOADER", w / 2f, h * 0.86f - u * 6f, u * 0.4f, Palette.CORE, DotFont.CENTER)

        for (i in levels.indices) {
            val r = cards[i]
            val lv = levels[i]
            val open = unlocked(i)
            val sel = i == selected
            val lw = if (sel) u * 0.6f else u * 0.3f
            sr.color = when {
                sel && open -> Palette.CORE
                open -> Palette.SIGNAL
                else -> Palette.DIM
            }
            sr.rectLine(r.x, r.y, r.x + r.width, r.y, lw)
            sr.rectLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height, lw)
            sr.rectLine(r.x, r.y, r.x, r.y + r.height, lw)
            sr.rectLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height, lw)

            val numPitch = r.width / 16f
            val num = (i + 1).toString().padStart(2, '0')
            val numColor = if (open) Palette.SIGNAL else Palette.DIM
            DotFont.draw(sr, if (open) num else "##", r.x + r.width / 2f, r.y + r.height * 0.52f, numPitch, numColor, DotFont.CENTER)

            val namePitch = min(r.width * 0.86f / (lv.name.length * 6f), numPitch * 0.45f)
            DotFont.draw(sr, if (open) lv.name else "LOCKED", r.x + r.width / 2f, r.y + r.height * 0.3f, namePitch,
                if (open) Palette.a(Palette.SIGNAL, 0.7f) else Palette.DIM, DotFont.CENTER)

            // Shards
            val mask = Save.shardMask(lv.id)
            val dot = r.width * 0.05f
            for (s in 0 until 3) {
                val cx = r.x + r.width / 2f + (s - 1) * dot * 3f
                val cy = r.y + r.height * 0.14f
                sr.color = if (mask and (1 shl s) != 0) Palette.CORE else Palette.DIM
                sr.circle(cx, cy, dot * (if (mask and (1 shl s) != 0) 1f else 0.5f), 10)
            }
            if (open && Save.isDone(lv.id)) {
                DotFont.draw(sr, "OK", r.x + r.width - numPitch * 0.5f, r.y + r.height - numPitch * 2.4f,
                    numPitch * 0.25f, Palette.CORE, DotFont.RIGHT)
            }
        }

        val small = u * 0.38f
        DotFont.draw(sr, "SOUND " + (if (Save.sound) "ON" else "OFF"), soundBtn.x, soundBtn.y + small * 4f, small,
            if (Save.sound) Palette.SIGNAL else Palette.DIM)
        DotFont.draw(sr, "HAPTIC " + (if (Save.haptics) "ON" else "OFF"), hapticBtn.x, hapticBtn.y + small * 4f, small,
            if (Save.haptics) Palette.SIGNAL else Palette.DIM)
        DotFont.draw(sr, "TAP A TARGET", w - u * 3f, u * 3f + small * 4f, small, Palette.DIM, DotFont.RIGHT)
        sr.end()
    }
}
