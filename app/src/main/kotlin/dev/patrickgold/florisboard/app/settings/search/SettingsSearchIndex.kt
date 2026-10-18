/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.search

import androidx.annotation.StringRes
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.Routes

/**
 * One searchable settings entry (issue #187). GENERATED from the settings screens by
 * scripts/gen_settings_search_index — do not curate by hand; rerun the generator when settings
 * change so the search stays complete. Each entry maps a localized [titleRes] to the [route]
 * that shows it, with [sectionRes] (the screen) and optional [parentRes] forming the breadcrumb.
 * When [anchor] is set the destination row is tagged with Modifier.settingsSearchAnchor and the
 * result scrolls to + highlights it; otherwise it lands on the screen.
 *
 * [keywordsRes] is for the handful of settings whose own name is not what anyone searches for. It is
 * matched like the title but never displayed, so the result row still reads as the setting's real
 * name. Set it only by hand — the generator cannot guess synonyms.
 */
data class SettingsSearchEntry(
    @StringRes val titleRes: Int,
    @StringRes val sectionRes: Int,
    val route: Any,
    @StringRes val parentRes: Int? = null,
    val anchor: String? = null,
    @StringRes val keywordsRes: Int? = null,
)

object SettingsSearchIndex {
    val entries: List<SettingsSearchEntry> = listOf(
        SettingsSearchEntry(R.string.about__title, R.string.about__title, Routes.Settings.About),
        SettingsSearchEntry(R.string.backup_and_restore__back_up__title, R.string.backup_and_restore__back_up__title, Routes.Settings.Backup, parentRes = R.string.settings__other__title),
        SettingsSearchEntry(R.string.settings__other__title, R.string.settings__other__title, Routes.Settings.Other),
        SettingsSearchEntry(R.string.backup_and_restore__restore__title, R.string.backup_and_restore__restore__title, Routes.Settings.Restore, parentRes = R.string.settings__other__title),
        SettingsSearchEntry(R.string.dictate__floating_button_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__history_title, R.string.dictate__history_title, Routes.Settings.DictateHistory, parentRes = R.string.dictate__title),
        // Hand-added: the generator only sees rows that navigate to a route, and this one opens the
        // system file picker instead (issue #301).
        SettingsSearchEntry(R.string.dictate__import_menu, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__import_menu"),
        SettingsSearchEntry(R.string.dictate__languages_title, R.string.dictate__languages_title, Routes.Settings.DictateLanguages, parentRes = R.string.dictate__title),
        // Hand-added: the languages screen is built from plain list items, not jetpref rows, so the
        // generator cannot see this switch (issue #431).
        SettingsSearchEntry(R.string.dictate__languages_follow_keyboard, R.string.dictate__languages_title, Routes.Settings.DictateLanguages, parentRes = R.string.dictate__title, anchor = "dictate__languages_follow_keyboard"),
        SettingsSearchEntry(R.string.dictate__mappings_title, R.string.dictate__mappings_title, Routes.Settings.DictateMappings, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__prompt_library_title, R.string.dictate__prompt_library_title, Routes.Settings.DictatePromptLibrary, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__providers_title, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__rewording_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__title, R.string.dictate__title, Routes.Settings.Dictate),
        SettingsSearchEntry(R.string.dictate__formatting_title, R.string.dictate__formatting_title, Routes.Settings.DictateFormatting, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__recording_group, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__output_group, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__stats_title, R.string.dictate__stats_title, Routes.Settings.DictateStats, parentRes = R.string.dictate__title),
        // Hand-added (issue #385): the generator has no entry for this screen file, and running it
        // would drop the keywordsRes arguments below along with every other hand-added row.
        SettingsSearchEntry(R.string.about__version__title, R.string.about__title, Routes.Settings.About, anchor = "about__version__title"),
        SettingsSearchEntry(R.string.about__website__title, R.string.about__title, Routes.Settings.About, anchor = "about__website__title"),
        SettingsSearchEntry(R.string.about__changelog__title, R.string.about__title, Routes.Settings.About, anchor = "about__changelog__title"),
        SettingsSearchEntry(R.string.about__repository__title, R.string.about__title, Routes.Settings.About, anchor = "about__repository__title"),
        SettingsSearchEntry(R.string.about__based_on_floris__title, R.string.about__title, Routes.Settings.About, anchor = "about__based_on_floris__title"),
        SettingsSearchEntry(R.string.about__feedback__title, R.string.about__title, Routes.Settings.About, anchor = "about__feedback__title"),
        SettingsSearchEntry(R.string.about__privacy_policy__title, R.string.about__title, Routes.Settings.About, anchor = "about__privacy_policy__title"),
        SettingsSearchEntry(R.string.about__project_license__title, R.string.about__title, Routes.Settings.About, anchor = "about__project_license__title"),
        SettingsSearchEntry(R.string.about__data_attributions__title, R.string.about__title, Routes.Settings.About, anchor = "about__data_attributions__title"),
        SettingsSearchEntry(R.string.pref__other__settings_theme__label, R.string.settings__other__title, Routes.Settings.Other, anchor = "pref__other__settings_theme__label"),
        SettingsSearchEntry(R.string.pref__other__settings_accent_color__label, R.string.settings__other__title, Routes.Settings.Other, anchor = "pref__other__settings_accent_color__label"),
        SettingsSearchEntry(R.string.pref__other__settings_language__label, R.string.settings__other__title, Routes.Settings.Other, anchor = "pref__other__settings_language__label"),
        SettingsSearchEntry(R.string.pref__other__show_app_icon__label, R.string.settings__other__title, Routes.Settings.Other, anchor = "pref__other__show_app_icon__label"),
        SettingsSearchEntry(R.string.physical_keyboard__title, R.string.settings__other__title, Routes.Settings.Other, anchor = "physical_keyboard__title"),
        SettingsSearchEntry(R.string.pref__other__clear_cache__label, R.string.settings__other__title, Routes.Settings.Other, anchor = "pref__other__clear_cache__label",
            keywordsRes = R.string.settings__search__keywords__clear_cache),
        SettingsSearchEntry(R.string.dictate__request_timeout_title, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title, anchor = "dictate__request_timeout_title",
            keywordsRes = R.string.settings__search__keywords__request_timeout),
        SettingsSearchEntry(R.string.backup_and_restore__title, R.string.settings__other__title, Routes.Settings.Other),
        SettingsSearchEntry(R.string.backup_and_restore__back_up__title, R.string.settings__other__title, Routes.Settings.Other, anchor = "backup_and_restore__back_up__title"),
        SettingsSearchEntry(R.string.backup_and_restore__restore__title, R.string.settings__other__title, Routes.Settings.Other, anchor = "backup_and_restore__restore__title"),
        // Hand-added (issue #395): a rerun of the generator no longer compiles, so new rows go in by hand.
        SettingsSearchEntry(R.string.dictate__floating_button_enable_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_enable_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_permission_group, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__floating_button_service_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_service_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_mic_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_mic_title"),
        // Hand-added (#439): replaces the "Show with Dictate keyboard" row; the generator is lossy.
        SettingsSearchEntry(R.string.dictate__floating_button_show_when_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_show_when_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_apps_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_apps_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_design_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_design_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_size_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_size_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_snap_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_snap_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_color_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_color_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_auto_dim_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_auto_dim_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_remember_position_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_remember_position_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_haptic_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_haptic_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_undo_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_undo_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_copy_to_clipboard_title, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_copy_to_clipboard_title"),
        SettingsSearchEntry(R.string.dictate__floating_button_menu_group, R.string.dictate__floating_button_title, Routes.Settings.DictateFloatingButton, parentRes = R.string.dictate__title, anchor = "dictate__floating_button_menu_group"),
        SettingsSearchEntry(R.string.dictate__history_enable_title, R.string.dictate__history_title, Routes.Settings.DictateHistory, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__history_audio_title, R.string.dictate__history_title, Routes.Settings.DictateHistory, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__history_retention_title, R.string.dictate__history_title, Routes.Settings.DictateHistory, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__history_export_title, R.string.dictate__history_title, Routes.Settings.DictateHistory, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__mappings_empty_title, R.string.dictate__mappings_title, Routes.Settings.DictateMappings, parentRes = R.string.dictate__title, anchor = "dictate__mappings_empty_title"),
        SettingsSearchEntry(R.string.dictate__mappings_add, R.string.dictate__mappings_title, Routes.Settings.DictateMappings, parentRes = R.string.dictate__title, anchor = "dictate__mappings_add"),
        SettingsSearchEntry(R.string.dictate__providers_add, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title, anchor = "dictate__providers_add"),
        SettingsSearchEntry(R.string.dictate__providers_add_custom, R.string.dictate__providers_add, Routes.Settings.DictateProvidersAdd, parentRes = R.string.dictate__providers_title, anchor = "dictate__providers_add_custom"),
        SettingsSearchEntry(R.string.dictate__providers_network_group, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_title, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title, anchor = "dictate__proxy_title"),
        SettingsSearchEntry(R.string.dictate__providers_active_rewording, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title, anchor = "dictate__providers_active_rewording"),
        SettingsSearchEntry(R.string.dictate__providers_active_transcription, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title, anchor = "dictate__providers_active_transcription"),
        // The long-press-send-local checkbox lives inside the on-device provider dialog (issue #228); no
        // scroll anchor exists for a dialog, so search just lands on the Providers screen (on-device is at
        // the top of the list).
        SettingsSearchEntry(R.string.dictate__local_longpress_send_title, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_enabled_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title, anchor = "dictate__proxy_enabled_title"),
        SettingsSearchEntry(R.string.dictate__proxy_server_group, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_type_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title, anchor = "dictate__proxy_type_title"),
        SettingsSearchEntry(R.string.dictate__proxy_host_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_port_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_auth_group, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_username_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__proxy_password_title, R.string.dictate__proxy_title, Routes.Settings.DictateProxy, parentRes = R.string.dictate__title),
        // Hand-added (issue #383): the switch lives in each account's editor dialog now, which no anchor can
        // reach, so the result opens the list of providers the dialog is opened from.
        SettingsSearchEntry(R.string.dictate__trust_user_certs_title, R.string.dictate__providers_title, Routes.Settings.DictateProviders, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__rewording_enabled_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title, anchor = "dictate__rewording_enabled_title"),
        SettingsSearchEntry(R.string.dictate__prompts_layout_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title, anchor = "dictate__prompts_layout_title"),
        SettingsSearchEntry(R.string.dictate__manage_prompts_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title, anchor = "dictate__manage_prompts_title"),
        SettingsSearchEntry(R.string.dictate__command_word_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title, anchor = "dictate__command_word_title"),
        // Hand-added (issue #283): the typing shortcut lives inside the prompt editor dialog, which the
        // generator cannot see — and it is exactly what someone looking for text expansion searches for.
        // The keywords are hand-added too (issue #333). The feature was complete and still went unfound,
        // because it is filed under "Prompts" and named "Typing shortcut": nobody looking for a text
        // expander types either of those words.
        SettingsSearchEntry(R.string.dictate__prompt_trigger_title, R.string.dictate__prompts_title, Routes.Settings.DictatePrompts(), parentRes = R.string.dictate__title,
            keywordsRes = R.string.settings__search__keywords__text_expansion),
        SettingsSearchEntry(R.string.dictate__auto_formatting_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title, anchor = "dictate__auto_formatting_title"),
        SettingsSearchEntry(R.string.dictate__reasoning_effort_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title, anchor = "dictate__reasoning_effort_title"),
        SettingsSearchEntry(R.string.dictate__system_prompt_title, R.string.dictate__rewording_title, Routes.Settings.DictateRewording, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__stats_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__stats_title"),
        SettingsSearchEntry(R.string.dictate__history_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__history_title"),
        SettingsSearchEntry(R.string.dictate__layout_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__layout_title"),
        SettingsSearchEntry(R.string.dictate__providers_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__providers_title"),
        SettingsSearchEntry(R.string.dictate__languages_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__languages_title"),
        SettingsSearchEntry(R.string.dictate__formatting_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__formatting_title"),
        SettingsSearchEntry(R.string.dictate__rewording_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__rewording_title"),
        SettingsSearchEntry(R.string.dictate__recording_group, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__recording_group"),
        SettingsSearchEntry(R.string.dictate__output_group, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__output_group"),
        SettingsSearchEntry(R.string.dictate__floating_button_enable_title, R.string.dictate__title, Routes.Settings.Dictate, anchor = "dictate__floating_button_enable_title"),
        SettingsSearchEntry(R.string.dictate__style_prompt_title, R.string.dictate__formatting_title, Routes.Settings.DictateFormatting, parentRes = R.string.dictate__title),
        SettingsSearchEntry(R.string.dictate__custom_words_title, R.string.dictate__formatting_title, Routes.Settings.DictateFormatting, parentRes = R.string.dictate__title),
        // Hand-added (issue #389); the generator is lossy, so a rerun of it must be diffed before keeping.
        // Both point at the custom-words row: importing and exporting live inside its dialog, so the
        // row is where a search for either of them has to land.
        SettingsSearchEntry(R.string.dictate__custom_words_import, R.string.dictate__formatting_title, Routes.Settings.DictateFormatting, parentRes = R.string.dictate__title, anchor = "dictate__custom_words_title"),
        SettingsSearchEntry(R.string.dictate__custom_words_export, R.string.dictate__formatting_title, Routes.Settings.DictateFormatting, parentRes = R.string.dictate__title, anchor = "dictate__custom_words_title"),
        SettingsSearchEntry(R.string.dictate__mappings_title, R.string.dictate__formatting_title, Routes.Settings.DictateFormatting, parentRes = R.string.dictate__title, anchor = "dictate__mappings_title"),
        SettingsSearchEntry(R.string.dictate__push_to_talk_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__push_to_talk_title"),
        SettingsSearchEntry(R.string.dictate__realtime_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__realtime_title"),
        SettingsSearchEntry(R.string.dictate__longform_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__longform_title"),
        SettingsSearchEntry(R.string.dictate__smart_turn_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__longform_title"),
        SettingsSearchEntry(R.string.dictate__audio_focus_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__audio_focus_title"),
        SettingsSearchEntry(R.string.dictate__bluetooth_mic_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__bluetooth_mic_title"),
        SettingsSearchEntry(R.string.dictate__audio_source_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__audio_source_title"),
        SettingsSearchEntry(R.string.dictate__keep_screen_awake_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__keep_screen_awake_title"),
        SettingsSearchEntry(R.string.dictate__recording_animation_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__recording_animation_title"),
        SettingsSearchEntry(R.string.dictate__skip_silent_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__skip_silent_title"),
        SettingsSearchEntry(R.string.dictate__trim_silent_gaps_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__trim_silent_gaps_title"),
        SettingsSearchEntry(R.string.dictate__speed_up_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__speed_up_title"),
        SettingsSearchEntry(R.string.dictate__instant_recording_title, R.string.dictate__recording_group, Routes.Settings.DictateRecording, parentRes = R.string.dictate__title, anchor = "dictate__instant_recording_title"),
        SettingsSearchEntry(R.string.dictate__auto_enter_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__auto_enter_title"),
        SettingsSearchEntry(R.string.dictate__instant_output_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__instant_output_title"),
        SettingsSearchEntry(R.string.dictate__output_speed_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__output_speed_title"),
        SettingsSearchEntry(R.string.dictate__paragraph_split_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__paragraph_split_title"),
        SettingsSearchEntry(R.string.dictate__resend_button_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__resend_button_title"),
        SettingsSearchEntry(R.string.dictate__haptic_feedback_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__haptic_feedback_title"),
        SettingsSearchEntry(R.string.dictate__remember_last_dictation_title, R.string.dictate__output_group, Routes.Settings.DictateOutput, parentRes = R.string.dictate__title, anchor = "dictate__remember_last_dictation_title"),
    )
}
