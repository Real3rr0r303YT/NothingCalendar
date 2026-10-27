package dev.corebreach.fx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram

/**
 * Post-Processing: Szene -> FrameBuffer -> Glitch/Chromatic/Scanlines/Vignette -> Screen.
 * Faellt still auf direktes Rendering zurueck, wenn das GPU-Setup zickt.
 */
class PostFx {
    private val batch = SpriteBatch(1)
    private val shader: ShaderProgram
    private var fbo: FrameBuffer? = null
    private var broken = false
    private var active = false

    /** 0 = ruhig, 1 = voller Glitch. Wird jedes Frame vom Screen gesetzt. */
    var intensity = 0f

    init {
        ShaderProgram.pedantic = false
        shader = ShaderProgram(VERT, FRAG)
        if (!shader.isCompiled) {
            Gdx.app.error("PostFx", "Shader-Fehler, Post-FX aus:\n${shader.log}")
            broken = true
        }
    }

    /** Startet das Rendern in den FrameBuffer. false = direkt auf den Screen rendern. */
    fun begin(): Boolean {
        active = false
        if (broken) return false
        val w = Gdx.graphics.backBufferWidth
        val h = Gdx.graphics.backBufferHeight
        if (w <= 0 || h <= 0) return false
        try {
            val current = fbo
            if (current == null || current.width != w || current.height != h) {
                current?.dispose()
                fbo = FrameBuffer(Pixmap.Format.RGBA8888, w, h, false)
            }
            fbo!!.begin()
            active = true
        } catch (e: Exception) {
            Gdx.app.error("PostFx", "FrameBuffer nicht verfuegbar, Post-FX aus", e)
            broken = true
            fbo = null
        }
        return active
    }

    fun end(time: Float) {
        if (!active) return
        val buffer = fbo ?: return
        buffer.end()
        active = false

        val w = buffer.width.toFloat()
        val h = buffer.height.toFloat()
        batch.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        batch.shader = shader
        batch.disableBlending()
        batch.begin()
        shader.setUniformf("u_time", time)
        shader.setUniformf("u_intensity", intensity.coerceIn(0f, 1f))
        shader.setUniformf("u_resolution", w, h)
        val tex = buffer.colorBufferTexture
        batch.draw(tex, 0f, 0f, w, h, 0, 0, tex.width, tex.height, false, true)
        batch.end()
        batch.enableBlending()
    }

    fun dispose() {
        fbo?.dispose()
        shader.dispose()
        batch.dispose()
    }

    companion object {
        private const val VERT = """
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
varying vec2 v_texCoords;
void main() {
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}
"""

        private const val FRAG = """
#ifdef GL_ES
precision mediump float;
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;
uniform float u_intensity;
uniform vec2 u_resolution;

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = v_texCoords;
    float t = floor(u_time * 14.0);

    // Horizontale Glitch-Baender
    float band = step(1.0 - u_intensity * 0.22, rand(vec2(floor(uv.y * 36.0), t)));
    uv.x += band * (rand(vec2(t, uv.y)) - 0.5) * 0.09 * u_intensity;

    // Chromatische Aberration
    float shift = 0.0015 + 0.006 * u_intensity;
    float r = texture2D(u_texture, uv + vec2(shift, 0.0)).r;
    float g = texture2D(u_texture, uv).g;
    float b = texture2D(u_texture, uv - vec2(shift, 0.0)).b;
    vec3 col = vec3(r, g, b);

    // Scanlines (an Aufloesung gekoppelt)
    float scan = 0.92 + 0.08 * sin(uv.y * u_resolution.y * 1.5);
    col *= scan;

    // Vignette
    vec2 d = v_texCoords - 0.5;
    col *= 1.0 - dot(d, d) * 0.55;

    // Rauschen bei Treffern
    col += (rand(uv * u_time) - 0.5) * 0.08 * u_intensity;

    gl_FragColor = vec4(col, 1.0);
}
"""
    }
}
