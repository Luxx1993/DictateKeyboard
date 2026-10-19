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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCaption
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UCheckMark
import dev.patrickgold.florisboard.app.main.UChoiceOption
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoLazyPage
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.dictate.DictateFloatingButtonAppScope
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.compose.FlorisCanvasIcon
import org.florisboard.lib.compose.stringRes

/**
 * Where the floating dictation button may appear (issue #392).
 *
 * The mode decides how the list below it is read, and the list is only shown once there is a mode that
 * reads it — a greyed-out list of a hundred apps would be a worse lie than no list at all (#297).
 */
@Composable
fun DictateFloatingButtonAppsScreen() {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scope by prefs.dictate.floatingButtonAppScope.collectAsState()
    val apps by prefs.dictate.floatingButtonApps.collectAsState()
    val anchors by prefs.dictate.floatingButtonPositions.collectAsState()
    val selected = apps.toSet()

    var searchTerm by remember { mutableStateOf("") }

    // Apps the button has actually been used in, from the remembered positions (#323). They are here because
    // package visibility is limited to launchable apps: without this, an app with no launcher entry could
    // never be ticked, and in "only the apps below" that means never reachable at all.
    val known = remember(anchors) { anchors.entries.map { it.pkg }.toSet() + selected }
    val installed by produceState<List<InstalledApp>?>(initialValue = null, known) {
        value = withContext(Dispatchers.IO) { loadInstalledApps(context, known) }
    }

    // Sorted once, against the selection the screen opened with: ticking a box must not make the row jump
    // out from under the finger, but opening the screen should still put your own choices on top.
    val initiallySelected = remember { selected }
    val ordered = remember(installed) {
        installed?.sortedWith(compareBy({ it.pkg !in initiallySelected }, { it.label.lowercase() }))
    }
    val filtered = remember(ordered, searchTerm) {
        val term = searchTerm.trim().lowercase()
        when {
            ordered == null -> null
            term.isEmpty() -> ordered
            else -> ordered.filter { it.label.lowercase().contains(term) || it.pkg.lowercase().contains(term) }
        }
    }

    // A lazy page: a phone can hold hundreds of apps, and only the rows in view are composed.
    UnisonoLazyPage(title = stringRes(R.string.dictate__floating_button_apps_title)) {
        item {
            UCard {
                DictateFloatingButtonAppScope.entries.forEachIndexed { index, entry ->
                    if (index > 0) UDivider()
                    UChoiceOption(
                        title = stringRes(entry.labelRes()),
                        subtitle = null,
                        selected = scope == entry,
                        onClick = { coroutineScope.launch { prefs.dictate.floatingButtonAppScope.set(entry) } },
                    )
                }
            }
        }
        item { UCaption(stringRes(R.string.dictate__floating_button_apps_note)) }

        if (scope != DictateFloatingButtonAppScope.ALL) {
            item {
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
                        if (searchTerm.isEmpty()) {
                            Text(stringRes(R.string.dictate__floating_button_apps_search), style = Unisono.Body, color = c.textTertiary)
                        }
                        BasicTextField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = Unisono.Body.copy(color = c.textPrimary),
                            cursorBrush = SolidColor(c.textPrimary),
                        )
                    }
                }
            }
            when {
                filtered == null -> item {
                    Text(
                        "\u2026",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        style = Unisono.SectionTitle,
                        color = c.textTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
                filtered.isEmpty() -> item {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringRes(R.string.dictate__floating_button_apps_none, "search_term" to searchTerm),
                        style = Unisono.Body,
                        color = c.textSecondary,
                    )
                }
                else -> itemsIndexed(filtered, key = { _, app -> app.pkg }) { index, app ->
                    // The rows of one card, drawn per row so the list can stay lazy: only the first and the
                    // last are rounded, and a hairline separates the others.
                    val shape = RoundedCornerShape(
                        topStart = if (index == 0) 16.dp else 0.dp,
                        topEnd = if (index == 0) 16.dp else 0.dp,
                        bottomStart = if (index == filtered.lastIndex) 16.dp else 0.dp,
                        bottomEnd = if (index == filtered.lastIndex) 16.dp else 0.dp,
                    )
                    Column(Modifier.clip(shape).background(c.raised)) {
                        if (index > 0) UDivider()
                        AppRow(
                            app = app,
                            checked = app.pkg in selected,
                            onClick = {
                                coroutineScope.launch {
                                    prefs.dictate.floatingButtonApps.set(apps.toggled(app.pkg))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp, checked: Boolean, onClick: () -> Unit) {
    val c = LocalUnisono.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (app.icon != null) {
            FlorisCanvasIcon(drawable = app.icon, modifier = Modifier.size(32.dp))
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(app.label, style = Unisono.Body, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(app.pkg, style = Unisono.Mono, color = c.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        UCheckMark(checked)
    }
}

/** The label shown for each mode. */
private fun DictateFloatingButtonAppScope.labelRes(): Int = when (this) {
    DictateFloatingButtonAppScope.ALL -> R.string.dictate__floating_button_apps_scope_all
    DictateFloatingButtonAppScope.ONLY_SELECTED -> R.string.dictate__floating_button_apps_scope_only
    DictateFloatingButtonAppScope.EXCEPT_SELECTED -> R.string.dictate__floating_button_apps_scope_except
}

/** One row of the picker. [icon] is null when the system will not hand one over. */
private data class InstalledApp(
    val pkg: String,
    val label: String,
    val icon: Drawable?,
)

/**
 * Everything the user can pick between: apps with a launcher (or home) entry, plus [extraPackages] —
 * packages we already know the button has been used in, which may not be launchable at all.
 *
 * Both reads go through the `<queries>` filter in the manifest, so this is not "every installed app" and
 * is not meant to be: `QUERY_ALL_PACKAGES` is a restricted permission with its own Play declaration, and
 * a keyboard asking to enumerate the phone is exactly the thing this feature exists to avoid. A package
 * we are not allowed to resolve still gets a row, labelled by its package name.
 */
private fun loadInstalledApps(context: Context, extraPackages: Set<String>): List<InstalledApp> {
    val pm = context.packageManager
    val found = LinkedHashMap<String, InstalledApp>()
    val intents = listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
    )
    for (intent in intents) {
        val resolved = runCatching { pm.queryIntentActivities(intent, 0) }.getOrDefault(emptyList())
        for (info in resolved) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (found.containsKey(pkg)) continue
            found[pkg] = InstalledApp(
                pkg = pkg,
                label = runCatching { info.loadLabel(pm).toString() }.getOrNull()?.takeIf { it.isNotBlank() } ?: pkg,
                icon = runCatching { info.loadIcon(pm) }.getOrNull()?.takeIf { it.hasSize() },
            )
        }
    }
    for (pkg in extraPackages) {
        if (found.containsKey(pkg)) continue
        val appInfo = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull()
        found[pkg] = InstalledApp(
            pkg = pkg,
            label = appInfo
                ?.let { runCatching { pm.getApplicationLabel(it).toString() }.getOrNull() }
                ?.takeIf { it.isNotBlank() } ?: pkg,
            icon = appInfo?.let { runCatching { pm.getApplicationIcon(it) }.getOrNull() }?.takeIf { it.hasSize() },
        )
    }
    return found.values.toList()
}

/** FlorisCanvasIcon rasterizes into a bitmap of the drawable's intrinsic size, which must not be zero. */
private fun Drawable.hasSize(): Boolean = intrinsicWidth > 0 && intrinsicHeight > 0
