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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlin.math.PI
import kotlin.math.sin

/**
 * The floating button "Welle": the app icon (a wave around a globe, see [DiktatGlobeGeometry]) as a line drawing in
 * one ink colour, black or white to suit whatever is behind it.
 *
 * At rest it stands still, exactly as the launcher icon. While recording the wave turns (animation D) and the
 * whole globe breathes, larger and smaller, a little more with the voice. While the transcript is on its way
 * back, or being reworded, the wave only turns. With the system's animations switched off nothing moves.
 */
class DictateWaveOrbView(context: Context) : View(context) {

    enum class Mode { IDLE, RECORDING, WORKING }

    /** Diameter of the globe's ring in px; the view itself is larger so the breathing has room. */
    var bodyDiameter: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var inkColor = Color.BLACK
    private var baseInk = Color.BLACK
    private var flashInk: Int? = null
    private var inkAnimator: ValueAnimator? = null

    private var mode = Mode.IDLE
    private var paused = false
    private val motionAllowed = ValueAnimator.areAnimatorsEnabled()

    /** 0 = the still icon, 1 = animation D; eased so a state change never makes the wave jump. */
    private var turning = 0f

    /** 0 = no breathing, 1 = breathing; eased likewise. */
    private var breathing = 0f
    private var loopPos = 0f
    private var breathPhase = 0f
    private var level = 0f
    private var levelTarget = 0f
    private var lastFrameNs = 0L

    fun setMode(newMode: Mode) {
        if (mode == newMode) return
        mode = newMode
        kick()
    }

    fun setPaused(value: Boolean) {
        if (paused == value) return
        paused = value
        kick()
    }

    /** Voice level in 0..1 while recording; makes the breathing a little deeper. */
    fun pushLevel(value: Float) {
        levelTarget = value.coerceIn(0f, 1f)
    }

    /** Black ink on a light background, white on a dark one; fades over to the new colour. */
    fun setBackdropLight(light: Boolean) {
        val target = if (light) Color.BLACK else Color.WHITE
        if (target == baseInk) return
        baseInk = target
        if (flashInk == null) fadeInkTo(target)
    }

    /** A one-off colour for the error and success marks; null goes back to the ink. */
    fun setFlash(color: Int?) {
        if (flashInk == color) return
        flashInk = color
        fadeInkTo(color ?: baseInk)
    }

    fun stop() {
        inkAnimator?.cancel()
        mode = Mode.IDLE
    }

    private fun fadeInkTo(target: Int) {
        inkAnimator?.cancel()
        if (!motionAllowed || !isAttachedToWindow) {
            inkColor = target
            invalidate()
            return
        }
        inkAnimator = ValueAnimator.ofObject(ArgbEvaluator(), inkColor, target).apply {
            duration = INK_FADE_MS
            addUpdateListener {
                inkColor = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    private fun kick() {
        lastFrameNs = 0L
        postInvalidateOnAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        kick()
    }

    override fun onDetachedFromWindow() {
        inkAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val now = System.nanoTime()
        val dt = if (lastFrameNs == 0L) 0f else ((now - lastFrameNs) / 1e9f).coerceAtMost(MAX_STEP_S)
        lastFrameNs = now

        val turnTarget = if (motionAllowed && mode != Mode.IDLE) 1f else 0f
        val breathTarget = if (motionAllowed && mode == Mode.RECORDING) 1f else 0f
        turning = approach(turning, turnTarget, dt / EASE_S)
        breathing = approach(breathing, breathTarget, dt / EASE_S)
        if (!paused) {
            if (turning > 0f) loopPos = (loopPos + dt * 1000f / DiktatGlobeGeometry.LOOP_MS) % 1f
            if (breathing > 0f) breathPhase = (breathPhase + dt / BREATH_S) % 1f
        }
        level += (levelTarget - level) * 0.25f

        val d = if (bodyDiameter > 0f) bodyDiameter else minOf(width, height) * 0.75f
        val breath = 1f + breathing * (BREATH_DEPTH + LEVEL_DEPTH * level) * sin(2f * PI.toFloat() * breathPhase)
        val unit = d / (2f * DiktatGlobeGeometry.R) * breath
        val cx = width / 2f
        val cy = height / 2f
        val origin = DiktatGlobeGeometry.C

        val haloColor = ColorUtils.setAlphaComponent(
            if (ColorUtils.calculateLuminance(inkColor) > 0.5) Color.BLACK else Color.WHITE,
            HALO_ALPHA,
        )
        ink.color = inkColor
        halo.color = haloColor

        val pts = wavePoints()
        // The halo first, all of it, so a stroke never gets a rim drawn over its neighbour.
        for (pass in 0..1) {
            val paint = if (pass == 0) halo else ink
            val extra = if (pass == 0) HALO_EXTRA * unit else 0f
            for (i in 0 until pts.size - 1) {
                val a = pts[i]
                val b = pts[i + 1]
                paint.strokeWidth = DiktatGlobeGeometry.widthForDepth((a.z + b.z) / 2) * unit * STROKE + extra
                canvas.drawLine(
                    cx + (a.x - origin) * unit, cy + (a.y - origin) * unit,
                    cx + (b.x - origin) * unit, cy + (b.y - origin) * unit,
                    paint,
                )
            }
            paint.strokeWidth = DiktatGlobeGeometry.W_FRONT * unit * STROKE + extra
            canvas.drawCircle(cx, cy, DiktatGlobeGeometry.R * unit, paint)
        }

        val moving = !paused && (mode != Mode.IDLE || turning > 0f || breathing > 0f)
        val easing = turning != turnTarget || breathing != breathTarget || level > 0.01f
        if (motionAllowed && (moving || easing)) postInvalidateOnAnimation()
    }

    /** The wave between the still icon and animation D, [turning] being the blend. */
    private fun wavePoints(): List<DiktatGlobeGeometry.Point> {
        if (turning <= 0f) return DiktatGlobeGeometry.wave(null)
        if (turning >= 1f) return DiktatGlobeGeometry.wave(loopPos)
        return List(DiktatGlobeGeometry.SAMPLES + 1) { i ->
            val lon = -180.0 + 360.0 * i / DiktatGlobeGeometry.SAMPLES
            val lat = DiktatGlobeGeometry.latStatic(lon) * (1 - turning) +
                DiktatGlobeGeometry.latD(lon, loopPos.toDouble()) * turning
            DiktatGlobeGeometry.project(lon, lat)
        }
    }

    private fun approach(value: Float, target: Float, step: Float): Float = when {
        value < target -> minOf(target, value + step)
        value > target -> maxOf(target, value - step)
        else -> value
    }

    private companion object {
        const val INK_FADE_MS = 280L
        const val EASE_S = 0.45f
        const val MAX_STEP_S = 0.064f

        /** One breath takes this long. */
        const val BREATH_S = 2.6f
        const val BREATH_DEPTH = 0.06f
        const val LEVEL_DEPTH = 0.09f

        /** Lines are drawn a little finer than in the launcher icon, which is meant to be seen large. */
        const val STROKE = 0.86f
        const val HALO_ALPHA = 70
        const val HALO_EXTRA = 1.1f
    }
}
