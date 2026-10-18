/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The choice that replaced the "Show with Dictate keyboard" switch (issue #439). The migration is the part
 * that must not go wrong quietly: a wrong answer here changes someone's setting without them touching it.
 */
class DictateFloatingButtonShowWhenTest {

    @Test
    fun `a removed keyboard option falls back to what the button has always done`() {
        assertEquals(
            DictateFloatingButtonShowWhen.FIELD_SELECTED,
            DictateFloatingButtonShowWhen.fromRemovedKeyboardOption(),
        )
    }

    @Test
    fun `only keyboard open needs a keyboard`() {
        assertFalse(DictateFloatingButtonShowWhen.FIELD_SELECTED.keyboardRequired)
        assertTrue(DictateFloatingButtonShowWhen.KEYBOARD_OPEN.keyboardRequired)
    }
}
