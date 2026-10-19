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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UTextField
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.USwitchRow
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState as collectFlowAsState
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.dictate.DictateController
import dev.patrickgold.florisboard.dictate.data.history.DictateHistoryEntry
import dev.patrickgold.florisboard.dictate.data.history.DictateHistoryExporter
import dev.patrickgold.florisboard.dictate.data.history.DictateHistoryStore
import dev.patrickgold.florisboard.dictate.ui.formatHistoryDuration
import dev.patrickgold.florisboard.dictate.ui.formatHistorySize
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.compose.stringRes

/**
 * Transcription history / activity log (issue #140) in the look of Entwurf 3: a searchable list of recent
 * dictations (including failed ones whose audio can be recovered). Each row shares/copies/deletes; tapping
 * it opens a detail dialog with the full transcript, playback, audio export, share and pin. Pinned entries
 * float to the top, are marked, and survive pruning. Two opt-ins, the storage limits, the folder export and
 * "clear all" sit in one card at the top. The export, detail and limits dialogs are still in the old look.
 */
@Composable
fun DictateHistoryScreen() = UnisonoPage(title = stringRes(R.string.dictate__history_title)) {
    val context = LocalContext.current
    val prefs by FlorisPreferenceStore
    val scope = rememberCoroutineScope()

    val historyEnabled by prefs.dictate.historyEnabled.collectAsState()
    val audioRetention by prefs.dictate.historyAudioRetention.collectAsState()
    val maxEntries by prefs.dictate.historyMaxEntries.collectAsState()
    val maxAgeDays by prefs.dictate.historyMaxAgeDays.collectAsState()
    val audioBudgetMb by prefs.dictate.historyAudioBudgetMb.collectAsState()
    val entries by remember { DictateHistoryStore.flow(context) }.collectFlowAsState(initial = emptyList())

    var confirmClear by remember { mutableStateOf(false) }
    var showLimits by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    // --- Folder export (issue #379) -------------------------------------------------------------
    val exportFolderUri by prefs.dictate.historyExportFolderUri.collectAsState()
    val exportFolderName by prefs.dictate.historyExportFolderName.collectAsState()
    val exportAudio by prefs.dictate.historyExportAudio.collectAsState()
    val exportLastFailure by prefs.dictate.historyExportLastFailure.collectAsState()
    var showExport by remember { mutableStateOf(false) }
    // Consent comes before the picker, never after: the folder is chosen knowing what will land in it.
    var showExportWarning by remember { mutableStateOf(false) }
    var showHistoryOffNotice by remember { mutableStateOf(false) }
    var bulkProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var bulkResult by remember { mutableStateOf<DictateHistoryExporter.BulkResult?>(null) }
    // Re-checked whenever the folder changes: a grant can be revoked, and a restored backup brings the
    // preference to a device that never had one.
    val exportFolderWritable = remember(exportFolderUri) {
        exportFolderUri.isNotBlank() && DictateHistoryExporter.canWrite(context, Uri.parse(exportFolderUri))
    }

    val exportFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (!DictateHistoryExporter.takeGrant(context, uri)) {
            Toast.makeText(context, R.string.dictate__history_export_pick_failed, Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            // Give the old folder's grant back before taking the new one to the bank: persisted URI
            // permissions are capped per app, and a user who re-picks often would eventually run out.
            DictateHistoryExporter.releaseGrant(context, prefs.dictate.historyExportFolderUri.get(), uri.toString())
            val name = withContext(Dispatchers.IO) { DictateHistoryExporter.folderName(context, uri) }
            prefs.dictate.historyExportFolderUri.set(uri.toString())
            prefs.dictate.historyExportFolderName.set(name)
            prefs.dictate.historyExportLastFailure.set(0L)
            bulkResult = null
        }
    }
    // Only the id is kept, never the entry itself: the row is an immutable snapshot, so holding it would
    // freeze the dialog at the state it had when opened — pinning from inside it did not light up the icon
    // until the dialog was closed and reopened. Resolved against the live list on every recomposition.
    var detailEntryId by remember { mutableStateOf<Long?>(null) }

    val filtered = remember(entries, query) {
        val q = query.trim()
        // Searches the raw transcript too (issue #240): after a rewrite, what you remember saying is often
        // exactly the wording that only survives in the original.
        if (q.isEmpty()) {
            entries
        } else {
            entries.filter {
                it.text.contains(q, ignoreCase = true) || it.originalText.contains(q, ignoreCase = true)
            }
        }
    }

    val c = LocalUnisono.current

    UCard {
        USwitchRow(
            title = stringRes(R.string.dictate__history_enable_title),
            subtitle = stringRes(R.string.dictate__history_enable_summary),
            checked = historyEnabled,
            onCheckedChange = { scope.launch { prefs.dictate.historyEnabled.set(it) } },
        )
        UDivider()
        USwitchRow(
            title = stringRes(R.string.dictate__history_audio_title),
            subtitle = stringRes(R.string.dictate__history_audio_summary),
            checked = audioRetention,
            enabled = historyEnabled,
            onCheckedChange = { scope.launch { prefs.dictate.historyAudioRetention.set(it) } },
        )
        UDivider()
        // All retention caps behind one row, in a dialog with free numeric input.
        UNavRow(
            title = stringRes(R.string.dictate__history_retention_title),
            subtitle = "$maxEntries \u00b7 ${ageLabel(maxAgeDays)} \u00b7 $audioBudgetMb MB",
            onClick = { if (historyEnabled) showLimits = true },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__history_export_title),
            // Not greyed out when history is off: it opens and offers to switch history on (issue #297).
            onClick = { if (historyEnabled) showExport = true else showHistoryOffNotice = true },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__history_clear_title),
            onClick = { if (entries.isNotEmpty()) confirmClear = true },
        )
    }

    val totalAudio = entries.sumOf { it.audioBytes }
    if (totalAudio > 0L) {
        Text(
            text = stringRes(
                R.string.dictate__history_disk_usage,
                "size" to (formatHistorySize(totalAudio) ?: "0 KB"),
            ),
            style = Unisono.Label,
            color = c.textTertiary,
        )
    }

    if (entries.isNotEmpty()) {
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
                    Text(stringRes(R.string.dictate__history_search), style = Unisono.Body, color = c.textTertiary)
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
    }

    if (filtered.isEmpty()) {
        Text(
            text = stringRes(R.string.dictate__history_empty),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            style = Unisono.Body,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
    } else {
        UCard {
            filtered.forEachIndexed { index, entry ->
                if (index > 0) UDivider()
                HistoryRow(
                    entry = entry,
                    onOpen = { detailEntryId = entry.id },
                    onShare = { shareText(context, entry.text) },
                    onCopy = {
                        copyToClipboard(context, entry.text)
                        Toast.makeText(context, R.string.dictate__history_copied, Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { scope.launch { DictateHistoryStore.delete(context, entry) } },
                )
            }
        }
    }

    // Deliberately resolved from `entries` rather than the filtered list, so an active search query
    // cannot pull the dialog out from under the user. A deleted entry resolves to null and closes it.
    val detailEntry = detailEntryId?.let { id -> entries.firstOrNull { it.id == id } }
    detailEntry?.let { entry ->
        DetailDialog(
            entry = entry,
            onShare = { shareText(context, entry.text) },
            onExport = { DictateController.exportHistoryAudio(context, entry) },
            onTogglePin = { scope.launch { DictateHistoryStore.setPinned(context, entry.id, !entry.pinned) } },
            onDismiss = { detailEntryId = null },
        )
    }

    if (showLimits) {
        RetentionDialog(
            entries = maxEntries,
            ageDays = maxAgeDays,
            budgetMb = audioBudgetMb,
            onSave = { e, a, b ->
                scope.launch {
                    prefs.dictate.historyMaxEntries.set(e)
                    prefs.dictate.historyMaxAgeDays.set(a)
                    prefs.dictate.historyAudioBudgetMb.set(b)
                }
                showLimits = false
            },
            onDismiss = { showLimits = false },
        )
    }

    if (showExport) {
        ExportDialog(
            folderName = exportFolderName,
            folderSet = exportFolderUri.isNotBlank(),
            folderWritable = exportFolderWritable,
            exportAudio = exportAudio,
            audioRetention = audioRetention,
            lastFailure = exportLastFailure,
            progress = bulkProgress,
            result = bulkResult,
            onPick = {
                // Already consented once; changing folders does not ask again.
                if (exportFolderUri.isBlank()) showExportWarning = true else exportFolderPicker.launch(null)
            },
            onDisconnect = {
                scope.launch {
                    DictateHistoryExporter.releaseGrant(context, prefs.dictate.historyExportFolderUri.get())
                    prefs.dictate.historyExportFolderUri.set("")
                    prefs.dictate.historyExportFolderName.set("")
                    prefs.dictate.historyExportLastFailure.set(0L)
                    bulkResult = null
                }
            },
            onToggleAudio = { scope.launch { prefs.dictate.historyExportAudio.set(it) } },
            onExportAll = {
                scope.launch {
                    bulkResult = null
                    bulkProgress = 0 to entries.size
                    bulkResult = DictateHistoryExporter.exportAll(context, entries) { done, total ->
                        bulkProgress = done to total
                    }
                    bulkProgress = null
                }
            },
            onDismiss = { showExport = false; bulkResult = null },
        )
    }

    if (showExportWarning) {
        UDialog(
            title = stringRes(R.string.dictate__history_export_warning_title),
            description = stringRes(R.string.dictate__history_export_warning_message),
            doneLabel = stringRes(android.R.string.cancel),
            onDismiss = { showExportWarning = false },
        ) {
            UNavRow(
                title = stringRes(R.string.dictate__history_export_choose),
                onClick = {
                    showExportWarning = false
                    exportFolderPicker.launch(null)
                },
            )
        }
    }

    if (showHistoryOffNotice) {
        UDialog(
            title = stringRes(R.string.dictate__history_export_title),
            description = stringRes(R.string.dictate__history_export_needs_history),
            doneLabel = stringRes(android.R.string.cancel),
            onDismiss = { showHistoryOffNotice = false },
        ) {
            UNavRow(
                title = stringRes(R.string.dictate__history_export_enable_history),
                onClick = {
                    scope.launch { prefs.dictate.historyEnabled.set(true) }
                    showHistoryOffNotice = false
                    showExport = true
                },
            )
        }
    }

    if (confirmClear) {
        UDialog(
            title = stringRes(R.string.dictate__history_clear_title),
            description = stringRes(R.string.dictate__history_clear_message),
            doneLabel = stringRes(android.R.string.cancel),
            onDismiss = { confirmClear = false },
        ) {
            UNavRow(
                title = stringRes(R.string.dictate__history_clear_confirm),
                onClick = {
                    scope.launch { DictateHistoryStore.clearAll(context) }
                    confirmClear = false
                },
            )
        }
    }
}

@Composable
private fun ageLabel(days: Int): String =
    if (days <= 0) stringRes(R.string.dictate__history_age_never)
    else stringRes(R.string.dictate__history_age_days, "days" to days.toString())

/**
 * Everything the folder export (issue #379) needs, behind one row: which folder, whether the audio goes
 * with it, and the one-off pass that fills the folder.
 *
 * A dialog rather than rows in the screen, because this is set once and then never touched again.
 */
@Composable
private fun ExportDialog(
    folderName: String,
    folderSet: Boolean,
    folderWritable: Boolean,
    exportAudio: Boolean,
    audioRetention: Boolean,
    lastFailure: Long,
    progress: Pair<Int, Int>?,
    result: DictateHistoryExporter.BulkResult?,
    onPick: () -> Unit,
    onDisconnect: () -> Unit,
    onToggleAudio: (Boolean) -> Unit,
    onExportAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalUnisono.current
    UDialog(
        title = stringRes(R.string.dictate__history_export_title),
        description = stringRes(R.string.dictate__history_export_summary),
        onDismiss = onDismiss,
    ) {
        UNavRow(
            title = stringRes(R.string.dictate__history_export_folder),
            subtitle = when {
                !folderSet -> stringRes(R.string.dictate__history_export_folder_unset)
                !folderWritable -> stringRes(R.string.dictate__history_export_folder_unavailable)
                else -> folderName
            },
            onClick = onPick,
        )
        if (folderSet) {
            UDivider()
            UNavRow(title = stringRes(R.string.dictate__history_export_disconnect), onClick = onDisconnect)
            UDivider()
            USwitchRow(
                title = stringRes(R.string.dictate__history_export_audio_title),
                // Says why it is off rather than silently doing nothing: without retention the recording is
                // deleted the moment it has been transcribed, so there is no file to put beside the transcript.
                subtitle = stringRes(
                    if (audioRetention) {
                        R.string.dictate__history_export_audio_summary
                    } else {
                        R.string.dictate__history_export_audio_needs_retention
                    },
                ),
                checked = exportAudio && audioRetention,
                enabled = audioRetention,
                onCheckedChange = onToggleAudio,
            )
            UDivider()
            if (progress != null) {
                val (done, total) = progress
                Text(
                    text = "$done / $total",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    style = Unisono.Mono,
                    color = c.textPrimary,
                )
            } else {
                UNavRow(
                    title = stringRes(R.string.dictate__history_export_all),
                    subtitle = stringRes(R.string.dictate__history_export_all_summary),
                    onClick = { if (folderWritable) onExportAll() },
                )
            }
            result?.let {
                Text(
                    text = stringRes(
                        R.string.dictate__history_export_all_result,
                        "written" to it.written.toString(),
                        "skipped" to it.skipped.toString(),
                        "failed" to it.failed.toString(),
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                    style = Unisono.Label,
                    color = c.textSecondary,
                )
            }
            if (lastFailure > 0L) {
                Text(
                    text = stringRes(
                        R.string.dictate__history_export_last_failure,
                        "when" to DateUtils.getRelativeTimeSpanString(
                            lastFailure,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                        ).toString(),
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                    style = Unisono.Label,
                    color = HISTORY_ERROR,
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    entry: DictateHistoryEntry,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val c = LocalUnisono.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(start = 18.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.text,
                style = Unisono.Body,
                color = if (entry.failed) HISTORY_ERROR else c.textPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (entry.pinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = stringRes(R.string.emoji__history__pinned),
                        modifier = Modifier.size(12.dp),
                        tint = c.textPrimary,
                    )
                }
                Text(
                    text = DateUtils.getRelativeDateTimeString(
                        context, entry.createdAt, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0,
                    ).toString(),
                    style = Unisono.Mono,
                    color = c.textTertiary,
                )
            }
            val meta = historyRowMeta(entry)
            if (meta.isNotEmpty()) {
                Text(text = meta, style = Unisono.Mono, color = c.textTertiary)
            }
        }
        // Share and copy are disabled for a failed entry (no committed text yet) until it is re-transcribed.
        IconButton(onClick = onShare, enabled = !entry.failed, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringRes(R.string.dictate__stats_share),
                modifier = Modifier.size(20.dp),
                tint = c.textSecondary,
            )
        }
        IconButton(onClick = onCopy, enabled = !entry.failed, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringRes(R.string.dictate__history_copy),
                modifier = Modifier.size(20.dp),
                tint = c.textSecondary,
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringRes(R.string.dictate__history_delete),
                modifier = Modifier.size(20.dp),
                tint = HISTORY_ERROR,
            )
        }
    }
}

private val HISTORY_ERROR = Color(0xFFB3443A)

@Composable
private fun DetailDialog(
    entry: DictateHistoryEntry,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onTogglePin: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val c = LocalUnisono.current
    // The player itself lives in AudioPlayback.kt and is shared with the import screen (#301): the
    // lifecycle (stop on completion, release on dispose, poll while running) is the part worth having in
    // one place. The rows below stay here, because this dialog's also carry export, share and pin.
    val player = rememberAudioPlayer(entry.audioPath) {
        Toast.makeText(context, R.string.dictate__history_audio_missing, Toast.LENGTH_SHORT).show()
    }
    val playing = player.playing
    fun stop() = player.stop()

    UDialog(
        title = DateUtils.getRelativeDateTimeString(
            context, entry.createdAt, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0,
        ).toString(),
        onDismiss = { stop(); onDismiss() },
    ) {
        // When a prompt rewrote the dictation, both versions are shown (issue #240): the raw transcript is
        // otherwise unrecoverable, since re-transcribing runs the whole prompt chain again. Driven by
        // originalText rather than entry.reworded, which is only set for live prompts and would miss
        // auto-formatting and auto-apply prompts.
        val hasOriginal = entry.originalText.isNotEmpty() && entry.originalText != entry.text
        SelectionContainer {
            Column(
                Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (hasOriginal) {
                    TranscriptSection(
                        label = stringRes(R.string.dictate__history_original),
                        text = entry.originalText,
                        onCopy = { copyToClipboard(context, entry.originalText) },
                    )
                    TranscriptSection(
                        label = stringRes(R.string.dictate__history_result),
                        text = entry.text,
                        onCopy = { copyToClipboard(context, entry.text) },
                    )
                } else {
                    Text(
                        text = entry.text,
                        style = Unisono.Body,
                        color = if (entry.failed) HISTORY_ERROR else c.textPrimary,
                    )
                }
            }
        }
        val meta = historyRowMeta(entry)
        if (meta.isNotEmpty()) {
            Text(
                text = meta,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                style = Unisono.Mono,
                color = c.textTertiary,
            )
        }
        UDivider()
        if (entry.audioPath != null) {
            UNavRow(
                title = stringRes(R.string.dictate__history_play),
                subtitle = if (playing) "${(player.progress * 100).toInt()} %" else null,
                onClick = { player.toggle() },
            )
            UDivider()
            UNavRow(title = stringRes(R.string.action__export), onClick = onExport)
            UDivider()
        }
        if (!entry.failed) {
            UNavRow(title = stringRes(R.string.dictate__stats_share), onClick = onShare)
            UDivider()
        }
        UNavRow(
            title = stringRes(R.string.emoji__history__pinned),
            subtitle = if (entry.pinned) "\u2713" else null,
            onClick = onTogglePin,
        )
    }
}

/**
 * One labelled block of the detail dialog when a dictation exists in two versions (issue #240): a small
 * caption, its own copy button, and the text itself. Kept inside the dialog's single scroll container so
 * the two versions scroll together and stay comparable.
 */
@Composable
private fun TranscriptSection(
    label: String,
    text: String,
    onCopy: () -> Unit,
) {
    val c = LocalUnisono.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = Unisono.Mono, color = c.textTertiary)
            Spacer(Modifier.weight(1f))
            // Outside the text flow, so copying a whole version stays one tap even though the text itself is
            // also selectable by hand.
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringRes(R.string.dictate__legacy_action_copy),
                    modifier = Modifier.size(18.dp),
                    tint = c.textSecondary,
                )
            }
        }
        Text(text = text, style = Unisono.Body, color = c.textPrimary)
    }
}

/** The three retention limits in one dialog; closing it without the save row leaves them as they were. */
@Composable
private fun RetentionDialog(
    entries: Int,
    ageDays: Int,
    budgetMb: Int,
    onSave: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var e by remember { mutableStateOf(entries.toString()) }
    var a by remember { mutableStateOf(ageDays.toString()) }
    var b by remember { mutableStateOf(budgetMb.toString()) }
    // Reuse the translated "{days} days" string to get the localized bare day-unit word for the label.
    val dayUnit = stringRes(R.string.dictate__history_age_days, "days" to "").trim()

    UDialog(
        title = stringRes(R.string.dictate__history_retention_title),
        doneLabel = stringRes(android.R.string.cancel),
        onDismiss = onDismiss,
    ) {
        UTextField(
            label = stringRes(R.string.dictate__history_max_entries_title),
            placeholder = "",
            value = e,
            onValueChange = { e = it.filter { ch -> ch.isDigit() }.take(7) },
            keyboardType = KeyboardType.Number,
        )
        UTextField(
            label = "${stringRes(R.string.dictate__history_max_age_title)} ($dayUnit) \u00b7 0 = ${stringRes(R.string.dictate__history_age_never)}",
            placeholder = "",
            value = a,
            onValueChange = { a = it.filter { ch -> ch.isDigit() }.take(7) },
            keyboardType = KeyboardType.Number,
        )
        UTextField(
            label = "${stringRes(R.string.dictate__history_audio_budget_title)} (MB)",
            placeholder = "",
            value = b,
            onValueChange = { b = it.filter { ch -> ch.isDigit() }.take(7) },
            keyboardType = KeyboardType.Number,
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.action__save),
            onClick = {
                onSave(
                    e.toIntOrNull()?.coerceIn(1, 100_000) ?: entries,
                    a.toIntOrNull()?.coerceIn(0, 100_000) ?: ageDays,
                    b.toIntOrNull()?.coerceIn(0, 1_000_000) ?: budgetMb,
                )
            },
        )
    }
}

/** "OpenAI · gpt-4o-mini-transcribe · 0:12 · 0.4 MB" — omits empty parts. */
private fun historyRowMeta(entry: DictateHistoryEntry): String {
    val parts = ArrayList<String>(4)
    if (entry.providerName.isNotBlank()) parts.add(entry.providerName)
    if (entry.model.isNotBlank()) parts.add(entry.model)
    formatHistoryDuration(entry.durationSecs)?.let { parts.add(it) }
    formatHistorySize(entry.audioBytes)?.let { parts.add(it) }
    return parts.joinToString(" · ")
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Dictate", text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
