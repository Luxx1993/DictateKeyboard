/*
 * Copyright (C) 2026 Lukas Klenk (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.dictate.DictateController
import dev.patrickgold.florisboard.dictate.data.prompts.PromptModel
import dev.patrickgold.florisboard.dictate.data.prompts.PromptsDatabaseHelper
import dev.patrickgold.florisboard.dictate.data.prompts.snippetBody
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.compose.stringRes

/**
 * Stile (Entwurf 3, `Stile.html`): the prompts that rewrite a dictation, one of them active.
 *
 * A style is a saved prompt; snippets are left out, since they insert text rather than clean it up. The
 * active style is the one set to run after every dictation (auto-apply). With that switched off it stays
 * the chosen one ([activeStyleId][dev.patrickgold.florisboard.app.FlorisPreferenceModel.Dictate.activeStyleId]),
 * so the switch can bring it back. Only one style runs at a time from here: choosing another moves the
 * auto-apply over. Tapping a style card chooses it, holding it opens the editor; "Neuer Stil" writes a new
 * one. Both edit in place ([StyleEditor]); the full prompt screen behind "Stile verwalten" keeps the rest
 * (order, reasoning effort, import and export, the library).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StylesTab() {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val db = remember { PromptsDatabaseHelper.getInstance(context) }

    UPageHeader(
        title = stringRes(R.string.main__tab_styles),
        subtitle = stringRes(R.string.main__styles_subtitle),
    )

    var styles by remember { mutableStateOf<List<PromptModel>>(emptyList()) }
    var version by remember { mutableStateOf(0) }
    LaunchedEffect(version) {
        styles = withContext(Dispatchers.IO) {
            runCatching { db.getAll() }.getOrDefault(emptyList())
        }.filter { !it.name.isNullOrBlank() && it.snippetBody() == null }
    }

    val chosenId by prefs.dictate.activeStyleId.collectAsState()
    val rewordingEnabled by prefs.dictate.rewordingEnabled.collectAsState()
    val active = styles.firstOrNull { it.autoApply } ?: styles.firstOrNull { it.id == chosenId }
    val applying = active != null && active.autoApply && rewordingEnabled

    /** Makes [target] the active style; it runs after every dictation when [apply] is true. */
    fun choose(target: PromptModel, apply: Boolean) {
        scope.launch {
            withContext(Dispatchers.IO) {
                for (style in styles) {
                    val shouldApply = apply && style.id == target.id
                    if (style.autoApply != shouldApply) db.update(style.copy(autoApply = shouldApply))
                }
            }
            prefs.dictate.activeStyleId.set(target.id)
            if (apply) prefs.dictate.rewordingEnabled.set(true)
            DictateController.refreshPrompts(context)
            version++
        }
    }

    // The style in the editor: a new one has id -1. Null while the editor is closed.
    var editing by remember { mutableStateOf<PromptModel?>(null) }

    fun edit(style: PromptModel) {
        editing = style.copy()
    }

    editing?.let { draft ->
        StyleEditor(
            draft = draft,
            onDismiss = { editing = null },
            onSave = { name, prompt ->
                editing = null
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (draft.isPersisted()) {
                            db.update(draft.copy(name = name, prompt = prompt))
                        } else {
                            val pos = (db.getAll().maxOfOrNull { it.pos } ?: -1) + 1
                            db.add(PromptModel(-1, pos, name, prompt, requiresSelection = true, autoApply = false))
                        }
                    }
                    DictateController.refreshPrompts(context)
                    version++
                }
            },
            onDelete = if (draft.isPersisted()) {
                {
                    editing = null
                    scope.launch {
                        withContext(Dispatchers.IO) { db.delete(draft.id) }
                        if (prefs.dictate.activeStyleId.get() == draft.id) prefs.dictate.activeStyleId.set(-1)
                        DictateController.refreshPrompts(context)
                        version++
                    }
                }
            } else {
                null
            },
        )
    }

    if (active == null) {
        Text(stringRes(R.string.main__styles_none_active), style = Unisono.Body, color = c.textSecondary)
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Unisono.RadiusLg)
                .background(c.raised)
                .border(1.5.dp, c.action, Unisono.RadiusLg)
                .clickable { edit(active) }
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    active.name.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = Unisono.BodyStrong.copy(fontSize = 18.sp, lineHeight = 26.sp),
                    color = c.textPrimary,
                )
                Text(
                    stringRes(R.string.main__styles_active),
                    modifier = Modifier
                        .clip(Unisono.Pill)
                        .background(c.action)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = Unisono.NavLabel.copy(fontWeight = FontWeight.Bold),
                    color = c.onAction,
                )
            }
            Text(
                active.prompt.orEmpty(),
                style = Unisono.Body.copy(fontSize = 14.sp),
                color = c.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            val applyLabel = stringRes(R.string.main__styles_auto_apply)
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    applyLabel,
                    modifier = Modifier.weight(1f),
                    style = Unisono.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = c.textPrimary,
                )
                USwitch(
                    checked = applying,
                    onCheckedChange = { choose(active, apply = it) },
                    contentDescription = applyLabel,
                )
            }
        }
    }

    val others = styles.filter { it.id != active?.id }
    others.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (style in pair) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 108.dp)
                        .clip(Unisono.RadiusLg)
                        .background(c.raised)
                        .border(0.5.dp, c.borderSubtle, Unisono.RadiusLg)
                        .combinedClickable(
                            onClick = { choose(style, apply = applying) },
                            onLongClick = { edit(style) },
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(style.name.orEmpty(), style = Unisono.BodyStrong, color = c.textPrimary, maxLines = 2)
                    Text(
                        style.prompt.orEmpty(),
                        style = Unisono.Label.copy(lineHeight = 19.sp),
                        color = c.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (pair.size == 1) Box(Modifier.weight(1f))
        }
    }

    val dash = c.borderStrong
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(Unisono.Pill)
            .drawBehind {
                val inset = 0.75.dp.toPx()
                drawRoundRect(
                    color = dash,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - 2 * inset, size.height - 2 * inset),
                    cornerRadius = CornerRadius(size.height / 2),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                    ),
                )
            }
            .clickable { editing = PromptModel(-1, 0, "", "", requiresSelection = true, autoApply = false) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(UnisonoIcons.Plus, contentDescription = null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
        Text(
            stringRes(R.string.main__styles_new),
            modifier = Modifier.padding(start = 8.dp),
            style = Unisono.Body.copy(fontWeight = FontWeight.SemiBold),
            color = c.textPrimary,
        )
    }

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            stringRes(R.string.main__styles_manage),
            modifier = Modifier
                .clip(Unisono.RadiusMd)
                .clickable { navController.navigate(Routes.Settings.DictatePrompts()) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            style = Unisono.Caption,
            color = c.textSecondary,
        )
    }
}

/**
 * Writing a style: its name and what the AI should do with the dictation. Saving needs both. [onDelete] is
 * null for a style that does not exist yet; deleting asks once more.
 */
@Composable
private fun StyleEditor(
    draft: PromptModel,
    onDismiss: () -> Unit,
    onSave: (name: String, prompt: String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val c = LocalUnisono.current
    var name by rememberSaveable { mutableStateOf(draft.name.orEmpty()) }
    var prompt by rememberSaveable { mutableStateOf(draft.prompt.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val canSave = name.isNotBlank() && prompt.isNotBlank()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(Unisono.RadiusLg)
                .background(c.raised)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                stringRes(if (draft.isPersisted()) R.string.main__styles_edit_title else R.string.main__styles_new),
                style = Unisono.SectionTitle,
                color = c.textPrimary,
            )
            EditorField(
                label = stringRes(R.string.main__styles_field_name),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
            )
            EditorField(
                label = stringRes(R.string.main__styles_field_prompt),
                value = prompt,
                onValueChange = { prompt = it },
                singleLine = false,
                hint = stringRes(R.string.main__styles_field_prompt_hint),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text(stringRes(R.string.action__delete), color = c.textSecondary)
                    }
                }
                Box(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringRes(R.string.action__cancel), color = c.textSecondary) }
                Text(
                    stringRes(R.string.main__styles_save),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(Unisono.Pill)
                        .background(if (canSave) c.action else c.borderStrong)
                        .clickable(enabled = canSave) { onSave(name.trim(), prompt.trim()) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    style = Unisono.Caption,
                    color = c.onAction,
                )
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(stringRes(R.string.main__styles_delete_confirm, "name" to name.ifBlank { draft.name.orEmpty() })) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringRes(R.string.action__delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringRes(R.string.action__cancel)) }
            },
        )
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    hint: String? = null,
) {
    val c = LocalUnisono.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = Unisono.Label.copy(fontWeight = FontWeight.SemiBold), color = c.textSecondary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (singleLine) 48.dp else 160.dp)
                .clip(Unisono.RadiusMd)
                .background(c.sunken)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (value.isEmpty() && hint != null) {
                Text(hint, style = Unisono.Body, color = c.textTertiary)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                textStyle = Unisono.Body.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.textPrimary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
                ),
            )
        }
    }
}
