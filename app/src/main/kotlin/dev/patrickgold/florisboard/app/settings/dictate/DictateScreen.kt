/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.dictate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UPrefSlider
import dev.patrickgold.florisboard.app.main.UPrefSwitch
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Segment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import java.text.DecimalFormat
import kotlin.math.roundToInt
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.florisboard.dictate.DictateLongformMode
import dev.patrickgold.florisboard.dictate.DictateRecordMode
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import dev.patrickgold.florisboard.dictate.importer.TranscribeShareActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.dictate.DictateLanguages
import dev.patrickgold.florisboard.dictate.audio.AudioSpeedUp
import dev.patrickgold.florisboard.dictate.audio.DictateAudioSource
import dev.patrickgold.florisboard.dictate.audio.SmartTurnModel
import dev.patrickgold.florisboard.dictate.provider.LocalModelCatalog
import dev.patrickgold.florisboard.dictate.provider.LocalModelDownloads
import dev.patrickgold.florisboard.dictate.data.prompts.DictatePromptDefaults
import dev.patrickgold.florisboard.dictate.provider.ProviderAccounts
import dev.patrickgold.florisboard.dictate.provider.ProviderRegistry
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import org.florisboard.lib.compose.florisDialogScroll
import org.florisboard.lib.compose.stringRes
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.datastore.ui.DialogSliderPreference
import dev.patrickgold.jetpref.datastore.ui.PreferenceGroup
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries
import kotlinx.coroutines.launch

/**
 * Sub-screen (issue #153): input formatting & vocabulary: the style prompt, custom words appended to the
 * transcription prompt, and the deterministic find-and-replace mappings.
 */
@Composable
fun DictateFormattingScreen() = UnisonoPage(title = stringRes(R.string.dictate__formatting_title)) {
    val prefs by FlorisPreferenceStore
    val navController = LocalNavController.current
    val activeLang by prefs.dictate.activeInputLanguage.collectAsState()

    UCard {
        PromptSelectionRow(
            pref = prefs.dictate.stylePromptSelection,
            title = stringRes(R.string.dictate__style_prompt_title),
            choices = promptSelectionChoices(),
            infoDescription = stringRes(R.string.dictate__style_prompt_info_description),
            // Null for a language that has no example sentence: since issue #275 none is sent at all
            // rather than an English one, and the info box should say so instead of showing nothing.
            infoPromptText = DictatePromptDefaults.punctuationPromptFor(activeLang)
                ?: stringRes(R.string.dictate__style_prompt_info_none),
            customPref = prefs.dictate.stylePromptCustom,
            customPlaceholder = stringRes(R.string.dictate__style_prompt_custom_placeholder),
        )
        UDivider()
        // The editor, what the list costs on every request, and the file import/export (issue #389).
        CustomWordsSection(prefs.dictate.customWords)
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__mappings_title),
            subtitle = stringRes(R.string.dictate__mappings_entry_summary),
            onClick = { navController.navigate(Routes.Settings.DictateMappings) },
            modifier = Modifier.settingsSearchAnchor("dictate__mappings_title"),
        )
    }
}

/**
 * Sub-screen (issue #153): output & insertion: how the finished text is written (instant vs typed),
 * auto-enter, the resend button, and remembering the last dictation.
 */
@Composable
fun DictateOutputScreen() = UnisonoPage(title = stringRes(R.string.dictate__output_group)) {
    val prefs by FlorisPreferenceStore
    val instantOutput by prefs.dictate.instantOutput.collectAsState()

    UCard {
        UPrefSwitch(
            prefs.dictate.autoEnter,
            title = stringRes(R.string.dictate__auto_enter_title),
            subtitle = stringRes(R.string.dictate__auto_enter_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__auto_enter_title"),
        )
        UDivider()
        UPrefSwitch(
            prefs.dictate.instantOutput,
            title = stringRes(R.string.dictate__instant_output_title),
            subtitle = stringRes(R.string.dictate__instant_output_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__instant_output_title"),
        )
        UDivider()
        UPrefSlider(
            prefs.dictate.outputSpeed,
            title = stringRes(R.string.dictate__output_speed_title),
            valueLabel = { stringRes(R.string.dictate__output_speed_value, "v" to it) },
            min = 1,
            max = 10,
            enabled = !instantOutput,
            modifier = Modifier.settingsSearchAnchor("dictate__output_speed_title"),
        )
        UDivider()
        UPrefSlider(
            prefs.dictate.paragraphSplitWords,
            title = stringRes(R.string.dictate__paragraph_split_title),
            valueLabel = {
                if (it <= 0) stringRes(R.string.dictate__paragraph_split_off)
                else stringRes(R.string.dictate__paragraph_split_value, "n" to it)
            },
            min = 0,
            max = 100,
            step = 5,
            modifier = Modifier.settingsSearchAnchor("dictate__paragraph_split_title"),
        )
    }

    UCard {
        UPrefSwitch(
            prefs.dictate.resendButton,
            title = stringRes(R.string.dictate__resend_button_title),
            subtitle = stringRes(R.string.dictate__resend_button_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__resend_button_title"),
        )
        UDivider()
        UPrefSwitch(
            prefs.dictate.hapticFeedback,
            title = stringRes(R.string.dictate__haptic_feedback_title),
            subtitle = stringRes(R.string.dictate__haptic_feedback_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__haptic_feedback_title"),
        )
        UDivider()
        UPrefSwitch(
            prefs.dictate.rememberLastDictation,
            title = stringRes(R.string.dictate__remember_last_dictation_title),
            subtitle = stringRes(R.string.dictate__remember_last_dictation_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__remember_last_dictation_title"),
        )
    }
}

/**
 * The speed-up slider's value: "Off" at 1.0×, otherwise the rate itself (issue #272). Written through
 * [DecimalFormat] so the decimal mark is the reader's own — "1,5×" in German, "1.5×" in English — and so
 * the whole rates lose their pointless ".0".
 */
@Composable
internal fun speedUpValueLabel(percent: Int): String =
    if (percent <= AudioSpeedUp.MIN_PERCENT) {
        stringRes(R.string.dictate__speed_up_off)
    } else {
        stringRes(R.string.dictate__speed_up_value, "rate" to DecimalFormat("0.##").format(percent / 100.0))
    }

/** How much billed audio a given speed disposes of, as whole percent: 150 % of speed → 33 % less audio. */
internal fun speedUpSaving(percent: Int): String =
    (100.0 * (1.0 - 100.0 / percent)).roundToInt().toString()

