package dev.corebreach.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import dev.corebreach.CoreBreachGame

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("CORE//BREACH")
        setWindowedMode(1280, 720)
        useVsync(true)
        setForegroundFPS(60)
        setBackBufferConfig(8, 8, 8, 8, 16, 0, 4)
    }
    Lwjgl3Application(CoreBreachGame(), config)
}
