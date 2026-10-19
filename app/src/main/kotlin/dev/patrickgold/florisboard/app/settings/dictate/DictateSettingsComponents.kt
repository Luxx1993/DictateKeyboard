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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UChoice
import dev.patrickgold.florisboard.app.main.UChoiceOption
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UTextField
import dev.patrickgold.florisboard.dictate.DictateReasoningEffort
import dev.patrickgold.florisboard.dictate.data.prompts.DictatePromptDefaults
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes

/**
 * The shared none/predefined/custom choice for the system and style prompts, keyed by the integer
 * selection values in [DictatePromptDefaults] (0/1/2) used by the rewording engine.
 */
@Composable
internal fun promptSelectionChoices(): List<UChoice<Int>> = listOf(
    UChoice(DictatePromptDefaults.SELECTION_NONE, stringRes(R.string.dictate__prompt_selection_none)),
    UChoice(DictatePromptDefaults.SELECTION_PREDEFINED, stringRes(R.string.dictate__prompt_selection_predefined)),
    UChoice(DictatePromptDefaults.SELECTION_CUSTOM, stringRes(R.string.dictate__prompt_selection_custom)),
)

/**
 * A none/predefined/custom prompt selector: the row names the current choice, and a dialog picks one.
 * With the *predefined* option selected the dialog also shows the built-in prompt behind it (selectable,
 * so it can be copied), which is useful both to see what it sends and as a starting point for a custom
 * prompt. With [customPref] given, picking "custom" grows the dialog by a text area, and saving stores the
 * choice and the text together; closing discards both halves.
 *
 * @param infoPromptText the built-in prompt shown for the predefined option; for per-language prompts
 * the caller passes the resolved text for the active language.
 */
@Composable
internal fun PromptSelectionRow(
    pref: PreferenceData<Int>,
    title: String,
    choices: List<UChoice<Int>>,
    infoDescription: String,
    infoPromptText: String,
    modifier: Modifier = Modifier,
    customPref: PreferenceData<String>? = null,
    customPlaceholder: String = "",
) {
    val scope = rememberCoroutineScope()
    val c = LocalUnisono.current
    val value by pref.collectAsState()
    var open by remember { mutableStateOf(false) }

    UNavRow(
        title = title,
        subtitle = choices.firstOrNull { it.value == value }?.title,
        onClick = { open = true },
        modifier = modifier,
    )

    if (open) {
        var tmpValue by remember(value) { mutableStateOf(value) }
        // Read once as the dialog opens rather than collected: this is a draft the user is editing, and it
        // must not be overwritten by the store underneath it.
        var tmpCustom by remember { mutableStateOf(customPref?.get().orEmpty()) }
        UDialog(
            title = title,
            doneLabel = stringRes(R.string.action__cancel),
            onDismiss = { open = false },
        ) {
            for (choice in choices) {
                UChoiceOption(
                    title = choice.title,
                    subtitle = choice.subtitle,
                    selected = choice.value == tmpValue,
                    onClick = { tmpValue = choice.value },
                )
            }
            if (tmpValue == DictatePromptDefaults.SELECTION_PREDEFINED) {
                SelectionContainer {
                    Column(
                        Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(infoDescription, style = Unisono.Label, color = c.textSecondary)
                        Text(infoPromptText, style = Unisono.Mono, color = c.textPrimary)
                    }
                }
            }
            if (customPref != null && tmpValue == DictatePromptDefaults.SELECTION_CUSTOM) {
                UTextField(
                    label = title,
                    placeholder = customPlaceholder,
                    value = tmpCustom,
                    onValueChange = { tmpCustom = it },
                    singleLine = false,
                    minHeight = 160.dp,
                )
            }
            UDivider()
            UNavRow(
                title = stringRes(R.string.action__ok),
                onClick = {
                    scope.launch {
                        pref.set(tmpValue)
                        customPref?.set(tmpCustom.trim())
                    }
                    open = false
                },
            )
        }
    }
}

/**
 * A picker dialog for [DictateReasoningEffort] with an inline text field for the CUSTOM value
 * (issue #186). Shared by the global rewording setting and the per-prompt override. When
 * [includeUseGlobal] is set, a leading "use the global setting" option (value null) is offered.
 */
@Composable
internal fun ReasoningEffortDialog(
    initialEffort: DictateReasoningEffort?,
    initialCustom: String,
    includeUseGlobal: Boolean,
    onConfirm: (effort: DictateReasoningEffort?, custom: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(initialEffort) }
    var custom by remember { mutableStateOf(initialCustom) }
    val options = buildList {
        if (includeUseGlobal) add(null)
        add(DictateReasoningEffort.OFF)
        add(DictateReasoningEffort.MINIMAL)
        add(DictateReasoningEffort.LOW)
        add(DictateReasoningEffort.MEDIUM)
        add(DictateReasoningEffort.HIGH)
        add(DictateReasoningEffort.CUSTOM)
    }
    UDialog(
        title = stringRes(R.string.dictate__reasoning_effort_title),
        doneLabel = stringRes(R.string.action__cancel),
        onDismiss = onDismiss,
    ) {
        options.forEach { option ->
            UChoiceOption(
                title = reasoningEffortDialogLabel(option),
                subtitle = null,
                selected = selected == option,
                onClick = { selected = option },
            )
        }
        if (selected == DictateReasoningEffort.CUSTOM) {
            UTextField(
                label = stringRes(R.string.dictate__reasoning_effort_custom),
                placeholder = "minimal",
                value = custom,
                onValueChange = { custom = it },
            )
        }
        UDivider()
        UNavRow(
            title = stringRes(R.string.action__apply),
            onClick = { onConfirm(selected, custom.trim()) },
        )
    }
}

@Composable
internal fun reasoningEffortDialogLabel(effort: DictateReasoningEffort?): String = stringRes(
    when (effort) {
        null -> R.string.dictate__prompt_reasoning_default
        DictateReasoningEffort.OFF -> R.string.dictate__reasoning_effort_off
        DictateReasoningEffort.MINIMAL -> R.string.dictate__reasoning_effort_minimal
        DictateReasoningEffort.LOW -> R.string.dictate__reasoning_effort_low
        DictateReasoningEffort.MEDIUM -> R.string.dictate__reasoning_effort_medium
        DictateReasoningEffort.HIGH -> R.string.dictate__reasoning_effort_high
        DictateReasoningEffort.CUSTOM -> R.string.dictate__reasoning_effort_custom
    },
)
