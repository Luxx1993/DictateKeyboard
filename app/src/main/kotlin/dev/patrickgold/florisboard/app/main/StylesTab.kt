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
 * auto-apply over. Tapping a style card chooses it, holding it opens the editor.
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

    fun edit(style: PromptModel) = navController.navigate(Routes.Settings.DictatePrompts(editPromptId = style.id))

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
            .clickable { navController.navigate(Routes.Settings.DictatePrompts(editPromptId = PromptModel.ID_ADD_PROMPT)) },
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
