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
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UButton
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UChip
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.dictate.DictateController
import dev.patrickgold.florisboard.dictate.data.prompts.PromptLibraryEntry
import dev.patrickgold.florisboard.dictate.data.prompts.PromptLibraryLegacyStore
import dev.patrickgold.florisboard.dictate.data.prompts.PromptLibraryManager
import dev.patrickgold.florisboard.dictate.data.prompts.PromptLibraryMarks
import dev.patrickgold.florisboard.dictate.data.prompts.PromptsDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.compose.stringRes

/**
 * Browse the community prompt library and import prompts into the user's own list (issue #105).
 *
 * The library is a static JSON file hosted on the project's own repository ([PromptLibraryCatalog]);
 * it is fetched on first open and cached on disk (see [PromptLibraryManager]), so re-opening is instant
 * and works offline. Adding a prompt writes it into the same `prompts.db` the [DictatePromptsScreen]
 * manages, so imported prompts immediately show up in the user's list and on the keyboard chips.
 */
@Composable
fun DictatePromptLibraryScreen() = UnisonoPage(title = stringRes(R.string.dictate__prompt_library_title)) {
    val c = LocalUnisono.current

    val context = LocalContext.current
    val db = remember { PromptsDatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()

    val entries = remember { mutableStateListOf<PromptLibraryEntry>() }
    // What the user currently has, derived from their prompt rows alone (issue #303): the library ids
    // the rows carry, plus name+prompt keys as the fallback for prompts imported before the LIBRARY_ID
    // column existed. Rebuilt on every (re)load and after each add — so a prompt that is gone stops
    // being marked as added, however it left the list.
    var installed by remember { mutableStateOf(PromptLibraryMarks.Installed(emptySet(), emptySet())) }
    // Full-screen spinner: only while we have nothing to show yet. The background network refresh uses
    // [refreshing] (a thin top bar) instead, so a warm cache never blanks the screen on open.
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<PromptLibraryEntry?>(null) }

    suspend fun refreshInstalled() {
        val catalog = entries.toList()
        installed = withContext(Dispatchers.IO) {
            val rows = db.getAll()
            // One-time carry-over of the pre-v6 side-store onto the rows it can still be matched to.
            // Skipped entirely on a fresh install (nothing writes there any more) and a no-op once the
            // rows carry their own ids, so the common path is a single read.
            val carried = PromptLibraryMarks.carryOver(rows, catalog, PromptLibraryLegacyStore.all(context))
            if (carried.isEmpty()) {
                PromptLibraryMarks.installedIn(rows)
            } else {
                val updated = rows.map { row ->
                    carried[row.id]?.let { id -> row.copy(libraryId = id).also { db.update(it) } } ?: row
                }
                PromptLibraryMarks.installedIn(updated)
            }
        }
    }

    fun isAdded(entry: PromptLibraryEntry): Boolean = PromptLibraryMarks.isAdded(installed, entry)

    // Always fetches from the network (used on open and by the refresh button). Keeps whatever is
    // already shown visible while it runs; only swaps the list in once a result arrives.
    suspend fun refreshFromNetwork() {
        refreshing = true
        val result = PromptLibraryManager.load(context, forceRefresh = true)
        if (result.entries.isNotEmpty() || entries.isEmpty()) {
            entries.clear()
            entries.addAll(result.entries)
        }
        refreshInstalled()
        loadError = entries.isEmpty() && result.error != null
        loading = false
        refreshing = false
    }

    // On open: paint the cached library instantly (no spinner) if we have one, then always refresh in
    // the background so the page is up to date every time it is opened.
    suspend fun openLoad() {
        val cached = PromptLibraryManager.cachedOnly(context) ?: PromptLibraryManager.bundled(context)
        if (cached != null) {
            entries.clear()
            entries.addAll(cached)
            refreshInstalled()
            loading = false
        }
        refreshFromNetwork()
    }

    fun addEntry(entry: PromptLibraryEntry) {
        scope.launch {
            // The row itself carries the library id (issue #303) — nothing to keep in sync on the side.
            withContext(Dispatchers.IO) { db.add(entry.toPromptModel(db.count())) }
            refreshInstalled()
            DictateController.refreshPrompts(context)
            Toast.makeText(context, R.string.dictate__prompt_library_added, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) { openLoad() }

    val categories = remember(entries.toList()) {
        entries.mapNotNull { it.category?.takeIf { c -> c.isNotBlank() } }.distinct().sorted()
    }
    val filtered = remember(entries.toList(), query, activeCategory) {
        val q = query.trim().lowercase()
        entries.filter { e ->
            (activeCategory == null || e.category == activeCategory) &&
                (q.isEmpty() ||
                    e.name.lowercase().contains(q) ||
                    e.description?.lowercase()?.contains(q) == true ||
                    e.prompt.lowercase().contains(q))
        }
    }

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
                Text(stringRes(R.string.dictate__prompt_library_search), style = Unisono.Body, color = c.textTertiary)
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

    if (categories.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UChip(stringRes(R.string.dictate__prompt_library_all), activeCategory == null) { activeCategory = null }
            categories.forEach { category ->
                UChip(category, activeCategory == category) {
                    activeCategory = if (activeCategory == category) null else category
                }
            }
        }
    }

    when {
        loading -> Text(
            "\u2026",
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            style = Unisono.SectionTitle,
            color = c.textTertiary,
            textAlign = TextAlign.Center,
        )
        loadError -> UCard {
            Text(
                text = stringRes(R.string.dictate__prompt_library_error),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                style = Unisono.Body,
                color = c.textSecondary,
            )
            UDivider()
            UNavRow(
                title = stringRes(R.string.dictate__prompt_library_retry),
                onClick = { scope.launch { refreshFromNetwork() } },
            )
        }
        filtered.isEmpty() -> Text(
            text = stringRes(R.string.dictate__prompt_library_empty),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            style = Unisono.Body,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        else -> UCard {
            filtered.forEachIndexed { index, entry ->
                if (index > 0) UDivider()
                LibraryRow(
                    entry = entry,
                    added = isAdded(entry),
                    onClick = { preview = entry },
                    onAdd = { addEntry(entry) },
                )
            }
        }
    }

    // The background refresh over already-shown content; also the way to pull a fresh copy by hand.
    UButton(
        text = stringRes(R.string.dictate__prompt_library_retry),
        primary = false,
        enabled = !refreshing,
        modifier = Modifier.fillMaxWidth(),
        onClick = { scope.launch { refreshFromNetwork() } },
    )

    val current = preview
    if (current != null) {
        PreviewDialog(
            entry = current,
            added = isAdded(current),
            onDismiss = { preview = null },
            onAdd = {
                addEntry(current)
                preview = null
            },
        )
    }
}

@Composable
private fun LibraryRow(
    entry: PromptLibraryEntry,
    added: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit,
) {
    val c = LocalUnisono.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 18.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Title with the category beside it, then the author, then the short description (falling back to
        // the raw prompt).
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(entry.name, style = Unisono.BodyStrong, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val meta = listOfNotNull(
                entry.category?.takeIf { it.isNotBlank() },
                entry.author?.takeIf { it.isNotBlank() }?.let {
                    stringRes(R.string.dictate__prompt_library_author, "author" to it)
                },
            ).joinToString(" \u00b7 ")
            if (meta.isNotEmpty()) Text(meta, style = Unisono.Mono, color = c.textTertiary)
            Text(
                text = entry.description?.takeIf { it.isNotBlank() } ?: entry.prompt,
                style = Unisono.Label,
                color = c.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (added) {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = Icons.Default.Check,
                contentDescription = stringRes(R.string.dictate__prompt_library_added),
                tint = c.success,
            )
        } else {
            Text(
                stringRes(R.string.dictate__prompt_library_add),
                modifier = Modifier
                    .clip(Unisono.Pill)
                    .border(1.dp, c.borderStrong, Unisono.Pill)
                    .clickable(onClick = onAdd)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = Unisono.Caption,
                color = c.textPrimary,
            )
        }
    }
}

/** Closing the dialog is "Fertig"; adding is its own row. Live even when the prompt is already in the list. */
@Composable
private fun PreviewDialog(
    entry: PromptLibraryEntry,
    added: Boolean,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
) {
    val c = LocalUnisono.current
    UDialog(title = entry.name, onDismiss = onDismiss) {
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val flags = listOfNotNull(
                entry.category?.takeIf { it.isNotBlank() },
                if (entry.requiresSelection) stringRes(R.string.dictate__prompt_badge_selection) else null,
                if (entry.autoApply) stringRes(R.string.dictate__prompt_badge_auto) else null,
            ).joinToString(" \u00b7 ")
            if (flags.isNotEmpty()) Text(flags, style = Unisono.Mono, color = c.textTertiary)
            if (!entry.description.isNullOrBlank()) {
                Text(entry.description, style = Unisono.Body, color = c.textPrimary)
            }
            Text(entry.prompt, style = Unisono.Mono, color = c.textSecondary)
            if (!entry.author.isNullOrBlank()) {
                Text(
                    stringRes(R.string.dictate__prompt_library_author, "author" to entry.author),
                    style = Unisono.Label,
                    color = c.textTertiary,
                )
            }
        }
        UDivider()
        UNavRow(
            title = stringRes(
                if (added) R.string.dictate__prompt_library_add_again else R.string.dictate__prompt_library_add,
            ),
            onClick = onAdd,
        )
    }
}
