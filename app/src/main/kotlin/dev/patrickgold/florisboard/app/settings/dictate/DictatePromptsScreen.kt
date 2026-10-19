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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UChip
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.USwitchRow
import dev.patrickgold.florisboard.app.main.UTextField
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.dictate.DictateController
import dev.patrickgold.florisboard.dictate.DictateReasoningEffort
import dev.patrickgold.florisboard.dictate.data.prompts.PromptLibraryContribution
import dev.patrickgold.florisboard.dictate.data.prompts.PromptModel
import dev.patrickgold.florisboard.dictate.data.prompts.PromptsDatabaseHelper
import dev.patrickgold.florisboard.dictate.data.prompts.promptRequiresSelection
import dev.patrickgold.florisboard.dictate.data.prompts.snippetBodyOf
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import org.florisboard.lib.compose.FlorisIconButton
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.compose.florisScrollbar
import org.florisboard.lib.compose.florisDialogScroll
import org.florisboard.lib.compose.stringRes
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manage the user's rewording prompts (roadmap 4.2 / 4.8): list, add, edit, delete, reorder by
 * drag, and JSON export/import. Persisted in the shared `prompts.db` via [PromptsDatabaseHelper] so
 * prompts created here also drive the keyboard chips (Phase 3) and the auto-apply chain. Replaces the
 * legacy `PromptsOverviewActivity` / `PromptEditActivity`; the export/import file format
 * (`{"version":1,"prompts":[…]}`) is kept byte-compatible with the legacy app so users can carry
 * their prompt collections across.
 */
@Composable
fun DictatePromptsScreen(
    // When deep-linked from a long-pressed prompt chip on the keyboard, the id of the prompt whose
    // editor should open automatically once the list has loaded (-1 = none, the normal entry;
    // PromptModel.ID_ADD_PROMPT = the editor for a new prompt).
    editPromptId: Int = -1,
) = UnisonoPage(title = stringRes(R.string.dictate__prompts_title)) {
    val c = LocalUnisono.current

    val context = LocalContext.current
    val navController = LocalNavController.current
    val db = remember { PromptsDatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()
    val prompts = remember { mutableStateListOf<PromptModel>() }
    // The prompt currently being added/edited, or null when the editor is closed. Declared here so
    // every builder lambda (actions, fab, content) shares the same state.
    var editorTarget by remember { mutableStateOf<PromptModel?>(null) }
    // Prompts parsed from an import file, awaiting the user's replace-vs-add choice.
    var pendingImport by remember { mutableStateOf<List<PromptModel>?>(null) }
    // A prompt the user chose to contribute to the community library, awaiting the "open GitHub" confirm.
    var pendingShare by remember { mutableStateOf<PromptModel?>(null) }

    fun toast(resId: Int) = Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()

    suspend fun reload() {
        val all = withContext(Dispatchers.IO) { db.getAll() }
        prompts.clear()
        prompts.addAll(all)
        // Push the change into the live keyboard's prompt flow too (same process, shared singleton),
        // so the Smartbar prompt row/panel reflects adds/edits/deletes/reorders without a reopen.
        DictateController.refreshPrompts(context)
    }

    // Persist the current in-memory order (POS = list index), preserving ids so auto-apply and the
    // keyboard chips keep working. Only rows whose position actually changed are written.
    suspend fun persistOrder() {
        withContext(Dispatchers.IO) {
            prompts.forEachIndexed { index, prompt ->
                if (prompt.pos != index) db.update(prompt.copy(pos = index))
            }
        }
        reload()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = withContext(Dispatchers.IO) { exportPrompts(context, uri, db.getAll()) }
            toast(if (ok) R.string.dictate__prompts_export_success else R.string.dictate__prompts_export_failed)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val parsed = withContext(Dispatchers.IO) { importPrompts(context, uri) }
            when {
                parsed == null -> toast(R.string.dictate__prompts_import_failed)
                parsed.isEmpty() -> toast(R.string.dictate__prompts_import_no_prompts)
                else -> pendingImport = parsed
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    // Deep-linked from a long-pressed prompt chip on the keyboard: open that prompt's editor once
    // the list has loaded. Guarded so it fires a single time (and not again after the user closes
    // the editor, even though the route arg stays put).
    var deepLinkEditHandled by remember { mutableStateOf(false) }
    LaunchedEffect(editPromptId, prompts.size) {
        if (deepLinkEditHandled) return@LaunchedEffect
        // "Neuer Stil" on the styles tab: open the editor for a new prompt straight away.
        if (editPromptId == PromptModel.ID_ADD_PROMPT) {
            editorTarget = PromptModel(-1, 0, "", "", requiresSelection = true, autoApply = false)
            deepLinkEditHandled = true
            return@LaunchedEffect
        }
        if (editPromptId < 0) return@LaunchedEffect
        val target = prompts.firstOrNull { it.id == editPromptId } ?: return@LaunchedEffect
        editorTarget = target.copy()
        deepLinkEditHandled = true
    }


    // Id of the prompt being dragged, plus its current finger offset (px) for the lift effect, and the
    // measured height of each row (the list is a plain column, so the rows report their own size).
    var draggingId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val rowHeights = remember { mutableStateMapOf<Int, Int>() }

    // Always-visible entry into the community library, pinned above the user's own prompts so it is
    // discoverable without hunting for it (issue #105).
    UCard {
        UNavRow(
            title = stringRes(R.string.dictate__prompt_library_menu),
            icon = UnisonoIcons.Cloud,
            onClick = { navController.navigate(Routes.Settings.DictatePromptLibrary) },
        )
    }

    if (prompts.isEmpty()) {
        Text(
            text = stringRes(R.string.dictate__prompts_empty),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            style = Unisono.Body,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
    } else {
        UCard {
            prompts.forEachIndexed { index, prompt ->
                key(prompt.id) {
                    if (index > 0) UDivider()
                    val isDragging = draggingId == prompt.id
                    // The whole row both edits (tap) and reorders (long-press drag); the handle is a visual
                    // affordance. The dragged row is lifted above the rest via zIndex + translation.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                            .background(if (isDragging) c.sunken else Color.Transparent)
                            .onSizeChanged { rowHeights[prompt.id] = it.height }
                            .pointerInput(prompt.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggingId = prompt.id; dragOffset = 0f },
                                    onDragEnd = {
                                        draggingId = null
                                        dragOffset = 0f
                                        scope.launch { persistOrder() }
                                    },
                                    onDragCancel = {
                                        draggingId = null
                                        dragOffset = 0f
                                        scope.launch { reload() }
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.y
                                        val curIndex = prompts.indexOfFirst { it.id == draggingId }
                                        if (curIndex < 0) return@detectDragGesturesAfterLongPress
                                        val itemHeight = rowHeights[prompt.id]
                                            ?: return@detectDragGesturesAfterLongPress
                                        // Cross the half-item threshold: swap with the neighbour and carry the
                                        // offset over so the row stays under the finger.
                                        if (dragOffset > itemHeight / 2 && curIndex < prompts.lastIndex) {
                                            prompts.add(curIndex + 1, prompts.removeAt(curIndex))
                                            dragOffset -= itemHeight
                                        } else if (dragOffset < -itemHeight / 2 && curIndex > 0) {
                                            prompts.add(curIndex - 1, prompts.removeAt(curIndex))
                                            dragOffset += itemHeight
                                        }
                                    },
                                )
                            }
                            .clickable { editorTarget = prompt.copy() }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(prompt.name.orEmpty(), style = Unisono.BodyStrong, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(prompt.prompt.orEmpty(), style = Unisono.Label, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            // Two indicators, mirroring the legacy app: "needs a selection" and "applies
                            // automatically", so the prompt's type is readable at a glance.
                            val kinds = listOfNotNull(
                                if (prompt.requiresSelection) stringRes(R.string.dictate__prompt_badge_selection) else null,
                                if (prompt.autoApply) stringRes(R.string.dictate__prompt_badge_auto) else null,
                            ).joinToString(" \u00b7 ")
                            if (kinds.isNotEmpty()) Text(kinds, style = Unisono.Mono, color = c.textTertiary)
                        }
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = c.textTertiary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    UCard {
        UNavRow(
            title = stringRes(R.string.dictate__prompt_add),
            icon = UnisonoIcons.Plus,
            onClick = {
                // -1 = a not-yet-persisted prompt; defaults mirror the legacy edit screen.
                editorTarget = PromptModel(-1, 0, "", "", requiresSelection = true, autoApply = false)
            },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__prompts_import),
            onClick = { importLauncher.launch(arrayOf("application/json")) },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__prompts_export),
            onClick = { exportLauncher.launch(context.getString(R.string.dictate__prompts_export_filename)) },
        )
    }

    val target = editorTarget
    if (target != null) {
        PromptEditorDialog(
            initial = target,
            onDismiss = { editorTarget = null },
            onShare = { name, text, requiresSelection, autoApply ->
                // No reasoning here on purpose — shared prompts stay reasoning-agnostic.
                pendingShare = PromptModel(0, 0, name, text, requiresSelection, autoApply)
            },
            onSave = { name, text, requiresSelection, autoApply, reasoning, reasoningCustom ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (target.id < 0) {
                            db.add(PromptModel(0, db.count(), name, text, requiresSelection, autoApply, reasoning, reasoningCustom))
                        } else {
                            db.update(
                                target.copy(
                                    name = name,
                                    prompt = text,
                                    requiresSelection = requiresSelection,
                                    autoApply = autoApply,
                                    reasoningEffort = reasoning,
                                    reasoningEffortCustom = reasoningCustom,
                                ),
                            )
                        }
                    }
                    reload()
                }
                editorTarget = null
            },
            onDelete = if (target.id < 0) {
                null
            } else {
                {
                    scope.launch {
                        withContext(Dispatchers.IO) { db.delete(target.id) }
                        reload()
                    }
                    editorTarget = null
                }
            },
        )
    }

    val imported = pendingImport
    if (imported != null) {
        ImportModeDialog(
            onDismiss = { pendingImport = null },
            onReplace = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.replaceAll(imported.mapIndexed { i, p -> p.copy(id = 0, pos = i) })
                    }
                    reload()
                    toast(R.string.dictate__prompts_import_success)
                }
                pendingImport = null
            },
            onAdd = {
                scope.launch {
                    val start = withContext(Dispatchers.IO) { db.count() }
                    withContext(Dispatchers.IO) {
                        db.addAll(imported.mapIndexed { i, p -> p.copy(id = 0, pos = start + i) })
                    }
                    reload()
                    toast(R.string.dictate__prompts_import_success)
                }
                pendingImport = null
            },
        )
    }

    val share = pendingShare
    if (share != null) {
        ShareConfirmDialog(
            onDismiss = { pendingShare = null },
            onConfirm = { category, description ->
                val url = PromptLibraryContribution.buildSubmissionUrl(share, category, description)
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                    )
                }.onFailure { toast(R.string.dictate__prompts_import_failed) }
                pendingShare = null
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String?, description: String?) -> Unit,
) {
    // Optional metadata the contributor supplies so the community library stays sorted and readable.
    // Category is a fixed vocabulary (matching the library); description is a free one-liner.
    var category by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    val c = LocalUnisono.current

    UDialog(
        title = stringRes(R.string.dictate__prompt_share_title),
        description = stringRes(R.string.dictate__prompt_share_message),
        doneLabel = stringRes(R.string.action__cancel),
        onDismiss = onDismiss,
    ) {
        Text(
            stringRes(R.string.dictate__prompt_share_category),
            modifier = Modifier.padding(horizontal = 18.dp),
            style = Unisono.Mono,
            color = c.textTertiary,
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PromptLibraryContribution.CATEGORIES.forEach { option ->
                UChip(option, category == option) { category = if (category == option) null else option }
            }
        }
        UTextField(
            label = stringRes(R.string.dictate__prompt_share_description),
            placeholder = stringRes(R.string.dictate__prompt_share_description_placeholder),
            value = description,
            onValueChange = { description = it },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__prompt_share_continue),
            onClick = { onConfirm(category, description.trim().ifBlank { null }) },
        )
    }
}

/** Serialises [prompts] as `{"version":1,"prompts":[…]}` (legacy-compatible). Returns success. */
private fun exportPrompts(context: android.content.Context, uri: Uri, prompts: List<PromptModel>): Boolean {
    return runCatching {
        val array = JSONArray()
        prompts.forEach { p ->
            array.put(
                JSONObject()
                    .put("name", p.name.orEmpty())
                    .put("prompt", p.prompt.orEmpty())
                    .put("requiresSelection", p.requiresSelection)
                    .put("autoApply", p.autoApply)
                    // Only written when set (null = use the global reasoning setting); issue #155.
                    .apply { p.reasoningEffort?.let { put("reasoningEffort", it.name) } }
                    .apply { p.reasoningEffortCustom?.takeIf { it.isNotBlank() }?.let { put("reasoningEffortCustom", it) } }
                    // The library origin (issue #303) travels with the user's own export, so a
                    // re-import keeps the browser's "Added" marks honest.
                    .apply { p.libraryId?.takeIf { it.isNotBlank() }?.let { put("libraryId", it) } },
            )
        }
        val root = JSONObject().put("version", 1).put("prompts", array)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        } ?: return false
        true
    }.getOrDefault(false)
}

/**
 * Reads prompts from [uri]. Accepts both the wrapped form (`{"version":…,"prompts":[…]}`) and a bare
 * top-level array. Returns null on read/parse error, or the (possibly empty) list of valid prompts.
 */
private fun importPrompts(context: android.content.Context, uri: Uri): List<PromptModel>? {
    return runCatching {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: return null
        val array = runCatching { JSONObject(json).optJSONArray("prompts") }.getOrNull()
            ?: JSONArray(json)
        val result = ArrayList<PromptModel>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val name = obj.optString("name", "")
            val prompt = obj.optString("prompt", "")
            if (name.isEmpty() || prompt.isEmpty()) continue
            result.add(
                PromptModel(
                    id = 0,
                    pos = result.size,
                    name = name,
                    prompt = prompt,
                    requiresSelection = obj.optBoolean("requiresSelection", false),
                    autoApply = obj.optBoolean("autoApply", false),
                    // Optional per-prompt reasoning override; unknown/missing → null (global). Issue #155.
                    reasoningEffort = obj.optString("reasoningEffort", "").takeIf { it.isNotEmpty() }
                        ?.let { runCatching { DictateReasoningEffort.valueOf(it) }.getOrNull() },
                    reasoningEffortCustom = obj.optString("reasoningEffortCustom", "").takeIf { it.isNotEmpty() },
                    // Where the prompt came from, if the file says so (issue #303); missing → null.
                    libraryId = obj.optString("libraryId", "").trim().takeIf { it.isNotEmpty() },
                ),
            )
        }
        result
    }.getOrNull()
}

@Composable
private fun ImportModeDialog(
    onDismiss: () -> Unit,
    onReplace: () -> Unit,
    onAdd: () -> Unit,
) {
    UDialog(
        title = stringRes(R.string.dictate__prompts_import_mode_title),
        description = stringRes(R.string.dictate__prompts_import_mode_message),
        doneLabel = stringRes(R.string.action__cancel),
        onDismiss = onDismiss,
    ) {
        UNavRow(title = stringRes(R.string.dictate__prompts_import_mode_add), onClick = onAdd)
        UDivider()
        UNavRow(title = stringRes(R.string.dictate__prompts_import_mode_replace), onClick = onReplace)
    }
}

@Composable
private fun PromptEditorDialog(
    initial: PromptModel,
    onDismiss: () -> Unit,
    // Reasoning effort is intentionally NOT part of sharing — it's a local, per-user/per-server choice,
    // so community contributions never carry it (recipients decide their own). Only onSave gets it.
    onShare: (name: String, prompt: String, requiresSelection: Boolean, autoApply: Boolean) -> Unit,
    onSave: (name: String, prompt: String, requiresSelection: Boolean, autoApply: Boolean, reasoning: DictateReasoningEffort?, reasoningCustom: String?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial.name.orEmpty()) }
    var text by remember { mutableStateOf(initial.prompt.orEmpty()) }
    var requiresSelection by remember { mutableStateOf(initial.requiresSelection) }
    var autoApply by remember { mutableStateOf(initial.autoApply) }
    var reasoning by remember { mutableStateOf(initial.reasoningEffort) }
    var reasoningCustom by remember { mutableStateOf(initial.reasoningEffortCustom.orEmpty()) }
    var showError by remember { mutableStateOf(false) }
    // Whether the prompt field has focus — decides whether its label carries the bracket tip.
    var textFocused by remember { mutableStateOf(false) }

    // A prompt wrapped in [brackets] is inserted literally — that is what makes it a snippet.
    val isSnippet = snippetBodyOf(text.trim()) != null

    // "Requires selection" has no meaning for a snippet: it is written verbatim, the selection is never
    // read. Clear it the moment the brackets appear rather than saving a flag that quietly does nothing.
    // An automatic prompt is the opposite case: it always works on the dictation, so the switch is set
    // and locked while "Apply automatically" is on (see [promptRequiresSelection]).
    LaunchedEffect(isSnippet, autoApply) {
        requiresSelection = promptRequiresSelection(text.trim(), requiresSelection, autoApply)
    }

    val save = {
        when {
            name.isBlank() || text.isBlank() -> showError = true
            else -> onSave(
                name.trim(),
                text.trim(),
                requiresSelection,
                autoApply,
                reasoning,
                reasoningCustom.trim().ifBlank { null },
            )
        }
    }

    UDialog(
        title = stringRes(if (initial.id < 0) R.string.dictate__prompt_add else R.string.dictate__prompt_edit),
        // Closing without saving; saving is its own button, because a blank prompt has to say what is wrong.
        doneLabel = stringRes(R.string.action__cancel),
        onDismiss = onDismiss,
    ) {
        UTextField(
            label = stringRes(R.string.dictate__prompt_name_title),
            placeholder = stringRes(R.string.dictate__prompt_name_placeholder),
            value = name,
            onValueChange = { name = it; showError = false },
            isError = showError && name.isBlank(),
        )
        // An empty, untouched field carries the tip about the brackets in place of the bare "Prompt" label:
        // that sentence is the whole answer to issue #283. As soon as the field is used, the label shrinks
        // back to "Prompt" and the tip stays in the placeholder.
        UTextField(
            label = stringRes(R.string.dictate__prompt_text_title),
            placeholder = stringRes(R.string.dictate__prompt_text_placeholder),
            value = text,
            onValueChange = { text = it; showError = false },
            singleLine = false,
            isError = showError && text.isBlank(),
        )
        UDivider()
        USwitchRow(
            title = stringRes(R.string.dictate__prompt_requires_selection_title),
            subtitle = stringRes(R.string.dictate__prompt_requires_selection_summary),
            checked = requiresSelection,
            onCheckedChange = { requiresSelection = it },
            // A snippet is inserted verbatim and never looks at a selection, so the switch would be a
            // promise the prompt cannot keep: cleared and locked while the brackets are there. An automatic
            // prompt keeps it set and locked, because off it would only ever see its own instruction.
            enabled = !isSnippet && !autoApply,
        )
        UDivider()
        USwitchRow(
            title = stringRes(R.string.dictate__prompt_auto_apply_title),
            subtitle = stringRes(R.string.dictate__prompt_auto_apply_summary),
            checked = autoApply,
            onCheckedChange = { autoApply = it },
        )
        UDivider()
        // Per-prompt reasoning-effort override (issue #155): "Default" = use the global setting.
        ReasoningRow(
            selected = reasoning,
            custom = reasoningCustom,
            onSelected = { effort, c -> reasoning = effort; reasoningCustom = c },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.action__save),
            onClick = save,
        )
        // Contribute this prompt to the community library (issue #105), only once there is something worth
        // sharing; the submission itself happens as a GitHub pull request.
        if (name.isNotBlank() && text.isNotBlank()) {
            UDivider()
            UNavRow(
                title = stringRes(R.string.dictate__prompt_share_title),
                onClick = { onShare(name.trim(), text.trim(), requiresSelection, autoApply) },
            )
        }
        if (onDelete != null) {
            UDivider()
            UNavRow(title = stringRes(R.string.action__delete), onClick = onDelete)
        }
    }
}

/**
 * Per-prompt reasoning-effort selector (issue #155): a label that opens the shared radio dialog with a
 * custom-value field (issue #186). "Default" = use the global setting.
 */
@Composable
private fun ReasoningRow(
    selected: DictateReasoningEffort?,
    custom: String,
    onSelected: (DictateReasoningEffort?, String) -> Unit,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    UNavRow(
        title = stringRes(R.string.dictate__prompt_reasoning_title),
        subtitle = if (selected == DictateReasoningEffort.CUSTOM && custom.isNotBlank()) {
            custom
        } else {
            reasoningEffortLabel(selected)
        },
        onClick = { dialogOpen = true },
    )
    if (dialogOpen) {
        ReasoningEffortDialog(
            initialEffort = selected,
            initialCustom = custom,
            includeUseGlobal = true,
            onConfirm = { effort, c -> onSelected(effort, c); dialogOpen = false },
            onDismiss = { dialogOpen = false },
        )
    }
}

/** Localized label for a per-prompt reasoning choice; null = "use the global setting". */
@Composable
private fun reasoningEffortLabel(effort: DictateReasoningEffort?): String = stringRes(
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
