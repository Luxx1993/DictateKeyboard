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

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The drawn globe must be the designed one. Reference values come from docs/design/icon/diktat_icon.py
 * (`proj`, `lat_static`, `lat_anim(..., 'D')`, `width_for_depth`), so a drift between the two shows up here.
 */
class DiktatGlobeGeometryTest {

    private fun assertPoint(x: Double, y: Double, z: Double, p: DiktatGlobeGeometry.Point) {
        assertEquals(x, p.x.toDouble(), 1e-3)
        assertEquals(y, p.y.toDouble(), 1e-3)
        assertEquals(z, p.z.toDouble(), 1e-3)
    }

    @Test
    fun `the still icon matches the generator`() {
        with(DiktatGlobeGeometry) {
            assertPoint(4.5438, 28.1250, -0.2605, project(-100.0, latStatic(-100.0)))
            assertPoint(24.0, 31.7169, 0.9272, project(0.0, latStatic(0.0)))
            assertPoint(35.9565, 35.5977, 0.5884, project(37.5, latStatic(37.5)))
        }
    }

    @Test
    fun `animation D matches the generator`() {
        with(DiktatGlobeGeometry) {
            assertPoint(35.9686, 35.5453, 0.5902, project(37.5, latD(37.5, 0.0)))
            assertPoint(36.4015, 27.2189, 0.7830, project(37.5, latD(37.5, 0.3)))
        }
    }

    @Test
    fun `stroke width follows depth`() {
        assertEquals(2.476f, DiktatGlobeGeometry.widthForDepth(0.4f), 1e-3f)
        assertEquals(DiktatGlobeGeometry.W_BACK, DiktatGlobeGeometry.widthForDepth(-1f), 1e-6f)
        assertEquals(DiktatGlobeGeometry.W_FRONT, DiktatGlobeGeometry.widthForDepth(1f), 1e-6f)
    }

    /** The wave is one closed line that never leaves the globe, in every frame of the loop. */
    @Test
    fun `the wave is closed and stays on the globe`() {
        for (loop in listOf<Float?>(null, 0f, 0.13f, 0.5f, 0.87f)) {
            val points = DiktatGlobeGeometry.wave(loop)
            val first = points.first()
            val last = points.last()
            assertTrue(abs(first.x - last.x) < 1e-3 && abs(first.y - last.y) < 1e-3, "open at loop $loop")
            for (p in points) {
                val r = hypot((p.x - DiktatGlobeGeometry.C).toDouble(), (p.y - DiktatGlobeGeometry.C).toDouble())
                assertTrue(r <= DiktatGlobeGeometry.R + 1e-3, "outside the ring at loop $loop")
            }
        }
    }
}
