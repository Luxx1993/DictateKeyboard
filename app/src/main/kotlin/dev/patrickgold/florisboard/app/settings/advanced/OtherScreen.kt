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

package dev.patrickgold.florisboard.app.settings.advanced

import android.text.format.Formatter
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.AppIconColor
import dev.patrickgold.florisboard.app.AppTheme
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.enumDisplayEntriesOf
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UChoice
import dev.patrickgold.florisboard.app.main.UChoiceRow
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UGroupLabel
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.USwitchRow
import dev.patrickgold.florisboard.app.main.UTextField
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.lib.FlorisLocale
import dev.patrickgold.florisboard.lib.cache.AppCache
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.florisboard.lib.android.AndroidVersion
import org.florisboard.lib.compose.stringRes

/** The remaining app settings in the look of Entwurf 3: appearance, name and icon, the cache, backup. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OtherScreen() = UnisonoPage(title = stringRes(R.string.settings__other__title)) {
    val prefs by FlorisPreferenceStore
    val navController = LocalNavController.current
    val context = LocalContext.current
    val c = LocalUnisono.current
    val scope = rememberCoroutineScope()

    val theme by prefs.other.settingsTheme.collectAsState()
    val accent by prefs.other.accentColor.collectAsState()
    val language by prefs.other.settingsLanguage.collectAsState()
    val userName by prefs.other.userName.collectAsState()
    val iconColor by prefs.other.appIconColor.collectAsState()
    val showIcon by prefs.other.showAppIcon.collectAsState()
    var openDialog by remember { mutableStateOf<String?>(null) }

    // Walked rather than remembered: nothing counts the cache as it grows, and the walk is cheap enough to
    // redo whenever this screen opens or the cache is emptied (issue #337).
    var cacheBytes by remember { mutableStateOf(0L) }
    var cacheGeneration by remember { mutableStateOf(0) }
    LaunchedEffect(cacheGeneration) {
        cacheBytes = withContext(Dispatchers.IO) { AppCache.sizeBytes(context) }
    }

    UCard {
        val themeEntries = enumDisplayEntriesOf(AppTheme::class)
        UChoiceRow(
            title = stringRes(R.string.pref__other__settings_theme__label),
            choices = themeEntries.map { UChoice(it.key, it.label) },
            // The AMOLED values are no longer offered; a stored one shows as the plain variant.
            selected = when (theme) {
                AppTheme.AUTO_AMOLED -> AppTheme.AUTO
                AppTheme.AMOLED_DARK -> AppTheme.DARK
                else -> theme
            },
            onSelect = { v -> scope.launch { prefs.other.settingsTheme.set(v) } },
            open = openDialog == "theme",
            onOpenChange = { openDialog = if (it) "theme" else null },
            modifier = Modifier.settingsSearchAnchor("pref__other__settings_theme__label"),
        )
        UDivider()
        val systemDefault = stringRes(R.string.settings__system_default)
        val languageChoices = remember(systemDefault) {
            listOf("auto", "ar", "bg", "bs", "ca", "ckb", "cs", "da", "de", "el", "en", "eo", "es", "fa", "fi", "fr", "hr", "hu", "in", "it", "iw", "ja", "ko-KR", "ku", "lv-LV", "mk", "nds-DE", "nl", "no", "pl", "pt", "pt-BR", "ru", "sk", "sl", "sr", "sv", "tr", "uk", "zgh", "zh-CN").map { tag ->
                if (tag == "auto") {
                    UChoice(tag, systemDefault)
                } else {
                    val locale = FlorisLocale.fromTag(tag)
                    UChoice(locale.languageTag(), locale.displayName(locale))
                }
            }
        }
        UChoiceRow(
            title = stringRes(R.string.pref__other__settings_language__label),
            choices = languageChoices,
            selected = language,
            onSelect = { v -> scope.launch { prefs.other.settingsLanguage.set(v) } },
            open = openDialog == "language",
            onOpenChange = { openDialog = if (it) "language" else null },
            modifier = Modifier.settingsSearchAnchor("pref__other__settings_language__label"),
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.pref__other__user_name__label),
            subtitle = userName.ifBlank { null },
            onClick = { openDialog = "name" },
            modifier = Modifier.settingsSearchAnchor("pref__other__user_name__label"),
        )
    }

    UCard {
        UChoiceRow(
            title = stringRes(R.string.pref__other__app_icon_color__label),
            choices = listOf(
                UChoice(AppIconColor.WHITE, stringRes(R.string.pref__other__app_icon_color__white)),
                UChoice(AppIconColor.BLACK, stringRes(R.string.pref__other__app_icon_color__black)),
            ),
            selected = iconColor,
            onSelect = { v -> scope.launch { prefs.other.appIconColor.set(v) } },
            open = openDialog == "icon",
            onOpenChange = { openDialog = if (it) "icon" else null },
            modifier = Modifier.settingsSearchAnchor("pref__other__app_icon_color__label"),
        )
        UDivider()
        USwitchRow(
            title = stringRes(R.string.pref__other__show_app_icon__label),
            subtitle = if (AndroidVersion.ATLEAST_API29_Q) {
                stringRes(R.string.pref__other__show_app_icon__summary_atleast_q)
            } else {
                null
            },
            checked = showIcon,
            enabled = AndroidVersion.ATMOST_API28_P,
            onCheckedChange = { scope.launch { prefs.other.showAppIcon.set(it) } },
            modifier = Modifier.settingsSearchAnchor("pref__other__show_app_icon__label"),
        )
    }

    // What the app made and can make again, such as a shared file on its way through (issue #337). Emptied
    // on every cold start anyway, so the row exists for the hours in between, and it says how much there
    // is, because a cleaning button that will not tell you what it cleans is a button nobody trusts.
    UCard {
        UNavRow(
            title = stringRes(R.string.pref__other__clear_cache__label),
            subtitle = stringRes(
                R.string.pref__other__clear_cache__summary,
                "size" to Formatter.formatShortFileSize(context, cacheBytes),
            ),
            onClick = { openDialog = "cache" },
            modifier = Modifier.settingsSearchAnchor("pref__other__clear_cache__label"),
        )
    }

    UGroupLabel(stringRes(R.string.backup_and_restore__title))
    UCard {
        UNavRow(
            title = stringRes(R.string.backup_and_restore__back_up__title),
            subtitle = stringRes(R.string.backup_and_restore__back_up__summary),
            onClick = { navController.navigate(Routes.Settings.Backup) },
            modifier = Modifier.settingsSearchAnchor("backup_and_restore__back_up__title"),
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.backup_and_restore__restore__title),
            subtitle = stringRes(R.string.backup_and_restore__restore__summary),
            onClick = { navController.navigate(Routes.Settings.Restore) },
            modifier = Modifier.settingsSearchAnchor("backup_and_restore__restore__title"),
        )
    }

    when (openDialog) {
        "name" -> {
            var draft by remember { mutableStateOf(userName) }
            UDialog(
                title = stringRes(R.string.pref__other__user_name__label),
                onDismiss = {
                    scope.launch { prefs.other.userName.set(draft.trim()) }
                    openDialog = null
                },
            ) {
                UTextField(
                    label = stringRes(R.string.pref__other__user_name__label),
                    placeholder = stringRes(R.string.main__greeting_name_hint),
                    value = draft,
                    onValueChange = { draft = it },
                )
            }
        }
        // Says what survives, not only what goes: "clear" next to an app that stores dictation history and
        // recordings is a word worth defusing.
        "cache" -> UDialog(
            title = stringRes(R.string.pref__other__clear_cache__label),
            description = stringRes(R.string.pref__other__clear_cache__message),
            onDismiss = { openDialog = null },
        ) {
            UNavRow(
                title = stringRes(R.string.pref__other__clear_cache__confirm),
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { AppCache.clear(context) }
                        cacheGeneration++
                    }
                    openDialog = null
                },
            )
        }
    }
}
