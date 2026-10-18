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

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The cloud has to move like the prototype in docs/design/entwurf-3/SchwebenderButton.html. The expected
 * values are worked out by hand from its CSS: the `.sw` layer reaches 25 % past the sphere, and a blob's
 * translate is a share of its own size.
 */
class CloudMotionTest {

    private val a = CloudMotion.BLOBS[0]

    @Test
    fun `blob a at rest sits where the CSS puts it`() {
        val b = CloudMotion.blobAt(a, 0f)
        // left 8 % + width 60 % / 2 = 38 % of the layer → 0.38 * 1.5 - 0.25
        assertEquals(0.32f, b.cx, 1e-4f)
        assertEquals(0.37f * 1.5f - 0.25f, b.cy, 1e-4f)
        assertEquals(0.45f, b.rx, 1e-4f)
        assertEquals(0f, b.rotation, 1e-6f)
    }

    @Test
    fun `blob a at its 50 percent keyframe`() {
        val b = CloudMotion.blobAt(a, 0.5f)
        // translate(12 %, 8 %) of 60 % × 58 %, scale 1.1, rotate 25°
        assertEquals((0.38f + 0.12f * 0.60f) * 1.5f - 0.25f, b.cx, 1e-4f)
        assertEquals((0.37f + 0.08f * 0.58f) * 1.5f - 0.25f, b.cy, 1e-4f)
        assertEquals(0.30f * 1.1f * 1.5f, b.rx, 1e-4f)
        assertEquals(Math.toRadians(25.0).toFloat(), b.rotation, 1e-4f)
    }

    @Test
    fun `alternate runs the keyframes back`() {
        val forth = CloudMotion.blobAt(a, 0.3f)
        val back = CloudMotion.blobAt(a, 1.7f)
        assertEquals(forth.cx, back.cx, 1e-5f)
        assertEquals(forth.cy, back.cy, 1e-5f)
        assertEquals(forth.rotation, back.rotation, 1e-5f)
    }

    @Test
    fun `ease-in-out is the CSS curve`() {
        assertEquals(0.5f, CloudMotion.easeInOut(0.5f), 1e-4f)
        // cubic-bezier(0.42, 0, 0.58, 1) at x = 0.25 gives about 0.129
        assertEquals(0.129f, CloudMotion.easeInOut(0.25f), 2e-3f)
    }

    @Test
    fun `the breath peaks at 2 percent half way`() {
        assertEquals(1f, CloudMotion.breath(0f), 1e-5f)
        assertEquals(1.02f, CloudMotion.breath(4.5f), 1e-4f)
    }

    @Test
    fun `stored colours map to palettes`() {
        assertEquals(CloudPalette.SUNSET.colors, CloudPalette.forColor(CloudPalette.SUNSET.main))
        // Dictate's old default light blue reads as the standard palette.
        assertEquals(CloudPalette.LAGOON.colors, CloudPalette.forColor(0xFF30B7E6.toInt()))
        // Any other colour gets a palette mixed from it, with that colour as the main blob.
        assertEquals(0xFF123456.toInt(), CloudPalette.forColor(0xFF123456.toInt()).o1)
    }
}
