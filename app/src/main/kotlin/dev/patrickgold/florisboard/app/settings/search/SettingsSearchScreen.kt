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

import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UChip
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UGroupLabel
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.datastore.ui.Preference
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.FlorisIconButton
import org.florisboard.lib.compose.stringRes

/** Title resources shown as quick "Popular" shortcuts when the search box is still empty. */
private val POPULAR_TITLES = setOf(
    R.string.dictate__rewording_title,
    R.string.dictate__providers_title,
    R.string.dictate__realtime_title,
    R.string.settings__theme__title,
    R.string.settings__typing__title,
    R.string.pref__glide__enabled__label,
)

/**
 * A search across every indexed settings entry (issue #187). Type to filter by localized title,
 * section or keywords; tap a result to jump to its screen — and, when the entry is anchored, scroll to
 * and highlight the exact row. Recent searches and a small "Popular" shortlist are shown while empty.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSearchScreen() = UnisonoPage(title = stringRes(R.string.settings__search__title)) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val prefs by FlorisPreferenceStore
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val c = LocalUnisono.current

    var query by rememberSaveable { mutableStateOf("") }
    val historyRaw by prefs.internal.settingsSearchHistory.collectAsState()
    val recent = remember(historyRaw) {
        historyRaw.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    }

    // The search field (autofocused on first entry).
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (query.isEmpty()) focus.requestFocus() }
    Row(
        modifier = Modifier
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
                Text(stringRes(R.string.settings__search__placeholder), style = Unisono.Body, color = c.textTertiary)
            }
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                singleLine = true,
                textStyle = Unisono.Body.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.textPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    scope.launch { SettingsSearchHistory.add(prefs, query) }
                    keyboardController?.hide()
                }),
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = c.textSecondary,
                modifier = Modifier.size(20.dp).clickable { query = "" },
            )
        }
    }

    val q = query.trim()
    val results = remember(q) {
        if (q.isEmpty()) {
            emptyList()
        } else {
            SettingsSearchIndex.entries.filter { e ->
                (context.getString(e.titleRes).contains(q, ignoreCase = true) ||
                    context.getString(e.sectionRes).contains(q, ignoreCase = true) ||
                    (e.parentRes != null && context.getString(e.parentRes).contains(q, ignoreCase = true)) ||
                    // Synonyms for a setting whose own name nobody would guess (issue #333). Matched, never
                    // shown: the row still reads as the setting's real name.
                    (e.keywordsRes != null && context.getString(e.keywordsRes).contains(q, ignoreCase = true)))
            }
                // Cluster the hits under their top-level section (the breadcrumb root).
                .sortedBy { context.getString(it.parentRes ?: it.sectionRes) }
        }
    }

    fun open(entry: SettingsSearchEntry) {
        // Retract the soft keyboard before navigating, so the destination screen doesn't briefly show with
        // the keyboard still up.
        keyboardController?.hide()
        scope.launch { SettingsSearchHistory.add(prefs, query) }
        SettingsSearchState.requestAnchor(entry.anchor)
        navController.navigate(entry.route)
    }

    if (q.isEmpty()) {
        // Recent searches as chips.
        if (recent.isNotEmpty()) {
            UGroupLabel(stringRes(R.string.settings__search__recent))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                recent.forEach { term -> UChip(term, selected = false, onClick = { query = term }) }
            }
            Text(
                text = stringRes(R.string.settings__search__clear_recent),
                style = Unisono.Caption,
                color = c.textSecondary,
                modifier = Modifier
                    .clickable { scope.launch { SettingsSearchHistory.clear(prefs) } }
                    .padding(vertical = 4.dp),
            )
        }
        // Popular shortcuts.
        val popular = SettingsSearchIndex.entries.filter { it.titleRes in POPULAR_TITLES }
        if (popular.isNotEmpty()) {
            UGroupLabel(stringRes(R.string.settings__search__popular))
            UCard {
                popular.forEachIndexed { index, entry ->
                    if (index > 0) UDivider()
                    ResultRow(entry, onClick = { open(entry) })
                }
            }
        }
    } else if (results.isEmpty()) {
        Text(
            text = stringRes(R.string.settings__search__no_results),
            style = Unisono.Body,
            color = c.textSecondary,
            modifier = Modifier.padding(24.dp),
        )
    } else {
        // Group results under their top-level section (the breadcrumb root).
        results.groupBy { it.parentRes ?: it.sectionRes }.forEach { (group, entries) ->
            UGroupLabel(stringRes(group))
            UCard {
                entries.forEachIndexed { index, entry ->
                    if (index > 0) UDivider()
                    ResultRow(entry, onClick = { open(entry) })
                }
            }
        }
    }
}

@Composable
private fun ResultRow(entry: SettingsSearchEntry, onClick: () -> Unit) {
    val section = stringRes(entry.sectionRes)
    val breadcrumb = entry.parentRes?.let { "${stringRes(it)} \u203a $section" } ?: section
    UNavRow(
        title = stringRes(entry.titleRes),
        subtitle = breadcrumb,
        onClick = onClick,
    )
}
