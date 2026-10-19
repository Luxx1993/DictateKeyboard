/*
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes

/*
 * Rows bound to a preference, in the look of Entwurf 3. They replace JetPref's SwitchPreference,
 * TextInputPreference, ListPreference and DialogSliderPreference on the settings pages.
 */

/** A switch row for a boolean preference. */
@Composable
fun UPrefSwitch(
    pref: PreferenceData<Boolean>,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val value by pref.collectAsState()
    val scope = rememberCoroutineScope()
    USwitchRow(
        title = title,
        subtitle = subtitle,
        checked = value,
        enabled = enabled,
        onCheckedChange = { scope.launch { pref.set(it) } },
        modifier = modifier,
    )
}

/** A slider row for an integer preference; the value is stored when the finger lifts. */
@Composable
fun UPrefSlider(
    pref: PreferenceData<Int>,
    title: String,
    valueLabel: @Composable (Int) -> String,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier,
    step: Int = 1,
    enabled: Boolean = true,
    subtitle: String? = null,
) {
    val value by pref.collectAsState()
    val scope = rememberCoroutineScope()
    var sliding by remember(value) { mutableStateOf(value.toFloat()) }
    val shown = (((sliding - min) / step).roundToInt() * step + min).coerceIn(min, max)
    USliderRow(
        title = title,
        valueLabel = valueLabel(shown),
        subtitle = subtitle,
        value = sliding,
        onValueChange = { sliding = it },
        onValueChangeFinished = { scope.launch { pref.set(shown) } },
        range = min.toFloat()..max.toFloat(),
        steps = ((max - min) / step - 1).coerceAtLeast(0),
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
    )
}

/**
 * A row that shows a string preference and edits it in a dialog; the text is stored when the dialog is
 * closed. [info] is shown under the title inside the dialog. Secret values are masked on the row and in
 * the field.
 */
@Composable
fun UPrefText(
    pref: PreferenceData<String>,
    title: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isSecret: Boolean = false,
    multiline: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    info: String? = null,
    summaryProvider: ((String) -> String)? = null,
) {
    val value by pref.collectAsState()
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    val notSet = stringRes(R.string.dictate__value_not_set)
    val summary = when {
        summaryProvider != null -> summaryProvider(value)
        value.isBlank() -> notSet
        isSecret -> "\u2022".repeat(value.length.coerceAtMost(12))
        else -> value
    }
    UNavRow(
        title = title,
        subtitle = summary,
        onClick = { if (enabled) open = true },
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
    )
    if (open) {
        var text by remember { mutableStateOf(value) }
        UDialog(
            title = title,
            description = info,
            onDismiss = {
                scope.launch { pref.set(text.trim()) }
                open = false
            },
        ) {
            UTextField(
                label = title,
                placeholder = placeholder,
                value = text,
                onValueChange = { text = it },
                singleLine = !multiline,
                keyboardType = keyboardType,
                isSecret = isSecret,
            )
        }
    }
}

/** A choice row for a preference; every option is picked in a dialog and stored at once. */
@Composable
fun <T : Any> UPrefChoice(
    pref: PreferenceData<T>,
    title: String,
    choices: List<UChoice<T>>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val value by pref.collectAsState()
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    UChoiceRow(
        title = title,
        choices = choices,
        selected = value,
        onSelect = { scope.launch { pref.set(it) } },
        open = open && enabled,
        onOpenChange = { open = it },
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
    )
}

/** A small caption under a card, in the tertiary text colour. */
@Composable
fun UCaption(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 4.dp),
        style = Unisono.Label,
        color = LocalUnisono.current.textTertiary,
    )
}
