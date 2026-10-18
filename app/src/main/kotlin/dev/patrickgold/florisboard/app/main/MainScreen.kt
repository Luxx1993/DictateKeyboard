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

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import org.florisboard.lib.compose.stringRes

/** The four tabs of Entwurf 3, in the order of the navigation bar. */
enum class MainTab(val icon: () -> ImageVector, val label: Int) {
    START({ UnisonoIcons.Home }, R.string.main__tab_start),
    DICTIONARY({ UnisonoIcons.Book }, R.string.main__tab_dictionary),
    STYLES({ UnisonoIcons.Sparkle }, R.string.main__tab_styles),
    SETTINGS({ UnisonoIcons.Gear }, R.string.main__tab_settings),
}

/**
 * The app's front door since Entwurf 3 (docs/design/diktat-design.md §2): four tabs on paper, a navigation
 * bar on the raised surface. The tabs lead into the existing settings screens for anything deeper; the
 * full list of them stays reachable as "Alle Einstellungen".
 */
@Composable
fun MainScreen() {
    val c = LocalUnisono.current
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(MainTab.START) }

    SideEffect {
        val window = (context as Activity).window
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.Transparent.toArgb()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.canvas),
    ) {
        // Each tab keeps its own scroll position while it is shown; switching starts the next one at the top.
        key(tab) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 20.dp, end = 20.dp, top = 40.dp),
                verticalArrangement = Arrangement.spacedBy(if (tab == MainTab.SETTINGS) 16.dp else 24.dp),
            ) {
                when (tab) {
                    MainTab.START -> StartTab()
                    MainTab.DICTIONARY -> DictionaryTab()
                    MainTab.STYLES -> StylesTab()
                    MainTab.SETTINGS -> SettingsTab()
                }
                UBottomSpace()
            }
        }
        MainNavBar(selected = tab, onSelect = { tab = it })
    }
}

@Composable
private fun MainNavBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    val c = LocalUnisono.current
    Column(Modifier.fillMaxWidth().background(c.raised)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (tab in MainTab.entries) {
                val active = tab == selected
                val color = if (active) c.textPrimary else c.textTertiary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = active,
                            onClick = { onSelect(tab) },
                            role = Role.Tab,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(tab.icon(), contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                    Text(
                        stringRes(tab.label),
                        style = Unisono.NavLabel.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
                        color = color,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
