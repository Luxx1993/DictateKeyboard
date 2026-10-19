/*
 * Copyright (C) 2025 The FlorisBoard Contributors
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

package dev.patrickgold.florisboard.app

import androidx.compose.runtime.Composable
import dev.patrickgold.florisboard.R
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries
import org.florisboard.lib.compose.stringRes
import org.florisboard.lib.kotlin.curlyFormat
import kotlin.reflect.KClass

private const val DEFAULT = ""

private val ENUM_DISPLAY_ENTRIES = mapOf<Pair<KClass<*>, String>, @Composable () -> List<ListPreferenceEntry<*>>>(
    AppTheme::class to DEFAULT to {
        listPrefEntries {
            entry(
                key = AppTheme.AUTO,
                label = stringRes(R.string.settings__system_default),
            )
            entry(
                key = AppTheme.LIGHT,
                label = stringRes(R.string.pref__other__settings_theme__light),
            )
            entry(
                key = AppTheme.DARK,
                label = stringRes(R.string.pref__other__settings_theme__dark),
            )
        }
    },
)

@Composable
fun <V : Any> enumDisplayEntriesOf(
    enumClass: KClass<V>,
    variant: String = DEFAULT,
): List<ListPreferenceEntry<V>> {
    @Suppress("UNCHECKED_CAST")
    return ENUM_DISPLAY_ENTRIES[enumClass to variant]?.invoke()
        as List<ListPreferenceEntry<V>>
}
