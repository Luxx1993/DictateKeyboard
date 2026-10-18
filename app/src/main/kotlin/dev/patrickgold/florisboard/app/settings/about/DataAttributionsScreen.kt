/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.about

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.lib.io.FlorisRef
import dev.patrickgold.florisboard.lib.io.loadTextAsset
import org.florisboard.lib.compose.stringRes

@Composable
fun DataAttributionsScreen() = UnisonoPage(title = stringRes(R.string.about__data_attributions__title)) {
    val context = LocalContext.current
    val c = LocalUnisono.current
    UCard {
        SelectionContainer(Modifier.padding(14.dp)) {
            val text = FlorisRef.assets("license/data_attributions.txt").loadTextAsset(context)
                .getOrElse { it.message ?: "" }
            Text(text = text, style = Unisono.Mono, color = c.textPrimary)
        }
    }
}
