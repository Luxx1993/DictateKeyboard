/*
 * Copyright (C) 2021-2025 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.app.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UnisonoPage
import org.florisboard.lib.compose.stringRes

/** The libraries the app ships, as plain rows (name, version, licence); the page itself scrolls. */
@Composable
fun ThirdPartyLicensesScreen() = UnisonoPage(title = stringRes(R.string.about__third_party_licenses__title)) {
    val libs by produceLibraries()
    val c = LocalUnisono.current
    UCard {
        libs?.libraries?.forEachIndexed { index, lib ->
            if (index > 0) UDivider()
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
                Text(lib.name, style = Unisono.Body, color = c.textPrimary)
                val meta = listOfNotNull(
                    lib.artifactVersion,
                    lib.licenses.joinToString(", ") { it.name }.ifBlank { null },
                ).joinToString(" \u00b7 ")
                if (meta.isNotEmpty()) Text(meta, style = Unisono.Mono, color = c.textTertiary)
            }
        }
    }
}
