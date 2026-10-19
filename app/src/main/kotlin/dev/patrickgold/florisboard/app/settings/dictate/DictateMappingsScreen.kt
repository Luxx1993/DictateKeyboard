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

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.USwitchRow
import dev.patrickgold.florisboard.app.main.UTextField
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.dictate.data.mappings.DictateMappings
import dev.patrickgold.florisboard.dictate.data.mappings.DictateMappingsText
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.android.showLongToast
import org.florisboard.lib.compose.stringRes

/**
 * Manager for the custom find-and-replace dictionary (issue #129) in the look of Entwurf 3: a list of
 * `from → to` rules applied deterministically to every finished transcript before it is inserted. Distinct
 * from "Custom words", which only biases the speech model's prompt. Each rule can match whole words only
 * and/or case-sensitively; the editor saves when it is closed.
 */
@Composable
fun DictateMappingsScreen() = UnisonoPage(title = stringRes(R.string.dictate__mappings_title)) {
    val prefs by FlorisPreferenceStore
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mappings by prefs.dictate.customMappings.collectAsState()

    // Index of the rule being edited; -1 = adding a new one; null = dialog closed.
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    // Rules read out of a file, waiting for the user to confirm what they would do (issue #389).
    var pendingImport by remember { mutableStateOf<DictateMappingsText.ImportReport?>(null) }

    fun write(updated: DictateMappings) {
        scope.launch { prefs.dictate.customMappings.set(updated) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val report = withContext(Dispatchers.IO) {
                readText(context, uri)?.let { DictateMappingsText.plan(prefs.dictate.customMappings.get(), it) }
            }
            when {
                report == null -> context.showLongToast(R.string.dictate__file_unreadable)
                report.added.isEmpty() && report.alreadyKnown == 0 ->
                    context.showLongToast(R.string.dictate__mappings_import_empty)
                else -> pendingImport = report
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/tab-separated-values"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = DictateMappingsText.format(prefs.dictate.customMappings.get().items)
            val ok = withContext(Dispatchers.IO) { writeText(context, uri, text) }
            context.showLongToast(
                if (ok) R.string.dictate__file_export_done else R.string.dictate__file_export_failed,
            )
        }
    }

    UCard {
        if (mappings.items.isEmpty()) {
            UNavRow(
                title = stringRes(R.string.dictate__mappings_empty_title),
                subtitle = stringRes(R.string.dictate__mappings_empty_summary),
                onClick = { editingIndex = -1 },
                modifier = Modifier.settingsSearchAnchor("dictate__mappings_empty_title"),
            )
        } else {
            val wholeWord = stringRes(R.string.dictate__mappings_opt_wholeword)
            val substring = stringRes(R.string.dictate__mappings_opt_substring)
            val caseSensitive = stringRes(R.string.dictate__mappings_opt_casesensitive)
            val caseInsensitive = stringRes(R.string.dictate__mappings_opt_caseinsensitive)
            mappings.items.forEachIndexed { index, m ->
                if (index > 0) UDivider()
                UNavRow(
                    title = "${m.from}  \u2192  ${m.to.ifBlank { "\u2205" }}",
                    subtitle = listOf(
                        if (m.wholeWord) wholeWord else substring,
                        if (m.matchCase) caseSensitive else caseInsensitive,
                    ).joinToString(" \u00b7 "),
                    onClick = { editingIndex = index },
                )
            }
        }
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__mappings_add),
            icon = UnisonoIcons.Plus,
            onClick = { editingIndex = -1 },
            modifier = Modifier.settingsSearchAnchor("dictate__mappings_add"),
        )
    }

    UCard {
        UNavRow(
            title = stringRes(R.string.dictate__mappings_import),
            onClick = {
                // A rules file is tab-separated text; the octet-stream fallback is there because
                // plenty of SAF providers hand any unfamiliar extension over as opaque bytes.
                importLauncher.launch(arrayOf("text/*", "application/octet-stream"))
            },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__mappings_export),
            onClick = { exportLauncher.launch(context.getString(R.string.dictate__mappings_export_filename)) },
        )
    }

    pendingImport?.let { report ->
        ImportMappingsDialog(
            report = report,
            onDismiss = { pendingImport = null },
            onConfirm = {
                pendingImport = null
                write(report.merged)
                scope.launch {
                    context.showLongToast(
                        R.string.dictate__mappings_import_done,
                        "count" to report.added.size,
                    )
                }
            },
        )
    }

    editingIndex?.let { idx ->
        val existing = mappings.items.getOrNull(idx)
        MappingEditorDialog(
            mapping = existing ?: DictateMappings.Mapping(from = "", to = ""),
            isNew = existing == null,
            onClose = { m ->
                // Empty "from" rules are skipped at apply time, so a blank new rule is simply not stored.
                if (m != null && (existing != null || m.from.isNotBlank())) {
                    write(if (existing != null) mappings.set(idx, m) else mappings.add(m))
                }
                editingIndex = null
            },
            onDelete = if (existing != null) {
                { write(mappings.removeAt(idx)); editingIndex = null }
            } else {
                null
            },
        )
    }
}

/** Saves when closed ([onClose] gets the edited rule); [onDelete] removes it instead. */
@Composable
private fun MappingEditorDialog(
    mapping: DictateMappings.Mapping,
    isNew: Boolean,
    onClose: (DictateMappings.Mapping?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var from by remember { mutableStateOf(mapping.from) }
    var to by remember { mutableStateOf(mapping.to) }
    var matchCase by remember { mutableStateOf(mapping.matchCase) }
    var wholeWord by remember { mutableStateOf(mapping.wholeWord) }

    UDialog(
        title = stringRes(R.string.dictate__mappings_editor_title),
        onDismiss = {
            onClose(mapping.copy(from = from.trim(), to = to, matchCase = matchCase, wholeWord = wholeWord))
        },
    ) {
        UTextField(
            label = stringRes(R.string.dictate__mappings_field_from),
            placeholder = stringRes(R.string.dictate__mappings_field_from_placeholder),
            value = from,
            onValueChange = { from = it },
        )
        UTextField(
            label = stringRes(R.string.dictate__mappings_field_to),
            placeholder = stringRes(R.string.dictate__mappings_field_to_placeholder),
            value = to,
            onValueChange = { to = it },
        )
        UDivider()
        USwitchRow(
            title = stringRes(R.string.dictate__mappings_wholeword_title),
            subtitle = stringRes(R.string.dictate__mappings_wholeword_summary),
            checked = wholeWord,
            onCheckedChange = { wholeWord = it },
        )
        UDivider()
        USwitchRow(
            title = stringRes(R.string.dictate__mappings_matchcase_title),
            subtitle = stringRes(R.string.dictate__mappings_matchcase_summary),
            checked = matchCase,
            onCheckedChange = { matchCase = it },
        )
        if (onDelete != null && !isNew) {
            UDivider()
            UNavRow(title = stringRes(R.string.action__delete), onClick = onDelete)
        }
    }
}

/**
 * What an imported rules file would do, before it does it. A file can carry hundreds of rules that rewrite
 * every transcript from here on, and "undo" for that is deleting them one at a time, so it asks first;
 * closing the dialog cancels.
 */
@Composable
private fun ImportMappingsDialog(
    report: DictateMappingsText.ImportReport,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val c = LocalUnisono.current
    UDialog(
        title = stringRes(R.string.dictate__mappings_import_title),
        onDismiss = onDismiss,
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringRes(R.string.dictate__mappings_import_message, "count" to report.added.size), style = Unisono.Body, color = c.textPrimary)
            if (report.alreadyKnown > 0) {
                Text(stringRes(R.string.dictate__mappings_import_known, "count" to report.alreadyKnown), style = Unisono.Label, color = c.textSecondary)
            }
            if (report.unusable > 0) {
                Text(stringRes(R.string.dictate__mappings_import_unusable, "count" to report.unusable), style = Unisono.Label, color = c.textSecondary)
            }
            if (report.added.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = report.added.take(6).joinToString("\n") { "${it.from}  \u2192  ${it.to.ifBlank { "\u2205" }}" } +
                        if (report.added.size > 6) "\n\u2026" else "",
                    style = Unisono.Mono,
                    color = c.textPrimary,
                )
            }
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = stringRes(R.string.dictate__mappings_import_format),
                style = Unisono.Label,
                color = c.textTertiary,
            )
        }
        UDivider()
        UNavRow(title = stringRes(R.string.action__add), onClick = onConfirm)
    }
}
