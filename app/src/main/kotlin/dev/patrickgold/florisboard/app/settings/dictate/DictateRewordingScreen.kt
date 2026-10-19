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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UPrefSwitch
import dev.patrickgold.florisboard.app.main.UPrefText
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.dictate.DictateReasoningEffort
import dev.patrickgold.florisboard.dictate.data.prompts.DictatePromptDefaults
import dev.patrickgold.florisboard.dictate.data.prompts.PromptsDatabaseHelper
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.compose.stringRes

/**
 * Rewording (AI) settings in the look of Entwurf 3: the master toggle, prompt management and the
 * system/auto-formatting options. The providers are configured on their own page; this page is purely
 * about the rewording side.
 */
@Composable
fun DictateRewordingScreen() = UnisonoPage(title = stringRes(R.string.dictate__rewording_title)) {
    val prefs by FlorisPreferenceStore
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rewordingEnabled by prefs.dictate.rewordingEnabled.collectAsState()

    UCard {
        UPrefSwitch(
            prefs.dictate.rewordingEnabled,
            title = stringRes(R.string.dictate__rewording_enabled_title),
            subtitle = stringRes(R.string.dictate__rewording_enabled_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__rewording_enabled_title"),
        )
        UDivider()
        val promptCount by produceState(initialValue = -1) {
            value = withContext(Dispatchers.IO) {
                PromptsDatabaseHelper.getInstance(context).count()
            }
        }
        UNavRow(
            title = stringRes(R.string.dictate__manage_prompts_title),
            subtitle = if (promptCount < 0) {
                stringRes(R.string.dictate__manage_prompts_summary_loading)
            } else {
                stringRes(R.string.dictate__manage_prompts_summary, "count" to promptCount)
            },
            onClick = { navController.navigate(Routes.Settings.DictatePrompts()) },
            modifier = Modifier.settingsSearchAnchor("dictate__manage_prompts_title"),
        )
        UDivider()
        // The spoken command word (#139): the live-prompt chip, reached by saying its name instead of
        // tapping it. Blank switches the recognition off, and that is the default: no word is rare enough
        // in everyone's speech to be safe unasked, so the user names their own.
        val commandWordOff = stringRes(R.string.dictate__command_word_off)
        UPrefText(
            prefs.dictate.commandTriggerWord,
            title = stringRes(R.string.dictate__command_word_title),
            placeholder = stringRes(R.string.dictate__command_word_placeholder),
            enabled = rewordingEnabled,
            info = stringRes(R.string.dictate__command_word_info_description),
            summaryProvider = { value ->
                if (value.isBlank()) commandWordOff else context.getString(R.string.dictate__command_word_summary, value)
            },
            modifier = Modifier.settingsSearchAnchor("dictate__command_word_title"),
        )
    }

    UCard {
        UPrefSwitch(
            prefs.dictate.autoFormattingEnabled,
            title = stringRes(R.string.dictate__auto_formatting_title),
            subtitle = stringRes(R.string.dictate__auto_formatting_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__auto_formatting_title"),
        )
        UDivider()
        val reasoningEffort by prefs.dictate.rewordingReasoningEffort.collectAsState()
        val reasoningCustom by prefs.dictate.rewordingReasoningEffortCustom.collectAsState()
        var reasoningDialogOpen by remember { mutableStateOf(false) }
        UNavRow(
            title = stringRes(R.string.dictate__reasoning_effort_title),
            subtitle = if (reasoningEffort == DictateReasoningEffort.CUSTOM) {
                reasoningCustom.ifBlank { stringRes(R.string.dictate__reasoning_effort_custom) }
            } else {
                reasoningEffortDialogLabel(reasoningEffort)
            },
            onClick = { if (rewordingEnabled) reasoningDialogOpen = true },
            modifier = Modifier.settingsSearchAnchor("dictate__reasoning_effort_title"),
        )
        if (reasoningDialogOpen) {
            ReasoningEffortDialog(
                initialEffort = reasoningEffort,
                initialCustom = reasoningCustom,
                includeUseGlobal = false,
                onConfirm = { effort, custom ->
                    scope.launch {
                        prefs.dictate.rewordingReasoningEffort.set(effort ?: DictateReasoningEffort.OFF)
                        prefs.dictate.rewordingReasoningEffortCustom.set(custom)
                    }
                    reasoningDialogOpen = false
                },
                onDismiss = { reasoningDialogOpen = false },
            )
        }
        UDivider()
        PromptSelectionRow(
            pref = prefs.dictate.systemPromptSelection,
            title = stringRes(R.string.dictate__system_prompt_title),
            choices = promptSelectionChoices(),
            infoDescription = stringRes(R.string.dictate__system_prompt_info_description),
            infoPromptText = DictatePromptDefaults.REWORDING_BE_PRECISE,
            customPref = prefs.dictate.systemPromptCustom,
            customPlaceholder = stringRes(R.string.dictate__system_prompt_custom_placeholder),
        )
    }
}
