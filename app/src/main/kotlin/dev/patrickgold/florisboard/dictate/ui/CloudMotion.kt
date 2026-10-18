/*
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.ui

import kotlin.math.PI
import kotlin.math.abs

/**
 * The motion of the cloud prototype (docs/design/entwurf-3/SchwebenderButton.html), as numbers: four
 * blurred blobs on a layer half again as large as the sphere, each running its CSS keyframes forth and back
 * (`alternate`, ease-in-out), the layer turning once in 140 s and the smoke on it the other way in 90 s.
 *
 * Positions are in the sphere's own unit square (0..1, top left origin), so the shader and the fallback
 * draw from the same values. Kept free of Android so it can be tested on the JVM.
 */
internal object CloudMotion {

    /** One blob of the prototype: its box on the layer, its colour slot, opacity and keyframes. */
    class BlobSpec(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val opacity: Float,
        /** Seconds for one pass from 0 % to 100 %. */
        val idleSeconds: Float,
        /** The same while listening; the prototype speeds up blobs a and b to 12 s. */
        val listenSeconds: Float,
        /** Keyframes at 50 % and 100 %: translate x/y (fraction of the blob's own size), scale, rotation°. */
        val mid: FloatArray,
        val end: FloatArray,
    )

    /** A blob at one moment: centre and radii in sphere units, rotation in radians. */
    data class Blob(val cx: Float, val cy: Float, val rx: Float, val ry: Float, val rotation: Float, val opacity: Float)

    /** The `.sw` layer reaches 25 % past every edge of the sphere. */
    private const val LAYER_INSET = 0.25f
    private const val LAYER_SIZE = 1f + 2 * LAYER_INSET

    const val LAYER_TURN_SECONDS = 140f
    const val SMOKE_TURN_SECONDS = 90f

    /** Blur of the blobs as a share of the sphere's diameter: `--b: 8px` on the 90 px sphere. */
    const val BLUR = 8f / 90f

    val BLOBS = listOf(
        // .a  o1
        BlobSpec(0.08f, 0.08f, 0.60f, 0.58f, 1.0f, 26f, 12f, floatArrayOf(0.12f, 0.08f, 1.10f, 25f), floatArrayOf(-0.06f, 0.12f, 0.94f, -15f)),
        // .b  o2
        BlobSpec(0.44f, 0.30f, 0.56f, 0.62f, 1.0f, 32f, 12f, floatArrayOf(-0.14f, -0.09f, 1.12f, -30f), floatArrayOf(0.07f, -0.04f, 0.96f, 10f)),
        // .c  o3
        BlobSpec(0.12f, 0.58f, 0.66f, 0.38f, 0.7f, 21f, 21f, floatArrayOf(0.10f, -0.12f, 1.08f, 0f), floatArrayOf(-0.08f, -0.04f, 1.03f, 0f)),
        // .d  o4
        BlobSpec(0.22f, 0.24f, 0.32f, 0.32f, 0.9f, 18f, 18f, floatArrayOf(0.28f, 0.20f, 1.18f, 0f), floatArrayOf(0.08f, 0.34f, 0.88f, 0f)),
    )

    /**
     * Blob [spec] at [phase], counted in passes: 0..1 runs the keyframes forward, 1..2 back (`alternate`),
     * and so on.
     */
    fun blobAt(spec: BlobSpec, phase: Float): Blob {
        val cycle = phase.mod(2f)
        val progress = if (cycle <= 1f) cycle else 2f - cycle
        val from: FloatArray
        val to: FloatArray
        val local: Float
        if (progress < 0.5f) {
            from = IDENTITY; to = spec.mid; local = progress * 2f
        } else {
            from = spec.mid; to = spec.end; local = (progress - 0.5f) * 2f
        }
        val e = easeInOut(local)
        fun lerp(i: Int) = from[i] + (to[i] - from[i]) * e
        val tx = lerp(0) * spec.width
        val ty = lerp(1) * spec.height
        val scale = lerp(2)
        val rot = lerp(3)
        val cxLayer = spec.left + spec.width / 2f + tx
        val cyLayer = spec.top + spec.height / 2f + ty
        return Blob(
            cx = cxLayer * LAYER_SIZE - LAYER_INSET,
            cy = cyLayer * LAYER_SIZE - LAYER_INSET,
            rx = spec.width / 2f * scale * LAYER_SIZE,
            ry = spec.height / 2f * scale * LAYER_SIZE,
            rotation = (rot * PI / 180.0).toFloat(),
            opacity = spec.opacity,
        )
    }

    /** The idle breath: 1 → 1.02 → 1 over 9 s, ease-in-out. [seconds] counts from any start. */
    fun breath(seconds: Float): Float {
        val p = (seconds / 9f).mod(1f)
        val half = if (p < 0.5f) p * 2f else (1f - p) * 2f
        return 1f + 0.02f * easeInOut(half)
    }

    private val IDENTITY = floatArrayOf(0f, 0f, 1f, 0f)

    /** CSS `ease-in-out`, cubic-bezier(0.42, 0, 0.58, 1). */
    fun easeInOut(x: Float): Float {
        if (x <= 0f) return 0f
        if (x >= 1f) return 1f
        // Solve bezierX(t) = x by Newton's method, then return bezierY(t).
        var t = x
        repeat(6) {
            val bx = bez(t, 0.42f, 0.58f) - x
            val d = bezD(t, 0.42f, 0.58f)
            if (abs(d) < 1e-6f) return@repeat
            t = (t - bx / d).coerceIn(0f, 1f)
        }
        return bez(t, 0f, 1f)
    }

    private fun bez(t: Float, p1: Float, p2: Float): Float {
        val u = 1f - t
        return 3f * u * u * t * p1 + 3f * u * t * t * p2 + t * t * t
    }

    private fun bezD(t: Float, p1: Float, p2: Float): Float {
        val u = 1f - t
        return 3f * u * u * p1 + 6f * u * t * (p2 - p1) + 3f * t * t * (1f - p2)
    }
}
