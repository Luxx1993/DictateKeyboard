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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UCheckRow
import dev.patrickgold.florisboard.app.main.UChoice
import dev.patrickgold.florisboard.app.main.UChoiceRow
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.USectionTitle
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.dictate.DictateLanguages
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes

/**
 * Which languages the user dictates in and which of them is active, in the look of Entwurf 3. The
 * selection is stored comma-separated in [prefs.dictate.inputLanguages]; catalog order is preserved and at
 * least one language always stays selected. The chosen languages stand as chips, as the own words do in
 * the dictionary; a tap takes one off, and the full list (with a search, it is over a hundred long) adds.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictateLanguagesScreen() = UnisonoPage(
    title = stringRes(R.string.dictate__languages_title),
    subtitle = stringRes(R.string.main__lang_subtitle),
) {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val scope = rememberCoroutineScope()
    val selectionRaw by prefs.dictate.inputLanguages.collectAsState()
    val selection = remember(selectionRaw) { DictateLanguages.parseSelection(selectionRaw) }
    val selectedCodes = remember(selection) { selection.map { it.code }.toSet() }
    // The active language: what is sent to the model. Selectable here so it can be changed without a
    // recording bar (issue #174 follow-up).
    val activeCode by prefs.dictate.activeInputLanguage.collectAsState()
    var openDialog by remember { mutableStateOf<String?>(null) }

    fun saveSelection(codes: Set<String>) {
        // Preserve catalog order and never allow an empty selection.
        val ordered = DictateLanguages.all.filter { it.code in codes }
            .ifEmpty { listOf(DictateLanguages.of(DictateLanguages.DETECT)) }
        scope.launch {
            prefs.dictate.inputLanguages.set(DictateLanguages.serializeSelection(ordered))
            // Keep the active language consistent: if it is no longer selected, snap to the first entry —
            // otherwise a stale "detect" would silently keep dictation on auto-detect.
            val active = prefs.dictate.activeInputLanguage.get()
            if (ordered.none { it.code == active }) {
                prefs.dictate.activeInputLanguage.set(ordered.first().code)
            }
        }
    }

    val detectLabel = stringRes(R.string.dictate__language_detect)
    fun languageLabel(code: String): String =
        if (code == DictateLanguages.DETECT) detectLabel else DictateLanguages.of(code).displayName()

    UCard {
        UChoiceRow(
            title = stringRes(R.string.dictate__languages_active_title),
            choices = selection.map { UChoice(it.code, languageLabel(it.code)) },
            selected = activeCode,
            onSelect = { code -> scope.launch { prefs.dictate.activeInputLanguage.set(code) } },
            open = openDialog == "active",
            onOpenChange = { openDialog = if (it) "active" else null },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        USectionTitle(stringRes(R.string.main__lang_mine)) {
            Text(selection.size.toString(), style = Unisono.Mono, color = c.textTertiary)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (lang in selection) {
                val removable = selection.size > 1
                Text(
                    languageLabel(lang.code),
                    modifier = Modifier
                        .clip(Unisono.Pill)
                        .background(c.raised)
                        .border(
                            if (lang.code == activeCode) 1.5.dp else 0.5.dp,
                            if (lang.code == activeCode) c.action else c.borderSubtle,
                            Unisono.Pill,
                        )
                        .clickable(enabled = removable) { saveSelection(selectedCodes - lang.code) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = Unisono.Caption,
                    color = c.textPrimary,
                )
            }
            Row(
                modifier = Modifier
                    .clip(Unisono.Pill)
                    .border(1.dp, c.borderStrong, Unisono.Pill)
                    .clickable { openDialog = "add" }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(UnisonoIcons.Plus, contentDescription = null, tint = c.textPrimary, modifier = Modifier.size(16.dp))
                Text(stringRes(R.string.main__lang_add), style = Unisono.Caption, color = c.textPrimary)
            }
        }
        if (selection.size > 1) {
            Text(stringRes(R.string.main__lang_tap_hint), style = Unisono.Label, color = c.textTertiary)
        }
    }

    if (openDialog == "add") {
        // Fixed for as long as the dialog is open: auto-detect and the user's own languages on top, then
        // everything else. A row never moves under the finger that ticked it.
        val (mine, rest) = remember {
            DictateLanguages.sortedForDisplay(DictateLanguages.all)
                .map { it.code to languageLabel(it.code) }
                .partition { (code, _) -> code == DictateLanguages.DETECT || code in selectedCodes }
        }
        var query by remember { mutableStateOf("") }
        UDialog(title = stringRes(R.string.main__lang_add), onDismiss = { openDialog = null }) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(Unisono.Pill)
                    .background(c.sunken)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(UnisonoIcons.Search, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(18.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(stringRes(R.string.main__lang_search), style = Unisono.Body, color = c.textTertiary)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = Unisono.Body.copy(color = c.textPrimary),
                        cursorBrush = SolidColor(c.textPrimary),
                    )
                }
            }
            val q = query.trim()
            val shown = (mine + rest).filter { (_, label) -> q.isEmpty() || label.contains(q, ignoreCase = true) }
            shown.forEachIndexed { i, (code, label) ->
                if (i > 0) UDivider()
                UCheckRow(
                    title = label,
                    checked = code in selectedCodes,
                    onCheckedChange = { on -> saveSelection(if (on) selectedCodes + code else selectedCodes - code) },
                )
            }
        }
    }
}
