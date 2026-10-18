/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.dictate

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UCheckRow
import dev.patrickgold.florisboard.app.main.UChoice
import dev.patrickgold.florisboard.app.main.UChoiceOption
import dev.patrickgold.florisboard.app.main.UChoiceRow
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UGroupLabel
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.USliderRow
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.dictate.audio.DictateAudioSource
import dev.patrickgold.florisboard.dictate.DictateLongformMode
import dev.patrickgold.florisboard.dictate.DictateRecordMode
import dev.patrickgold.florisboard.dictate.audio.AudioSpeedUp
import dev.patrickgold.florisboard.dictate.audio.SmartTurnModel
import dev.patrickgold.florisboard.dictate.provider.LocalModelCatalog
import dev.patrickgold.florisboard.dictate.provider.LocalModelDownloads
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes
import kotlin.math.roundToInt

/**
 * Recording and audio (issue #153), in the look of Entwurf 3: how the button records, the microphone, and
 * what happens to the audio before it is sent.
 */
@Composable
fun DictateRecordingScreen() = UnisonoPage(title = stringRes(R.string.dictate__recording_group)) {
    val prefs by FlorisPreferenceStore
    val scope = rememberCoroutineScope()
    var openDialog by remember { mutableStateOf<String?>(null) }

    UGroupLabel(stringRes(R.string.main__rec_group_start))
    UCard {
        val recordMode by prefs.dictate.recordMode.collectAsState()
        UChoiceRow(
            title = stringRes(R.string.dictate__record_mode_title),
            choices = listOf(
                UChoice(DictateRecordMode.TAP, stringRes(R.string.dictate__record_mode_tap_short), stringRes(R.string.dictate__record_mode_tap)),
                UChoice(DictateRecordMode.HOLD, stringRes(R.string.dictate__record_mode_hold_short), stringRes(R.string.dictate__record_mode_hold)),
            ),
            selected = recordMode,
            onSelect = { scope.launch { prefs.dictate.recordMode.set(it) } },
            open = openDialog == "recordMode",
            onOpenChange = { openDialog = if (it) "recordMode" else null },
            modifier = Modifier.settingsSearchAnchor("dictate__record_mode_title"),
        )
        UDivider()
        // All long-form settings behind one row that opens a single dialog (#170).
        val longformMode by prefs.dictate.longformMode.collectAsState()
        val longformSeconds by prefs.dictate.longformAutoSplitSeconds.collectAsState()
        Box(Modifier.settingsSearchAnchor("dictate__longform_title")) {
            UNavRow(
                title = stringRes(R.string.dictate__longform_title),
                subtitle = if (longformMode.isEnabled) {
                    stringRes(R.string.dictate__longform_autosplit_title) + " · " +
                        stringRes(R.string.dictate__longform_autosplit_value, "seconds" to "$longformSeconds")
                } else {
                    stringRes(R.string.dictate__longform_mode_off)
                },
                onClick = { openDialog = "longform" },
            )
        }
        UDivider()
        // One row, three states: off, stream and show it live, stream and only show the result (#345).
        val realtime by prefs.dictate.realtimeTranscription.collectAsState()
        val hidePreview by prefs.dictate.realtimeHidePreview.collectAsState()
        UChoiceRow(
            title = stringRes(R.string.dictate__realtime_title),
            description = stringRes(R.string.dictate__realtime_summary),
            choices = listOf(
                UChoice(0, stringRes(R.string.dictate__realtime_mode_off)),
                UChoice(1, stringRes(R.string.dictate__realtime_mode_live)),
                UChoice(2, stringRes(R.string.dictate__realtime_mode_at_end)),
            ),
            selected = when {
                !realtime -> 0
                hidePreview -> 2
                else -> 1
            },
            onSelect = { mode ->
                scope.launch {
                    prefs.dictate.realtimeTranscription.set(mode != 0)
                    // Only meaningful while streaming is on, but kept so the choice survives switching off.
                    if (mode != 0) prefs.dictate.realtimeHidePreview.set(mode == 2)
                }
            },
            open = openDialog == "realtime",
            onOpenChange = { openDialog = if (it) "realtime" else null },
            modifier = Modifier.settingsSearchAnchor("dictate__realtime_title"),
        )
    }

    UGroupLabel(stringRes(R.string.main__rec_group_mic))
    UCard {
        PrefSwitch(
            prefs.dictate.audioFocus, "dictate__audio_focus_title",
            R.string.dictate__audio_focus_title, R.string.dictate__audio_focus_summary,
        )
        UDivider()
        PrefSwitch(
            prefs.dictate.useBluetoothMic, "dictate__bluetooth_mic_title",
            R.string.dictate__bluetooth_mic_title, R.string.dictate__bluetooth_mic_summary,
        )
        UDivider()
        val source by prefs.dictate.audioInputSource.collectAsState()
        UChoiceRow(
            title = stringRes(R.string.dictate__audio_source_title),
            choices = listOf(
                UChoice(DictateAudioSource.DEFAULT, stringRes(R.string.dictate__audio_source_default), stringRes(R.string.dictate__audio_source_default_summary)),
                UChoice(DictateAudioSource.VOICE_RECOGNITION, stringRes(R.string.dictate__audio_source_voice_recognition), stringRes(R.string.dictate__audio_source_voice_recognition_summary)),
                UChoice(DictateAudioSource.UNPROCESSED, stringRes(R.string.dictate__audio_source_unprocessed), stringRes(R.string.dictate__audio_source_unprocessed_summary)),
                UChoice(DictateAudioSource.VOICE_COMMUNICATION, stringRes(R.string.dictate__audio_source_voice_communication), stringRes(R.string.dictate__audio_source_voice_communication_summary)),
            ),
            selected = source,
            onSelect = { scope.launch { prefs.dictate.audioInputSource.set(it) } },
            open = openDialog == "source",
            onOpenChange = { openDialog = if (it) "source" else null },
            modifier = Modifier.settingsSearchAnchor("dictate__audio_source_title"),
        )
        UDivider()
        PrefSwitch(
            prefs.dictate.keepScreenAwake, "dictate__keep_screen_awake_title",
            R.string.dictate__keep_screen_awake_title, R.string.dictate__keep_screen_awake_summary,
        )
    }

    UGroupLabel(stringRes(R.string.main__rec_group_send))
    UCard {
        PrefSwitch(
            prefs.dictate.skipSilentRecordings, "dictate__skip_silent_title",
            R.string.dictate__skip_silent_title, R.string.dictate__skip_silent_summary,
        )
        UDivider()
        PrefSwitch(
            prefs.dictate.trimSilentGaps, "dictate__trim_silent_gaps_title",
            R.string.dictate__trim_silent_gaps_title, R.string.dictate__trim_silent_gaps_summary,
        )
        UDivider()
        // Sibling of the trimmer above (#272): that one removes the pauses, this one shortens the speech
        // itself. The line under it carries the saving, and past the tested rate what it costs.
        val speed by prefs.dictate.audioSpeedUpPercent.collectAsState()
        var sliding by remember(speed) { mutableStateOf(speed.toFloat()) }
        val shown = (sliding / 5f).roundToInt() * 5
        USliderRow(
            title = stringRes(R.string.dictate__speed_up_title),
            valueLabel = speedUpValueLabel(shown),
            subtitle = when {
                shown <= AudioSpeedUp.MIN_PERCENT -> stringRes(R.string.dictate__speed_up_summary_off)
                shown >= AudioSpeedUp.CAUTION_PERCENT ->
                    stringRes(R.string.dictate__speed_up_summary_caution, "percent" to speedUpSaving(shown))
                else -> stringRes(R.string.dictate__speed_up_summary, "percent" to speedUpSaving(shown))
            },
            value = sliding,
            onValueChange = { sliding = it },
            onValueChangeFinished = { scope.launch { prefs.dictate.audioSpeedUpPercent.set(shown) } },
            range = AudioSpeedUp.MIN_PERCENT.toFloat()..AudioSpeedUp.MAX_PERCENT.toFloat(),
            steps = (AudioSpeedUp.MAX_PERCENT - AudioSpeedUp.MIN_PERCENT) / 5 - 1,
            modifier = Modifier.settingsSearchAnchor("dictate__speed_up_title"),
        )
    }

    if (openDialog == "longform") LongformDialog(onDismiss = { openDialog = null })
}

/**
 * Langdiktat (#170): off, or cut at pauses — the floating button has no "Next" button for a manual cut, so a
 * stored MANUAL shows and works as AUTO (see DictateController.isSegmentedMode). The pause length and the
 * optional Smart Turn model only matter while it is on.
 */
@Composable
private fun LongformDialog(onDismiss: () -> Unit) {
    val prefs by FlorisPreferenceStore
    val scope = rememberCoroutineScope()
    val mode by prefs.dictate.longformMode.collectAsState()
    val seconds by prefs.dictate.longformAutoSplitSeconds.collectAsState()
    val on = mode.isEnabled
    val smartTurnOn by prefs.dictate.smartTurnEnabled.collectAsState()
    val context = LocalContext.current
    val installedTick by LocalModelDownloads.installedTick.collectAsState()
    // It changes what the slider means: without Smart Turn the silence after which a segment is cut, with
    // it the maximum-pause fallback.
    val smartTurnActive = smartTurnOn && remember(installedTick) { SmartTurnModel.isModelAvailable(context) }

    UDialog(
        title = stringRes(R.string.dictate__longform_title),
        description = stringRes(R.string.dictate__longform_button_note),
        onDismiss = onDismiss,
    ) {
        UChoiceOption(
            title = stringRes(R.string.dictate__longform_mode_off),
            subtitle = stringRes(R.string.dictate__longform_mode_off_summary),
            selected = !on,
            onClick = { scope.launch { prefs.dictate.longformMode.set(DictateLongformMode.OFF) } },
        )
        UDivider()
        UChoiceOption(
            title = stringRes(R.string.dictate__longform_autosplit_title),
            subtitle = stringRes(R.string.dictate__longform_autosplit_summary),
            selected = on,
            onClick = { scope.launch { prefs.dictate.longformMode.set(DictateLongformMode.AUTO) } },
        )
        if (on) {
            UDivider()
            var sliding by remember(seconds) { mutableStateOf(seconds.toFloat()) }
            USliderRow(
                title = stringRes(
                    if (smartTurnActive) {
                        R.string.dictate__longform_autosplit_threshold_title
                    } else {
                        R.string.dictate__longform_pause_length_title
                    },
                ),
                valueLabel = stringRes(R.string.dictate__longform_autosplit_value, "seconds" to "${sliding.roundToInt()}"),
                value = sliding,
                onValueChange = { sliding = it },
                onValueChangeFinished = {
                    scope.launch { prefs.dictate.longformAutoSplitSeconds.set(sliding.roundToInt()) }
                },
                range = 2f..8f,
                steps = 5,
            )
            UDivider()
            SmartTurnRow()
        }
    }
}

/**
 * Opt-in Smart Turn v3 under the pause slider. Ticking it downloads the ~8 MB on-device model; it only turns
 * on once the download has fully succeeded. Off, auto-split falls back to the pure Silero silence timer.
 */
@Composable
private fun SmartTurnRow() {
    val prefs by FlorisPreferenceStore
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val downloads by LocalModelDownloads.state.collectAsState()
    val installedTick by LocalModelDownloads.installedTick.collectAsState()
    val enabledPref by prefs.dictate.smartTurnEnabled.collectAsState()

    val modelPresent = remember(installedTick) { SmartTurnModel.isModelAvailable(context) }
    val dl = downloads[LocalModelCatalog.SMART_TURN_ID]
    val downloading = dl != null && dl.error == null
    val failed = dl?.error != null
    val percent = dl?.percent ?: 0
    val checked = enabledPref && modelPresent
    var pendingEnable by remember { mutableStateOf(false) }

    // Only flip the pref on once the model has actually finished downloading; and if the file is ever gone
    // while the pref is on, turn it back off so activation and the tick never disagree.
    LaunchedEffect(modelPresent, pendingEnable, enabledPref) {
        if (modelPresent && pendingEnable) {
            prefs.dictate.smartTurnEnabled.set(true)
            pendingEnable = false
        }
        if (enabledPref && !modelPresent && !downloading && !pendingEnable) {
            prefs.dictate.smartTurnEnabled.set(false)
        }
    }

    UCheckRow(
        title = stringRes(R.string.dictate__smart_turn_title),
        subtitle = when {
            downloading -> stringRes(R.string.dictate__smart_turn_downloading, "percent" to "$percent")
            failed -> stringRes(R.string.dictate__smart_turn_download_failed)
            else -> stringRes(R.string.dictate__smart_turn_summary)
        },
        checked = checked || downloading,
        onCheckedChange = {
            when {
                downloading -> LocalModelDownloads.cancel(LocalModelCatalog.SMART_TURN_ID)
                checked -> scope.launch { prefs.dictate.smartTurnEnabled.set(false) }
                modelPresent -> scope.launch { prefs.dictate.smartTurnEnabled.set(true) }
                else -> {
                    if (failed) LocalModelDownloads.clearError(LocalModelCatalog.SMART_TURN_ID)
                    pendingEnable = true
                    LocalModelDownloads.start(context, LocalModelCatalog.SMART_TURN)
                }
            }
        },
    )
}
