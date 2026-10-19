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

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.dictate.data.prompts.CustomWordList
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes

/**
 * Wörterbuch (Entwurf 3, `Woerterbuch.html`): one field to search the own words and add to them, the words
 * as chips (tap one to remove it), and the replacements, which open their own screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DictionaryTab() {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    UPageHeader(
        title = stringRes(R.string.main__tab_dictionary),
        subtitle = stringRes(R.string.main__dictionary_subtitle),
    )

    val raw by prefs.dictate.customWords.collectAsState()
    val words = remember(raw) { CustomWordList.parse(raw) }
    var query by rememberSaveable { mutableStateOf("") }
    val term = query.trim()
    val shown = if (term.isEmpty()) words else words.filter { it.contains(term, ignoreCase = true) }
    val canAdd = term.isNotEmpty() && words.none { it.equals(term, ignoreCase = true) } &&
        !term.contains(',') && !term.contains('\n')

    fun add() {
        if (!canAdd) return
        // Appended rather than rewritten, so a list kept in its own order and separators stays as it was.
        val next = if (raw.isBlank()) term else raw.trimEnd() + "\n" + term
        scope.launch { prefs.dictate.customWords.set(next) }
        query = ""
    }

    val searchLabel = stringRes(R.string.main__dictionary_search)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(Unisono.Pill)
            .background(c.sunken)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(UnisonoIcons.Search, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(searchLabel, style = Unisono.Body, color = c.textTertiary, maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = Unisono.Body.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.textPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { add() }),
            )
        }
    }

    if (canAdd) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Unisono.Pill)
                .background(c.action)
                .clickable { add() }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(UnisonoIcons.Plus, contentDescription = null, tint = c.onAction, modifier = Modifier.size(18.dp))
            Text(
                stringRes(R.string.main__dictionary_add, "word" to term),
                style = Unisono.Caption.copy(fontSize = 15.sp),
                color = c.onAction,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    var removing by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        USectionTitle(stringRes(R.string.main__dictionary_words)) {
            Text(words.size.toString(), style = Unisono.Mono, color = c.textTertiary)
        }
        when {
            words.isEmpty() -> Text(
                stringRes(R.string.main__dictionary_words_empty),
                style = Unisono.Body,
                color = c.textSecondary,
            )
            shown.isEmpty() -> Text(
                stringRes(R.string.main__dictionary_no_match),
                style = Unisono.Body,
                color = c.textSecondary,
            )
            else -> FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (word in shown) {
                    Text(
                        word,
                        modifier = Modifier
                            .clip(Unisono.Pill)
                            .background(c.raised)
                            .border(0.5.dp, c.borderSubtle, Unisono.Pill)
                            .clickable { removing = word }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = Unisono.Caption,
                        color = c.textPrimary,
                    )
                }
            }
        }
    }

    removing?.let { word ->
        AlertDialog(
            onDismissRequest = { removing = null },
            text = { Text(stringRes(R.string.main__dictionary_remove, "word" to word)) },
            confirmButton = {
                TextButton(onClick = {
                    removing = null
                    var dropped = false
                    // Only the first entry of that spelling, in case the list holds it twice on purpose.
                    val rest = words.filter { w -> if (!dropped && w == word) { dropped = true; false } else true }
                    scope.launch { prefs.dictate.customWords.set(rest.joinToString("\n")) }
                }) { Text(stringRes(R.string.action__delete)) }
            },
            dismissButton = {
                TextButton(onClick = { removing = null }) { Text(stringRes(R.string.action__cancel)) }
            },
        )
    }

    val mappings by prefs.dictate.customMappings.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            USectionTitle(stringRes(R.string.main__dictionary_mappings))
            Text(
                stringRes(R.string.main__dictionary_mappings_subtitle),
                style = Unisono.Label.copy(lineHeight = 19.sp),
                color = c.textSecondary,
            )
        }
        UCard(Modifier.clickable { navController.navigate(Routes.Settings.DictateMappings) }) {
            if (mappings.items.isEmpty()) {
                Text(
                    stringRes(R.string.main__dictionary_mappings_empty),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    style = Unisono.Body,
                    color = c.textSecondary,
                )
            }
            mappings.items.forEachIndexed { i, m ->
                if (i > 0) UDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "„${m.from}“",
                        modifier = Modifier.weight(1f),
                        style = Unisono.Body,
                        color = c.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(UnisonoIcons.Arrow, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(16.dp))
                    Text(
                        m.to,
                        modifier = Modifier.weight(1f),
                        style = Unisono.Body.copy(fontWeight = FontWeight.SemiBold),
                        color = c.textPrimary,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
