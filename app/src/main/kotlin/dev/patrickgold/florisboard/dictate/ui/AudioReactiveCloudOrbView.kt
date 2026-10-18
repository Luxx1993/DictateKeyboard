/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * The shader's hash and value-noise helpers are kept from the earlier port of orb-ui's CloudTheme:
 * Copyright (c) 2026 Alexander Chen, licensed under the MIT License.
 * https://github.com/alexanderqchen/orb-ui/blob/main/src/themes/cloud/CloudTheme.tsx
 */

package dev.patrickgold.florisboard.dictate.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * The cloud of diktat: a glass sphere with coloured smoke in it (docs/design/diktat-design.md §4), drawn
 * after the CSS prototype `docs/design/entwurf-3/SchwebenderButton.html`.
 *
 * Layers, bottom to top, as in the prototype: the ground colour o0; four blurred blobs o1–o4 drifting on a
 * layer that turns once in 140 s ([CloudMotion]); smoke over them, turning back in 90 s; grain; in
 * [Mode.SUCCESS] / [Mode.ERROR] a tint in `color` blend mode; then the glass light top left, the shadow
 * bottom right and the bright rim.
 *
 * - [Mode.IDLE]: breathes by 2 % in 9 s while the smoke drifts. Drawn at a reduced frame rate, since nothing
 *   in it moves faster than a breath.
 * - [Mode.LISTENING]: rises with the voice, 3 % to 6.5 %, and blobs a and b drift faster (12 s).
 * - [Mode.THINKING]: a thin white spinner in the middle (1.8 s per turn).
 * - [Mode.SUCCESS] / [Mode.ERROR]: tinted green / red; the glyph on top is the caller's.
 *
 * With animations switched off in the system everything stands still. Android 13+ draws it as an AGSL
 * [RuntimeShader]; older versions, and any frame on a software canvas where a [RuntimeShader] may not draw,
 * get the same blobs and light as plain gradients without smoke and grain.
 */
internal class AudioReactiveCloudOrbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    enum class Mode { HIDDEN, IDLE, LISTENING, THINKING, SUCCESS, ERROR }

    /**
     * True to fill the whole view with the sphere, as the previews in the app do. The floating button
     * leaves room around it instead, so the sphere can rise while listening without being cut off.
     */
    var fullBleed: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /** True for a single still frame, as for the colour swatches. */
    var still: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private var colors: CloudColors = CloudPalette.DEFAULT.colors
    private var mode = Mode.HIDDEN
    private var targetLevel = 0f
    private var currentLevel = 0f
    private var currentScale = 1f
    private var seconds = 0f
    private val blobPhases = FloatArray(CloudMotion.BLOBS.size)
    private var currentTint = 0f
    private var tint = SUCCESS_TINT
    private var lastFrameNanos = 0L
    private var paused = false

    private val cloudRenderer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // A shader that does not compile falls back to the gradients rather than taking the button down.
        runCatching { RuntimeCloudRenderer() }
            .onFailure { Log.w("DiktatCloud", "cloud shader unavailable, drawing gradients", it) }
            .getOrNull()
    } else {
        null
    }

    private val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fallbackClip = Path()
    private val spinnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        // Start the blobs where the prototype's would be a little way in, rather than all at their 0 %.
        for (i in blobPhases.indices) blobPhases[i] = (i * 0.37f) % 1f
    }

    /** The palette to draw in. */
    fun setColors(value: CloudColors) {
        colors = value
        invalidate()
    }

    fun setMode(value: Mode) {
        if (mode == value) return
        mode = value
        lastFrameNanos = 0L
        when (value) {
            Mode.SUCCESS -> tint = SUCCESS_TINT
            Mode.ERROR -> tint = ERROR_TINT
            Mode.IDLE -> currentLevel = 0f
            else -> Unit
        }
        visibility = if (value == Mode.HIDDEN) INVISIBLE else VISIBLE
        invalidate()
    }

    fun setLevel(value: Float) {
        val next = value.coerceIn(0f, 1f)
        if (targetLevel == next) return
        targetLevel = next
        if (!shouldAnimate()) invalidate()
    }

    fun setPaused(value: Boolean) {
        if (paused == value) return
        paused = value
        alpha = if (value) PAUSED_ALPHA else 1f
        lastFrameNanos = 0L
        invalidate()
    }

    fun stop() {
        mode = Mode.HIDDEN
        targetLevel = 0f
        currentLevel = 0f
        currentScale = 1f
        currentTint = 0f
        lastFrameNanos = 0L
        visibility = INVISIBLE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        lastFrameNanos = 0L
    }

    override fun onDraw(canvas: Canvas) {
        if (mode == Mode.HIDDEN || width == 0 || height == 0) return

        val now = System.nanoTime()
        val deltaSeconds = if (lastFrameNanos == 0L) {
            0f
        } else {
            min((now - lastFrameNanos) / 1_000_000_000f, MAX_DELTA_SECONDS)
        }
        lastFrameNanos = now

        val motionEnabled = shouldAnimate()
        updateMotion(deltaSeconds, motionEnabled)

        // The AGSL field only exists on the GPU: handing a RuntimeShader to a software canvas throws
        // (issue #267), and having the version is no promise of having the pipeline. The floating button
        // is a window this app adds itself, and such a window can end up in ViewRootImpl.drawSoftware —
        // when the render thread is gone, under memory pressure, or because the platform decided so. The
        // canvas is the only thing that knows, and it knows per frame, so ask it per frame.
        val renderer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            canvas.isHardwareAccelerated
        ) {
            cloudRenderer
        } else {
            null
        }

        val cx = width / 2f
        val cy = height / 2f
        val box = min(width, height).toFloat()
        val base = if (fullBleed) 1f else BUTTON_SCALE
        // The sphere is drawn at full size within its box, so leave room for the voice when filling the view.
        val scale = base * currentScale / if (fullBleed) LISTEN_MAX_SCALE else 1f
        val blobs = CloudMotion.BLOBS.mapIndexed { i, spec -> CloudMotion.blobAt(spec, blobPhases[i]) }
        val layerTurn = (2.0 * PI * seconds / CloudMotion.LAYER_TURN_SECONDS).toFloat()
        val smokeTurn = (2.0 * PI * seconds / CloudMotion.SMOKE_TURN_SECONDS).toFloat()

        canvas.save()
        canvas.translate(cx - box / 2f, cy - box / 2f)
        canvas.scale(scale, scale, box / 2f, box / 2f)
        if (renderer != null) {
            renderer.draw(canvas, box, colors, blobs, layerTurn, smokeTurn, tint, currentTint, box * scale)
        } else {
            drawFallback(canvas, box, blobs, layerTurn)
        }
        canvas.restore()

        if (mode == Mode.THINKING) drawSpinner(canvas, cx, cy, box * scale, motionEnabled)

        if (motionEnabled) {
            when {
                renderer == null -> postInvalidateDelayed(FALLBACK_FRAME_MS)
                mode == Mode.IDLE && currentTint < 0.01f && abs(currentLevel) < 0.01f ->
                    postInvalidateDelayed(IDLE_FRAME_MS)
                else -> postInvalidateOnAnimation()
            }
        }
    }

    override fun onDetachedFromWindow() {
        lastFrameNanos = 0L
        super.onDetachedFromWindow()
    }

    private fun updateMotion(deltaSeconds: Float, motionEnabled: Boolean) {
        val tintTarget = if (mode == Mode.SUCCESS || mode == Mode.ERROR) 1f else 0f
        if (!motionEnabled) {
            currentLevel = 0f
            currentScale = if (mode == Mode.LISTENING) LISTEN_MIN_SCALE else 1f
            currentTint = tintTarget // reduced motion / paused / still: show the terminal tint, just static
            return
        }

        val levelRate = if (targetLevel > currentLevel) LEVEL_ATTACK_RATE else LEVEL_RELEASE_RATE
        currentLevel = damp(currentLevel, targetLevel, levelRate, deltaSeconds)
        currentTint = damp(currentTint, tintTarget, TINT_RATE, deltaSeconds)

        seconds += deltaSeconds
        val listening = mode == Mode.LISTENING
        CloudMotion.BLOBS.forEachIndexed { i, spec ->
            blobPhases[i] += deltaSeconds / if (listening) spec.listenSeconds else spec.idleSeconds
        }

        // Listening rises with the voice (the prototype's 1.03–1.065); everything else breathes.
        val scaleTarget = if (listening) {
            LISTEN_MIN_SCALE + currentLevel * (LISTEN_MAX_SCALE - LISTEN_MIN_SCALE)
        } else {
            CloudMotion.breath(seconds)
        }
        currentScale = damp(currentScale, scaleTarget, SCALE_RATE, deltaSeconds)
    }

    private fun shouldAnimate(): Boolean =
        !still && mode != Mode.HIDDEN && !paused && isShown && ValueAnimator.areAnimatorsEnabled()

    /** Blobs and light as gradients, for a canvas the shader cannot draw on. */
    private fun drawFallback(canvas: Canvas, box: Float, blobs: List<CloudMotion.Blob>, layerTurn: Float) {
        val r = box / 2f
        fallbackClip.reset()
        fallbackClip.addCircle(r, r, r, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(fallbackClip)
        fallbackPaint.shader = null
        fallbackPaint.color = colors.o0
        canvas.drawCircle(r, r, r, fallbackPaint)
        canvas.save()
        canvas.rotate(layerTurn * 180f / PI.toFloat(), r, r)
        val slots = intArrayOf(colors.o1, colors.o2, colors.o3, colors.o4)
        blobs.forEachIndexed { i, b ->
            val radius = (b.rx + b.ry) / 2f * box
            val reach = radius + CloudMotion.BLUR * box
            val c = slots[i]
            val a = (b.opacity * 255).toInt()
            fallbackPaint.shader = RadialGradient(
                b.cx * box, b.cy * box, reach,
                intArrayOf(withAlpha(c, a), withAlpha(c, a), withAlpha(c, 0)),
                floatArrayOf(0f, (radius - CloudMotion.BLUR * box).coerceAtLeast(0f) / reach, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(b.cx * box, b.cy * box, reach, fallbackPaint)
        }
        canvas.restore()
        if (currentTint > 0.01f) {
            fallbackPaint.shader = null
            fallbackPaint.color = withAlpha(tint.toColor(), (currentTint * 150).toInt())
            canvas.drawCircle(r, r, r, fallbackPaint)
        }
        fallbackPaint.shader = RadialGradient(
            0.34f * box, 0.17f * box, 0.20f * box,
            intArrayOf(Color.argb(153, 255, 255, 255), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(r, r, r, fallbackPaint)
        fallbackPaint.shader = null
        canvas.restore()
    }

    private fun drawSpinner(canvas: Canvas, cx: Float, cy: Float, orbSize: Float, motionEnabled: Boolean) {
        // The prototype's `.spin`: inset 39 %, a 2 px border on a 90 px sphere, two of four sides drawn.
        val radius = orbSize * 0.11f
        spinnerPaint.strokeWidth = max(orbSize * 0.022f, 1.5f * resources.displayMetrics.density / 2f)
        val angle = if (motionEnabled) (seconds * 360f / SPINNER_TURN_SECONDS) % 360f else 45f
        canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, angle + 180f, 180f, false, spinnerPaint)
    }

    private fun damp(current: Float, target: Float, rate: Float, deltaSeconds: Float): Float =
        current + (target - current) * (1f - exp(-rate * deltaSeconds))

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private class RuntimeCloudRenderer {
        private val shader = RuntimeShader(CLOUD_SHADER)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = this@RuntimeCloudRenderer.shader }

        fun draw(
            canvas: Canvas,
            box: Float,
            colors: CloudColors,
            blobs: List<CloudMotion.Blob>,
            layerTurn: Float,
            smokeTurn: Float,
            tint: FloatArray,
            tintStrength: Float,
            pixelSize: Float,
        ) {
            shader.setFloatUniform("u_size", box)
            shader.setFloatUniform("u_a", blobs[0].cx, blobs[0].cy, blobs[0].rx, blobs[0].ry)
            shader.setFloatUniform("u_b", blobs[1].cx, blobs[1].cy, blobs[1].rx, blobs[1].ry)
            shader.setFloatUniform("u_c", blobs[2].cx, blobs[2].cy, blobs[2].rx, blobs[2].ry)
            shader.setFloatUniform("u_d", blobs[3].cx, blobs[3].cy, blobs[3].rx, blobs[3].ry)
            shader.setFloatUniform("u_rot", blobs[0].rotation, blobs[1].rotation, blobs[2].rotation, blobs[3].rotation)
            shader.setFloatUniform("u_op", blobs[0].opacity, blobs[1].opacity, blobs[2].opacity, blobs[3].opacity)
            setColor("u_o0", colors.o0)
            setColor("u_o1", colors.o1)
            setColor("u_o2", colors.o2)
            setColor("u_o3", colors.o3)
            setColor("u_o4", colors.o4)
            shader.setFloatUniform("u_layerTurn", layerTurn)
            shader.setFloatUniform("u_smokeTurn", smokeTurn)
            shader.setFloatUniform("u_blur", CloudMotion.BLUR)
            // About 160 grains across the sphere, as the prototype's grain tile gives, but never below a pixel.
            shader.setFloatUniform("u_grain", max(1f, pixelSize / GRAIN_ACROSS) * box / max(pixelSize, 1f))
            shader.setFloatUniform("u_tint", tint[0], tint[1], tint[2])
            shader.setFloatUniform("u_tintStrength", tintStrength)
            canvas.drawRect(0f, 0f, box, box, paint)
        }

        private fun setColor(name: String, argb: Int) {
            shader.setFloatUniform(name, Color.red(argb) / 255f, Color.green(argb) / 255f, Color.blue(argb) / 255f)
        }
    }

    companion object {
        /** The sphere's share of the floating button's 64 dp: the 48 dp core, room left to rise. */
        private const val BUTTON_SCALE = 0.75f
        private const val LISTEN_MIN_SCALE = 1.03f
        private const val LISTEN_MAX_SCALE = 1.065f
        private const val LEVEL_ATTACK_RATE = 13f
        private const val LEVEL_RELEASE_RATE = 6f
        private const val SCALE_RATE = 8f
        private const val TINT_RATE = 7f
        private const val MAX_DELTA_SECONDS = 0.05f
        private const val PAUSED_ALPHA = 0.45f
        private const val FALLBACK_FRAME_MS = 33L
        /** Idle moves no faster than a breath; 20 frames a second keep it smooth at a fraction of the cost. */
        private const val IDLE_FRAME_MS = 50L
        private const val SPINNER_TURN_SECONDS = 1.8f
        private const val GRAIN_ACROSS = 160f

        /** `--status-success` #357A5B, and a red of the same weight. */
        private val SUCCESS_TINT = floatArrayOf(0x35 / 255f, 0x7A / 255f, 0x5B / 255f)
        private val ERROR_TINT = floatArrayOf(0xB3 / 255f, 0x44 / 255f, 0x3A / 255f)

        private fun FloatArray.toColor(): Int =
            Color.rgb((this[0] * 255).toInt(), (this[1] * 255).toInt(), (this[2] * 255).toInt())

        private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

        /**
         * The cloud's surface read top to bottom, so a control beside it can be cut from the same colours
         * instead of guessing at the palette.
         */
        fun surfaceGradient(colors: CloudColors): IntArray = intArrayOf(colors.o3, colors.o0, colors.o1)

        /** The prototype's layers in AGSL; see the class comment for the order. */
        private const val CLOUD_SHADER = """
            uniform float u_size;
            uniform float4 u_a;
            uniform float4 u_b;
            uniform float4 u_c;
            uniform float4 u_d;
            uniform float4 u_rot;
            uniform float4 u_op;
            uniform float3 u_o0;
            uniform float3 u_o1;
            uniform float3 u_o2;
            uniform float3 u_o3;
            uniform float3 u_o4;
            uniform float u_layerTurn;
            uniform float u_smokeTurn;
            uniform float u_blur;
            uniform float u_grain;
            uniform float3 u_tint;
            uniform float u_tintStrength;

            float hash(float2 p) {
                p = fract(p * float2(123.34, 456.21));
                p += dot(p, p + 45.32);
                return fract(p.x * p.y);
            }

            float noise(float2 p) {
                float2 i = floor(p);
                float2 f = fract(p);
                float2 u = f * f * (3.0 - 2.0 * f);
                return mix(
                    mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),
                    mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x),
                    u.y
                );
            }

            // feTurbulence fractalNoise with three octaves, roughly.
            float fbm3(float2 p) {
                float v = 0.0;
                float a = 0.5;
                for (int o = 0; o < 3; o++) {
                    v += a * noise(p);
                    p = p * 2.0 + float2(17.1, 9.4);
                    a *= 0.5;
                }
                return v / 0.875;
            }

            float2 turn(float2 v, float a) {
                float c = cos(a);
                float s = sin(a);
                return float2(c * v.x - s * v.y, s * v.x + c * v.y);
            }

            // A blurred ellipse: the share of blob b covering q, blur as a Gaussian of u_blur.
            float cover(float2 q, float4 b, float rot) {
                float2 d = turn(q - b.xy, -rot);
                float ln = max(length(d / b.zw), 0.0001);
                float dist = length(d) * (1.0 - 1.0 / ln);
                return 1.0 / (1.0 + exp(1.702 * dist / u_blur));
            }

            float3 overlay(float3 b, float s) {
                return mix(2.0 * b * s, 1.0 - 2.0 * (1.0 - b) * (1.0 - s), step(float3(0.5), b));
            }

            float lum(float3 c) { return dot(c, float3(0.3, 0.59, 0.11)); }

            // CSS mix-blend-mode: color — the hue and saturation of t at the lightness of b.
            float3 colorBlend(float3 b, float3 t) {
                float3 c = t + (lum(b) - lum(t));
                float l = lum(c);
                float n = min(min(c.r, c.g), c.b);
                float x = max(max(c.r, c.g), c.b);
                if (n < 0.0) c = l + (c - l) * l / (l - n);
                if (x > 1.0) c = l + (c - l) * (1.0 - l) / (x - l);
                return c;
            }

            half4 main(float2 fragCoord) {
                float2 uv = fragCoord / u_size;
                float2 c = uv - 0.5;
                float r = length(c);
                float aa = 1.2 / u_size;
                float edge = 1.0 - smoothstep(0.5 - aa, 0.5, r);
                if (edge <= 0.0) return half4(0.0);

                // The blob layer turns as a whole; find this point on it.
                float2 q = turn(c, -u_layerTurn) + 0.5;
                float3 col = u_o0;
                col = mix(col, u_o1, cover(q, u_a, u_rot.x) * u_op.x);
                col = mix(col, u_o2, cover(q, u_b, u_rot.y) * u_op.y);
                col = mix(col, u_o3, cover(q, u_c, u_rot.z) * u_op.z);
                col = mix(col, u_o4, cover(q, u_d, u_rot.w) * u_op.w);

                // Smoke on the same layer (half again the sphere), turning back on it. The prototype's
                // texture measures 0.74 grey at about half opacity, overlaid at 75 %, so it lightens.
                float2 s = turn(c, -(u_layerTurn - u_smokeTurn)) / 1.5 + 0.5;
                float smoke = fbm3(s * 5.6);
                col = mix(col, overlay(col, 0.736 + (smoke - 0.5) * 0.45), 0.39);

                // Grain, fixed on the sphere: 0.73 grey, ±0.06, half opacity, overlaid at 80 %.
                float g = hash(floor(fragCoord / u_grain));
                col = mix(col, overlay(col, 0.732 + (g - 0.5) * 0.2), 0.4);

                col = mix(col, colorBlend(col, u_tint), 0.9 * clamp(u_tintStrength, 0.0, 1.0));

                // Glass light, shadow, rim.
                float glass = max(0.0, 1.0 - length((uv - float2(0.34, 0.17)) / float2(0.20, 0.11)));
                col = mix(col, float3(1.0), 0.6 * glass);
                float shade = max(0.0, 1.0 - length(uv - float2(0.72, 0.82)) / 0.567);
                col = mix(col, float3(0.031, 0.055, 0.157), 0.28 * shade);
                float rim = clamp((r - 0.4525) / 0.2546, 0.0, 1.0);
                col = mix(col, float3(1.0), 0.28 * rim);

                return half4(half3(clamp(col, 0.0, 1.0) * edge), half(edge));
            }
        """
    }
}
