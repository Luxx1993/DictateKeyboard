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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UButton
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UGroupLabel
import dev.patrickgold.florisboard.app.main.UNotice
import dev.patrickgold.florisboard.app.main.UnisonoPage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.BuildConfig
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceModel
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.cacheManager
import dev.patrickgold.florisboard.dictate.data.history.DictateHistoryEntry
import dev.patrickgold.florisboard.dictate.data.history.DictateHistoryStore
import dev.patrickgold.florisboard.dictate.data.prompts.PromptModel
import dev.patrickgold.florisboard.dictate.data.prompts.PromptsDatabaseHelper
import dev.patrickgold.florisboard.dictate.provider.ProviderAccounts
import dev.patrickgold.florisboard.dictate.provider.hasNoSecrets
import dev.patrickgold.florisboard.lib.cache.CacheManager
import dev.patrickgold.florisboard.lib.io.ZipUtils
import dev.patrickgold.jetpref.datastore.runtime.AndroidAppDataStorage
import dev.patrickgold.jetpref.datastore.runtime.DataStoreReader
import dev.patrickgold.jetpref.datastore.runtime.FileBasedStorage
import dev.patrickgold.jetpref.datastore.runtime.ImportStrategy
import java.io.FileNotFoundException
import java.text.DateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.florisboard.lib.android.readToFile
import org.florisboard.lib.android.showLongToast
import org.florisboard.lib.android.showLongToastSync
import org.florisboard.lib.compose.stringRes
import org.florisboard.lib.kotlin.io.readJson
import org.florisboard.lib.kotlin.io.subDir
import org.florisboard.lib.kotlin.io.subFile

object Restore {
    const val MIN_VERSION_CODE = 64
    // Dictate's own application id (base, without the .debug/.beta variant suffixes) — a backup whose
    // metadata package starts with this is a genuine Dictate backup, not a "third-party" one. Was still
    // the upstream FlorisBoard id after the fork, which made every Dictate backup falsely warn.
    const val PACKAGE_NAME = "net.devemperor.dictate"
    const val BACKUP_ARCHIVE_FILE_NAME = "backup.zip"
}

/**
 * The preference import with the device's own credentials filled into whatever gaps the archive leaves
 * (issue #367).
 *
 * JetPref hands the whole document over in one `read`, so one pass is the whole job — and doing it here
 * rather than by rewriting the unpacked file leaves the archive in the cache exactly as it arrived.
 */
private class SecretPreservingReader(
    private val delegate: DataStoreReader,
    private val local: ProviderAccounts,
) : DataStoreReader {
    override suspend fun read() = BackupRedaction.restoreSecrets(delegate.read(), local)
}

@Composable
fun RestoreScreen() = UnisonoPage(title = stringRes(R.string.backup_and_restore__restore__title)) {
    val c = LocalUnisono.current

    val prefs by FlorisPreferenceStore
    val navController = LocalNavController.current
    val context = LocalContext.current
    val cacheManager by context.cacheManager()

    val restoreFilesSelector = remember { Backup.FilesSelector() }
    var importStrategy by remember { mutableStateOf(ImportStrategy.Merge) }
    // TODO: rememberCoroutineScope() is unusable because it provides the scope in a cancelled state, which does
    //  not make sense at all. I suspect that this is a bug and once it is resolved we can use it here again.
    val restoreScope = remember { CoroutineScope(Dispatchers.Main) }
    var restoreWorkspace by remember {
        mutableStateOf<CacheManager.BackupAndRestoreWorkspace?>(null)
    }

    val restoreDataFromFileSystemLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            runCatching {
                restoreWorkspace?.close()
                restoreWorkspace = null
                val workspace = cacheManager.backupAndRestore.new()
                workspace.zipFile = workspace.inputDir.subFile(Restore.BACKUP_ARCHIVE_FILE_NAME)
                context.contentResolver.readToFile(uri, workspace.zipFile)
                ZipUtils.unzip(workspace.zipFile, workspace.outputDir)
                workspace.metadata = try {
                    workspace.outputDir.subFile(Backup.METADATA_JSON_NAME).readJson()
                } catch (e: FileNotFoundException) {
                    error("Invalid archive: either backup_metadata.json is missing or file is not a ZIP archive.")
                }
                workspace.restoreWarningId = when {
                    workspace.metadata.versionCode != BuildConfig.VERSION_CODE -> {
                        R.string.backup_and_restore__restore__metadata_warn_different_version
                    }
                    !workspace.metadata.packageName.startsWith(Restore.PACKAGE_NAME) -> {
                        R.string.backup_and_restore__restore__metadata_warn_different_vendor
                    }
                    else -> null
                }
                workspace.restoreErrorId = when {
                    workspace.metadata.packageName.isBlank() || workspace.metadata.versionCode < Restore.MIN_VERSION_CODE -> {
                        R.string.backup_and_restore__restore__metadata_error_invalid_metadata
                    }
                    else -> null
                }
                // Say up front that no keys will come back from this one (issue #367). Found by looking at
                // the keyring the archive carries rather than at a flag, so it is equally true of an archive
                // written by any version — and of one whose owner simply never set a key up.
                workspace.restoreCredentialsMissing = runCatching {
                    val prefsFile = workspace.outputDir
                        .subDir(AndroidAppDataStorage.JETPREF_DIR_NAME)
                        .subFile("${FlorisPreferenceModel.NAME}.${AndroidAppDataStorage.JETPREF_FILE_EXT}")
                    prefsFile.exists() &&
                        BackupRedaction.accountsFrom(prefsFile.readText())?.hasNoSecrets == true
                }.getOrDefault(false)
                restoreWorkspace = workspace
            }.onFailure { error ->
                context.showLongToastSync(
                    R.string.backup_and_restore__restore__failure,
                    "error_message" to error.localizedMessage,
                )
            }
        },
    )

    suspend fun performRestore() {
        val workspace = restoreWorkspace!!
        val shouldReset = importStrategy == ImportStrategy.Erase
        if (restoreFilesSelector.jetprefDatastore) {
            val file = workspace.outputDir
                .subDir(AndroidAppDataStorage.JETPREF_DIR_NAME)
                .subFile("${FlorisPreferenceModel.NAME}.${AndroidAppDataStorage.JETPREF_FILE_EXT}")
            if (file.exists()) {
                val fileBasedStorage = FileBasedStorage(file.path)
                // The keyring is a single preference, so merging an archive whose keys were left out would
                // replace real keys with nothing (issue #367). Fill its gaps from what is already here on
                // the way in. Erase is left alone: wiping is what that mode was chosen for.
                val reader = if (importStrategy == ImportStrategy.Merge) {
                    SecretPreservingReader(fileBasedStorage, prefs.dictate.providerAccounts.get())
                } else {
                    fileBasedStorage
                }
                FlorisPreferenceStore.import(importStrategy, reader).getOrThrow()
            }
        }
        if (restoreFilesSelector.dictatePrompts) {
            val promptsFile = workspace.outputDir.subDir("dictate").subFile(Backup.DICTATE_PROMPTS_JSON_NAME)
            if (promptsFile.exists()) {
                val prompts = promptsFile.readJson<List<PromptModel>>()
                val db = PromptsDatabaseHelper.getInstance(context)
                if (shouldReset) {
                    // Erase mode: replace the whole prompt list with the backed-up one.
                    db.replaceAll(prompts)
                } else {
                    // Merge mode: append the backed-up prompts after the existing ones.
                    db.addAll(prompts)
                }
            }
        }
        if (restoreFilesSelector.dictateHistory) {
            // Older backups (before the history feature) simply have no such file → skipped, staying
            // compatible. Erase replaces the store, Merge appends.
            val dictateDir = workspace.outputDir.subDir("dictate")
            val historyFile = dictateDir.subFile(Backup.DICTATE_HISTORY_JSON_NAME)
            if (historyFile.exists()) {
                val entries = historyFile.readJson<List<DictateHistoryEntry>>()
                val audioDir = dictateDir.subDir(Backup.DICTATE_HISTORY_AUDIO_DIR)
                DictateHistoryStore.importEntries(
                    context, entries, audioDir.takeIf { it.exists() }, replace = shouldReset,
                )
            }
        }
    }

    UGroupLabel(stringRes(R.string.backup_and_restore__restore__mode))
    UCard {
        RadioListItem(
            onClick = { importStrategy = ImportStrategy.Merge },
            selected = importStrategy == ImportStrategy.Merge,
            text = stringRes(R.string.backup_and_restore__restore__mode_merge),
        )
        UDivider()
        RadioListItem(
            onClick = { importStrategy = ImportStrategy.Erase },
            selected = importStrategy == ImportStrategy.Erase,
            text = stringRes(R.string.backup_and_restore__restore__mode_erase_and_overwrite),
        )
    }
    UButton(
        text = stringRes(R.string.action__select_file),
        primary = false,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            runCatching {
                restoreDataFromFileSystemLauncher.launch("*/*")
            }.onFailure { error ->
                context.showLongToastSync(
                    R.string.backup_and_restore__restore__failure,
                    "error_message" to error.localizedMessage,
                )
            }
        },
    )
    val workspace = restoreWorkspace
    if (workspace == null) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            text = stringRes(R.string.state__no_file_selected),
            style = Unisono.Label,
            color = c.textTertiary,
        )
    } else {
        UGroupLabel(stringRes(R.string.backup_and_restore__restore__metadata))
        UCard {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(workspace.metadata.packageName, style = Unisono.Mono, color = c.textPrimary)
                Text(
                    "${workspace.metadata.versionName} (${workspace.metadata.versionCode})",
                    style = Unisono.Mono,
                    color = c.textPrimary,
                )
                Text(
                    remember(workspace.metadata.timestamp) {
                        val formatter = DateFormat.getDateTimeInstance()
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = workspace.metadata.timestamp
                        formatter.format(calendar.time)
                    },
                    style = Unisono.Mono,
                    color = c.textPrimary,
                )
            }
        }
        if (workspace.restoreErrorId != null) {
            UNotice(stringRes(workspace.restoreErrorId!!), warning = true)
        } else if (workspace.restoreWarningId != null) {
            UNotice(stringRes(workspace.restoreWarningId!!), warning = true)
        }
        // A plain statement of fact, not a warning: an archive can legitimately carry no keys, and the
        // point is only that the user learns it here rather than after the device change.
        if (workspace.restoreErrorId == null && workspace.restoreCredentialsMissing) {
            UNotice(stringRes(R.string.backup_and_restore__restore__metadata_note_no_credentials))
        }
        if (workspace.restoreErrorId == null) {
            BackupFilesSelector(
                filesSelector = restoreFilesSelector,
                title = stringRes(R.string.backup_and_restore__restore__files),
                showCredentials = false,
            )
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UButton(
            text = stringRes(R.string.action__cancel),
            primary = false,
            modifier = Modifier.weight(1f),
            onClick = {
                restoreWorkspace?.close()
                navController.navigateUp()
            },
        )
        UButton(
            text = stringRes(R.string.action__restore),
            modifier = Modifier.weight(1f),
            enabled = restoreWorkspace != null && restoreWorkspace?.restoreErrorId == null,
            onClick = {
                restoreScope.launch(Dispatchers.Main) {
                    try {
                        performRestore()
                        context.showLongToast(R.string.backup_and_restore__restore__success)
                        navController.navigateUp()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        context.showLongToast(
                            R.string.backup_and_restore__restore__failure,
                            "error_message" to e.localizedMessage,
                        )
                    }
                }
            },
        )
    }
}
