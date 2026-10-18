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

/**
 * When the floating dictation button comes up (issue #439).
 *
 * This used to be two switches that answered the same question from two sides: "Show with Dictate keyboard"
 * and "Only while the keyboard is open". One of their four combinations — waiting for a keyboard, and then
 * also standing over Dictate's own — was never asked for and never existed before either switch, so the
 * choice is three entries. [FIELD_SELECTED] is what the button has always done, and the default.
 */
enum class DictateFloatingButtonShowWhen {
    /**
     * As soon as an editable field holds focus, keyboard or not. This is what apps whose fields never call
     * themselves editable and hardware-keyboard users depend on. Steps aside while Dictate's own voice
     * input is on screen, which shows a microphone of its own.
     */
    FIELD_SELECTED,

    /**
     * Only while an on-screen keyboard is open — any keyboard's input-method window. A messenger that gives
     * its composer focus the moment a chat opens no longer brings the button up over a chat being read.
     * Steps aside for Dictate's own voice input like [FIELD_SELECTED].
     */
    KEYBOARD_OPEN;

    /** Whether a focused field needs an on-screen keyboard beside it before it counts. */
    val keyboardRequired: Boolean
        get() = this == KEYBOARD_OPEN

    companion object {
        /**
         * The value a stored setting from before the keyboard was removed stands for: the old
         * `dictate__floating_button_show_with_dictate_keyboard` switch, or the "also with the Dictate
         * keyboard" entry that replaced it. Both meant "as soon as a field is selected" apart from a
         * keyboard that no longer exists.
         */
        fun fromRemovedKeyboardOption(): DictateFloatingButtonShowWhen = FIELD_SELECTED
    }
}
