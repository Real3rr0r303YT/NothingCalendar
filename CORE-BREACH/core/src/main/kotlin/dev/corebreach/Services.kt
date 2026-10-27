package dev.corebreach

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound

/** Bitcrushed SFX aus assets/sfx. Fehlende Dateien crashen nichts. */
object Sfx {
    private val names = listOf("jump", "stomp", "hit", "shard", "gravity", "terminal", "win", "tick", "segfault")
    private val sounds = HashMap<String, Sound>()

    fun load() {
        for (n in names) {
            try {
                val f = Gdx.files.internal("sfx/$n.wav")
                if (f.exists()) sounds[n] = Gdx.audio.newSound(f)
            } catch (e: Exception) {
                Gdx.app.error("Sfx", "kann $n nicht laden", e)
            }
        }
    }

    fun play(name: String, volume: Float = 0.7f, pitch: Float = 1f) {
        if (!Save.sound) return
        sounds[name]?.play(volume, pitch, 0f)
    }

    fun dispose() {
        sounds.values.forEach { it.dispose() }
        sounds.clear()
    }
}

/** Haptik: kurze Ticks, langer Puls bei Schaden. */
object Haptics {
    fun pulse(ms: Int) {
        if (!Save.haptics || Gdx.app.type != Application.ApplicationType.Android) return
        try {
            Gdx.input.vibrate(ms)
        } catch (_: Exception) {
        }
    }
}

/** Fortschritt in SharedPreferences (Android) bzw. ~/.prefs (Desktop). */
object Save {
    private val prefs: Preferences by lazy { Gdx.app.getPreferences("dev.corebreach.save") }

    var unlocked: Int
        get() = prefs.getInteger("unlocked", 1)
        set(v) {
            prefs.putInteger("unlocked", v); prefs.flush()
        }

    var sound: Boolean
        get() = prefs.getBoolean("sound", true)
        set(v) {
            prefs.putBoolean("sound", v); prefs.flush()
        }

    var haptics: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(v) {
            prefs.putBoolean("haptics", v); prefs.flush()
        }

    fun shardMask(levelId: Int): Int = prefs.getInteger("shards_$levelId", 0)

    fun completeLevel(levelId: Int, levelIndex: Int, shardMask: Int) {
        prefs.putInteger("shards_$levelId", shardMask(levelId) or shardMask)
        if (unlocked < levelIndex + 2) prefs.putInteger("unlocked", levelIndex + 2)
        prefs.putBoolean("done_$levelId", true)
        prefs.flush()
    }

    fun isDone(levelId: Int): Boolean = prefs.getBoolean("done_$levelId", false)
}
