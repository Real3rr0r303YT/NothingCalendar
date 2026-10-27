package dev.corebreach

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Box2D
import dev.corebreach.fx.PostFx
import dev.corebreach.screens.BootScreen

class CoreBreachGame : Game() {
    lateinit var shapes: ShapeRenderer
        private set
    lateinit var fx: PostFx
        private set

    override fun create() {
        Box2D.init()
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        shapes = ShapeRenderer(8000)
        fx = PostFx()
        Sfx.load()
        setScreen(BootScreen(this))
    }

    /** Wechselt den Screen und entsorgt den alten. Nur am Ende von render() aufrufen. */
    fun go(next: Screen) {
        val old = screen
        setScreen(next)
        old?.dispose()
    }

    override fun dispose() {
        screen?.dispose()
        shapes.dispose()
        fx.dispose()
        Sfx.dispose()
    }
}
