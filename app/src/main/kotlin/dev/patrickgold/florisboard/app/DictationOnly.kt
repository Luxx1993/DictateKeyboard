/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app

import dev.patrickgold.florisboard.BuildConfig

/**
 * Diktat-Edition: the app without its full keyboard. Dictation reaches other apps through the voice
 * input method, the recognition service and the floating button; the keyboard IME and its settings
 * are switched off (see `dictationOnly` in app/build.gradle.kts and `${fullKeyboard}` in the manifest).
 */
object DictationOnly {
    val enabled: Boolean = BuildConfig.DICTATION_ONLY

    /** Settings screens that only concern the full keyboard. */
    private val keyboardRoutes: Set<Any> = setOf(
        Routes.Settings.Localization,
        Routes.Settings.Theme,
        Routes.Settings.Keyboard,
        Routes.Settings.InputFeedback,
        Routes.Settings.CustomSymbols,
        Routes.Settings.Smartbar,
        Routes.Settings.SmartbarSecondActions,
        Routes.Settings.Typing,
        Routes.Settings.Dictionary,
        Routes.Settings.LearnedWords,
        Routes.Settings.Gestures,
        Routes.Settings.Clipboard,
        Routes.Settings.Media,
        Routes.Settings.PhysicalKeyboard,
        Routes.Settings.Translation,
        Routes.Settings.DictateLayout,
        Routes.Settings.DictateWear,
    )

    fun isVisible(route: Any): Boolean =
        !enabled || (route !in keyboardRoutes && route !is Routes.Settings.UserDictionary)
}
