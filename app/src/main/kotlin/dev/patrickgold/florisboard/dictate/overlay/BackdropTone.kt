/*
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.view.Display
import androidx.core.graphics.ColorUtils
import java.util.concurrent.Executors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Whether the screen behind the floating button is light or dark, so the "Welle" button can be black or white.
 *
 * Where the system allows it (Android 11+, and only once the accessibility service has been granted the
 * screenshot capability) a still of the screen is taken, a handful of pixels around the button are read and the
 * picture is thrown away; nothing is stored or sent. Otherwise, or when that fails, the system's own light/dark
 * setting stands in.
 */
internal object BackdropTone {

    /** Mean luminance above this makes the backdrop light, below [DARK_BELOW] dark; in between nothing changes. */
    private const val LIGHT_ABOVE = 0.56
    private const val DARK_BELOW = 0.44

    /** The spokes the ring around the button is read at, and how far out (in button sizes) the two rings lie. */
    private const val SPOKES = 14
    private val RINGS = doubleArrayOf(0.55, 0.75)

    private val executor by lazy { Executors.newSingleThreadExecutor() }

    /**
     * Light or dark from the mean luminance of the sampled pixels. A dead band around the middle keeps the
     * ink from flipping back and forth on a mid-grey backdrop: inside it the [previous] verdict stays.
     */
    fun decide(meanLuminance: Double, previous: Boolean?): Boolean = when {
        meanLuminance >= LIGHT_ABOVE -> true
        meanLuminance <= DARK_BELOW -> false
        else -> previous ?: (meanLuminance >= 0.5)
    }

    /** Points on two rings around [bounds], clamped to a screen of [width] x [height]. */
    fun samplePoints(bounds: Rect, width: Int, height: Int): List<Pair<Int, Int>> {
        val size = maxOf(bounds.width(), bounds.height()).toDouble()
        val points = ArrayList<Pair<Int, Int>>(SPOKES * RINGS.size)
        for (ring in RINGS) {
            for (i in 0 until SPOKES) {
                val angle = 2 * Math.PI * i / SPOKES
                val x = (bounds.exactCenterX() + cos(angle) * ring * size).toInt().coerceIn(0, width - 1)
                val y = (bounds.exactCenterY() + sin(angle) * ring * size).toInt().coerceIn(0, height - 1)
                points += x to y
            }
        }
        return points
    }

    /** Whether this service may take screenshots at all (Android 11+ and the capability granted). */
    fun available(service: AccessibilityService): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            (service.serviceInfo?.capabilities ?: 0) and AccessibilityServiceInfo.CAPABILITY_CAN_TAKE_SCREENSHOT != 0

    /**
     * Reads the backdrop around [bounds] (screen pixels) and reports its mean luminance, or null when no still
     * could be taken. [onResult] runs on a background thread.
     */
    fun sample(service: AccessibilityService, bounds: Rect, onResult: (Double?) -> Unit) {
        if (!available(service)) {
            onResult(null)
            return
        }
        runCatching {
            service.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                executor,
                object : AccessibilityService.TakeScreenshotCallback {
                    override fun onSuccess(result: AccessibilityService.ScreenshotResult) {
                        val buffer = result.hardwareBuffer
                        val mean = runCatching {
                            val hardware = Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                            val soft = hardware?.copy(Bitmap.Config.ARGB_8888, false)
                            hardware?.recycle()
                            soft?.let { shot ->
                                val points = samplePoints(bounds, shot.width, shot.height)
                                val mean = points.map { (x, y) ->
                                    ColorUtils.calculateLuminance(shot.getPixel(x, y) or OPAQUE)
                                }.average()
                                shot.recycle()
                                mean
                            }
                        }.getOrNull()
                        buffer.close()
                        onResult(mean)
                    }

                    override fun onFailure(errorCode: Int) = onResult(null)
                },
            )
        }.onFailure { onResult(null) }
    }

    private const val OPAQUE = 0xFF000000.toInt()
}
