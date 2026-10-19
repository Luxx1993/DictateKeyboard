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

package dev.patrickgold.florisboard.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UnisonoIcons
import dev.patrickgold.florisboard.app.main.UnisonoPage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.PreferenceRecovery
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.dictate.data.stats.DictateStats
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import dev.patrickgold.jetpref.datastore.model.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.IconButton
import dev.patrickgold.florisboard.dictate.importer.TranscribeShareActivity
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import org.florisboard.lib.android.showLongToast
import org.florisboard.lib.compose.FlorisErrorCard
import org.florisboard.lib.compose.FlorisIconButton
import org.florisboard.lib.compose.FlorisWarningCard
import org.florisboard.lib.compose.stringRes

@Composable
fun HomeScreen() = UnisonoPage(
    // Reached from Einstellungen › Alle Einstellungen since the tabs of Entwurf 3 became the start screen.
    title = stringRes(R.string.main__settings_all),
) {
    val prefs by FlorisPreferenceStore
    val navController = LocalNavController.current
    val context = LocalContext.current
    val c = LocalUnisono.current

    // Settings that could not be read at start were kept instead of written over (#383). This is the way
    // back to them, offered until they are restored or discarded.
    val settingsUnreadable by PreferenceRecovery.pending.collectAsState()
    var showRecoveryDialog by remember { mutableStateOf(false) }
    val recoveryScope = rememberCoroutineScope()
    if (settingsUnreadable) {
        UCard {
            UNavRow(
                title = stringRes(R.string.dictate__settings_unreadable_card),
                onClick = { showRecoveryDialog = true },
            )
        }
    }
    if (showRecoveryDialog) {
        UDialog(
            title = stringRes(R.string.dictate__settings_unreadable_title),
            description = stringRes(R.string.dictate__settings_unreadable_message),
            doneLabel = stringRes(R.string.action__cancel),
            onDismiss = { showRecoveryDialog = false },
        ) {
            UNavRow(
                title = stringRes(R.string.action__restore),
                onClick = {
                    showRecoveryDialog = false
                    recoveryScope.launch {
                        PreferenceRecovery.restore(context).onFailure {
                            context.showLongToast(R.string.dictate__settings_restore_failed)
                        }
                    }
                },
            )
            UDivider()
            // Delete, not Discard: several translations of Discard read the same as Cancel beside it.
            UNavRow(
                title = stringRes(R.string.action__delete),
                onClick = {
                    showRecoveryDialog = false
                    PreferenceRecovery.discard(context)
                },
            )
        }
    }

    // The search, as the pill it is on the other pages.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(Unisono.Pill)
            .background(c.sunken)
            .clickable { navController.navigate(Routes.Settings.Search) }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(UnisonoIcons.Search, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(18.dp))
        Text(stringRes(R.string.settings__search__placeholder), style = Unisono.Body, color = c.textTertiary)
    }

    // Passive dictation-stats summary (issue #142): appears once the user has dictated, taps through to the
    // full statistics screen. No interruption, just a glanceable "time saved".
    val statDictations by prefs.dictate.statsDictations.collectAsState()
    val statWords by prefs.dictate.statsWords.collectAsState()
    val statSpoken by prefs.dictate.statsSpokenSeconds.collectAsState()
    if (statDictations > 0L) {
        val saved = DictateStats.savedSeconds(statWords, statSpoken)
        UCard {
            UNavRow(
                title = homeDuration(saved.toLong()),
                subtitle = "${stringRes(R.string.dictate__stats_time_saved)} \u00b7 " +
                    "${NumberFormat.getIntegerInstance().format(statDictations)} ${stringRes(R.string.dictate__stats_dictations)}",
                icon = UnisonoIcons.Clock,
                onClick = { navController.navigate(Routes.Settings.DictateStats) },
            )
        }
    }

    // "Transcribe a file" (issue #301) reachable in one tap from the top, without going through the Dictate
    // screen first. The labelled row stays there as well: this icon is what the settings search cannot find
    // and what nobody discovers who does not already know about it.
    val transcribePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.startActivity(TranscribeShareActivity.intentFor(context, uri))
        }
    }
    UCard {
        UNavRow(
            title = stringRes(R.string.dictate__title),
            icon = UnisonoIcons.Mic,
            onClick = { navController.navigate(Routes.Settings.Dictate) },
            trailing = {
                IconButton(onClick = { transcribePicker.launch(TranscribeShareActivity.MIME_TYPES) }) {
                    Icon(
                        imageVector = UnisonoIcons.AudioFile,
                        contentDescription = stringRes(R.string.dictate__import_menu),
                        tint = c.textSecondary,
                    )
                }
            },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.settings__other__title),
            icon = UnisonoIcons.Gear,
            onClick = { navController.navigate(Routes.Settings.Other) },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.about__title),
            icon = UnisonoIcons.Book,
            onClick = { navController.navigate(Routes.Settings.About) },
        )
    }
}

/** Compact duration for the home stats card: `3h`, `3h 12m`, `12m`, `45s`. */
private fun homeDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0L)
    val h = s / 3600
    val m = (s % 3600) / 60
    return when {
        h > 0 -> if (m > 0) "${h}h ${m}m" else "${h}h"
        m > 0 -> "${m}m"
        else -> "${s}s"
    }
}
