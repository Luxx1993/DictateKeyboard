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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * The surface of the floating button's plain styles (Pille, Ring, Kugel) in the cloud's colours: a fill
 * running from [light] top left to [deep] bottom right through [mid], the cloud's glass light top left, its
 * shadow bottom right and a faint bright rim (docs/design/diktat-design.md §4). A circle when square, a pill
 * when wider; the light and shadow keep the height's proportions so a pill is lit like a circle at its start.
 */
class GlassDrawable(light: Int, mid: Int, deep: Int) : Drawable() {

    private var colors = intArrayOf(light, mid, deep)
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shade = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val rect = RectF()

    /** Changes the three tones, e.g. when the button starts recording. */
    fun setTones(light: Int, mid: Int, deep: Int) {
        colors = intArrayOf(light, mid, deep)
        buildShaders()
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        super.onBoundsChange(bounds)
        buildShaders()
    }

    private fun buildShaders() {
        val b = bounds
        if (b.isEmpty) return
        val h = b.height().toFloat()
        val l = b.left.toFloat()
        val t = b.top.toFloat()
        fill.shader = LinearGradient(
            l, t, l + min(b.width().toFloat(), h * 1.2f), b.bottom.toFloat(),
            colors, floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
        )
        // The cloud's glass light: an ellipse at 34 % / 17 %, white at 60 % fading out.
        glow.shader = RadialGradient(
            l + h * 0.34f, t + h * 0.20f, h * 0.30f,
            intArrayOf(Color.argb(140, 255, 255, 255), Color.argb(0, 255, 255, 255)),
            null, Shader.TileMode.CLAMP,
        )
        // Its shadow at 72 % / 82 % of the circle, deep blue at 28 %.
        shade.shader = RadialGradient(
            b.right - h * 0.28f, t + h * 0.82f, h * 0.57f,
            intArrayOf(Color.argb(60, 8, 14, 40), Color.argb(0, 8, 14, 40)),
            null, Shader.TileMode.CLAMP,
        )
        rim.strokeWidth = h * 0.02f
        rim.color = Color.argb(60, 255, 255, 255)
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        rect.set(b)
        val r = min(b.width(), b.height()) / 2f
        canvas.drawRoundRect(rect, r, r, fill)
        canvas.drawRoundRect(rect, r, r, shade)
        canvas.drawRoundRect(rect, r, r, glow)
        val inset = rim.strokeWidth / 2f
        rect.inset(inset, inset)
        canvas.drawRoundRect(rect, r - inset, r - inset, rim)
    }

    /** Round, so the view's elevation casts a round shadow rather than a square one. */
    override fun getOutline(outline: Outline) {
        val b = bounds
        outline.setRoundRect(b, min(b.width(), b.height()) / 2f)
    }

    override fun setAlpha(alpha: Int) {
        fill.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fill.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
