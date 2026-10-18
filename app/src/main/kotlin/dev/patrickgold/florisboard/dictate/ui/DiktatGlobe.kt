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

import android.animation.ValueAnimator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The app icon "Welle um die Welt" as line art, drawn per frame (docs/design/diktat-design.md §5).
 *
 * With [animate] it plays animation D: the wave turns around the globe while its amplitude breathes, front
 * and back in counter-phase. Without it, or when the system has animations turned off, it shows the still
 * launcher icon. The geometry is the one of docs/design/icon/diktat_icon.py; see [DiktatGlobeGeometry].
 */
@Composable
fun DiktatGlobe(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val moving = animate && remember { ValueAnimator.areAnimatorsEnabled() }
    val loop = if (moving) {
        val transition = rememberInfiniteTransition(label = "diktat-globe")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(DiktatGlobeGeometry.LOOP_MS, easing = LinearEasing),
                RepeatMode.Restart,
            ),
            label = "diktat-globe-loop",
        )
        value
    } else {
        null
    }
    Canvas(modifier) {
        val scale = size.minDimension / DiktatGlobeGeometry.BOX
        val dx = (size.width - DiktatGlobeGeometry.BOX * scale) / 2f
        val dy = (size.height - DiktatGlobeGeometry.BOX * scale) / 2f
        fun at(p: DiktatGlobeGeometry.Point) = Offset(dx + p.x * scale, dy + p.y * scale)

        val points = DiktatGlobeGeometry.wave(loop)
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            drawLine(
                color = color,
                start = at(a),
                end = at(b),
                strokeWidth = DiktatGlobeGeometry.widthForDepth((a.z + b.z) / 2) * scale,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = color,
            radius = DiktatGlobeGeometry.R * scale,
            center = Offset(dx + DiktatGlobeGeometry.C * scale, dy + DiktatGlobeGeometry.C * scale),
            style = Stroke(width = DiktatGlobeGeometry.W_FRONT * scale),
        )
    }
}

/**
 * The globe's geometry, a port of docs/design/icon/diktat_icon.py: a 48-unit box, an orthographic projection
 * of a tilted sphere, and a wave `lat = A · sin(K·lon + PHASE)` around it. Kept free of Android so it can be
 * tested on the JVM.
 */
internal object DiktatGlobeGeometry {
    const val BOX = 48f
    const val C = 24f
    const val R = 20.6f
    const val W_FRONT = 2.8f
    const val W_BACK = 1.3f
    private const val A = 19.0
    private const val K = 3
    private const val TILT = 22.0
    private const val PHASE = 180.0

    /** Samples along the wave, as in the generator. */
    const val SAMPLES = 240

    /** One loop of animation D: the wave turns 120°, so a full turn takes three loops (18 s). */
    const val LOOP_MS = 6000

    data class Point(val x: Float, val y: Float, val z: Float)

    private fun rad(deg: Double) = deg * PI / 180.0

    fun project(lon: Double, lat: Double): Point {
        val lo = rad(lon)
        val la = rad(lat)
        val t = rad(TILT)
        val x = cos(la) * sin(lo)
        val y = sin(la)
        val z = cos(la) * cos(lo)
        val y2 = y * cos(t) - z * sin(t)
        val z2 = y * sin(t) + z * cos(t)
        return Point((C + R * x).toFloat(), (C - R * y2).toFloat(), z2.toFloat())
    }

    /** Latitude of the still icon. */
    fun latStatic(lon: Double): Double = A * sin(rad(K * lon + PHASE))

    /**
     * Latitude in animation D at loop position [tt] in [0, 1): the wave turns by 120° per loop, and its
     * amplitude breathes twice per loop down to 65 %, the back half in counter-phase to the front.
     */
    fun latD(lon: Double, tt: Double): Double {
        val off = 120.0 * tt
        val c = 0.5 - 0.5 * cos(2 * PI * 2 * tt)
        var w = (cos(rad(lon)) + 1) / 2
        w = w * w * (3 - 2 * w)
        val cc = w * c + (1 - w) * (1 - c)
        return A * (1.0 - 0.35 * cc) * sin(rad(K * (lon + off) + PHASE))
    }

    /** The projected wave, [SAMPLES] + 1 points; [loop] null gives the still icon. */
    fun wave(loop: Float?): List<Point> = List(SAMPLES + 1) { i ->
        val lon = -180.0 + 360.0 * i / SAMPLES
        project(lon, if (loop == null) latStatic(lon) else latD(lon, loop.toDouble()))
    }

    /** Stroke width by depth: [W_FRONT] in front, easing to [W_BACK] behind the globe. */
    fun widthForDepth(z: Float): Float {
        var t = ((z + 1f) / 2f).coerceIn(0f, 1f)
        t = t * t * (3 - 2 * t)
        return W_BACK + (W_FRONT - W_BACK) * t
    }
}
