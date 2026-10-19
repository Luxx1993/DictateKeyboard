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

import kotlin.test.Test
import kotlin.test.assertEquals

class BackdropToneTest {

    @Test
    fun clearlyLightAndClearlyDarkAreDecidedOutright() {
        assertEquals(true, BackdropTone.decide(0.9, previous = false))
        assertEquals(false, BackdropTone.decide(0.1, previous = true))
    }

    @Test
    fun midGreyKeepsTheLastVerdict() {
        assertEquals(true, BackdropTone.decide(0.5, previous = true))
        assertEquals(false, BackdropTone.decide(0.5, previous = false))
    }

    @Test
    fun midGreyWithNoHistoryFollowsTheHalfWay() {
        assertEquals(true, BackdropTone.decide(0.52, previous = null))
        assertEquals(false, BackdropTone.decide(0.46, previous = null))
    }
}
