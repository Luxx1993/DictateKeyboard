/*
 * Copyright (C) 2022-2025 The FlorisBoard Contributors
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

package dev.patrickgold.florisboard.app.devtools

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.settings.advanced.Backup
import dev.patrickgold.florisboard.clipboardManager
import dev.patrickgold.florisboard.lib.compose.FlorisScreen
import dev.patrickgold.florisboard.lib.devtools.Devtools
import org.florisboard.lib.android.showShortToastSync
import org.florisboard.lib.compose.FlorisButton
import org.florisboard.lib.compose.florisHorizontalScroll
import org.florisboard.lib.compose.florisScrollbar
import org.florisboard.lib.compose.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A log with logcat runs past 1 MB, more than one Binder transaction carries, and the clipboard then
 * throws TransactionTooLargeException. Text is sent as UTF-16, so this many characters stay well below.
 */
private const val CLIPBOARD_SAFE_CHARS = 200_000

/** The head (device and app info) and the newest lines, which are the ones that explain a problem. */
private fun clipboardSafe(text: String): Pair<String, Boolean> {
    if (text.length <= CLIPBOARD_SAFE_CHARS) return text to false
    val head = text.take(CLIPBOARD_SAFE_CHARS / 10)
    val tail = text.takeLast(CLIPBOARD_SAFE_CHARS - head.length)
    return "$head\n\n[…]\n\n$tail" to true
}

private suspend fun shareDebugLog(context: Context, text: String) {
    val file = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "debug-log").apply { mkdirs() }
        File(dir, "dictate-debug-log.txt").apply { writeText(text) }
    }
    val uri = FileProvider.getUriForFile(context, Backup.FILE_PROVIDER_AUTHORITY, file)
    val shareIntent = ShareCompat.IntentBuilder(context)
        .setStream(uri)
        .setType("text/plain")
        .createChooserIntent()
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(shareIntent)
}

// TODO: This screen is just a quick thrown-together thing and needs further enhancing in the UI
@Composable
fun ExportDebugLogScreen() = FlorisScreen {
    title = stringRes(R.string.devtools__debuglog__title)
    scrollable = false

    val prefs by FlorisPreferenceStore
    val context = LocalContext.current
    val resources = LocalResources.current
    val clipboardManager by context.clipboardManager()
    val scope = rememberCoroutineScope()

    fun copyToClipboard(text: String) {
        val (safeText, truncated) = clipboardSafe(text)
        clipboardManager.addNewPlaintext(safeText)
        context.showShortToastSync(
            resources.getString(
                if (truncated) R.string.devtools__debuglog__copied_truncated
                else R.string.devtools__debuglog__copied_to_clipboard,
            ),
        )
    }

    var debugLog by remember { mutableStateOf<List<String>?>(null) }
    var formattedDebugLog by remember { mutableStateOf<List<String>?>(null) }

    LaunchedEffect(Unit) {
        debugLog = Devtools.generateDebugLog(context, prefs, includeLogcat = true).lines()
        formattedDebugLog = Devtools.generateDebugLogForGithub(context, prefs, includeLogcat = true).lines()
    }

    bottomBar {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // The whole log as a file: unlike the clipboard it has no size limit.
            FlorisButton(
                onClick = {
                    val text = debugLog!!.joinToString("\n")
                    scope.launch { shareDebugLog(context, text) }
                },
                modifier = Modifier,
                text = stringRes(R.string.devtools__debuglog__share_log),
                enabled = debugLog != null,
            )
            FlorisButton(
                onClick = { copyToClipboard(formattedDebugLog!!.joinToString("\n")) },
                text = stringRes(R.string.devtools__debuglog__copy_for_github),
                enabled = debugLog != null,
            )
        }
    }

    content {
        // Forcing LTR because text displayed is a debug log
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .florisScrollbar(lazyListState, isVertical = true)
                    .florisHorizontalScroll(),
                state = lazyListState,
            ) {
                val log = debugLog
                if (log == null) {
                    item {
                        Text(stringRes(R.string.devtools__debuglog__loading))
                    }
                } else {
                    items(log) { logLine ->
                        Text(
                            text = logLine,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}
