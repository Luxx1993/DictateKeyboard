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

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.settings.dictate.isOverlayServiceEnabled
import dev.patrickgold.florisboard.dictate.DictateLanguages
import dev.patrickgold.florisboard.dictate.DictateRecordMode
import dev.patrickgold.florisboard.dictate.importer.TranscribeShareActivity
import dev.patrickgold.florisboard.dictate.provider.ProviderListing
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes

/**
 * The five colours of the cloud (docs/design/diktat-design.md §4), as the HTML references draw them:
 * light, main and deep tone plus the highlight. The main tone is what the floating button stores; the
 * shader itself is matched to the prototype in a later step.
 */
enum class CloudPalette(val label: Int, val light: Color, val main: Color, val deep: Color, val glow: Color) {
    LAGOON(R.string.main__palette_lagoon, Color(0xFF9AD7E6), Color(0xFF3FA9C8), Color(0xFF1E6F8E), Color(0xFFDDF4EF)),
    PERIWINKLE(R.string.main__palette_periwinkle, Color(0xFFB8C7F9), Color(0xFF7A8FFB), Color(0xFF5C63FB), Color(0xFFE3EBFE)),
    SAGE(R.string.main__palette_sage, Color(0xFFB5D9A8), Color(0xFF6BA86F), Color(0xFF2F6A52), Color(0xFFEDF3CF)),
    SUNSET(R.string.main__palette_sunset, Color(0xFFEDBFCF), Color(0xFFC96F9D), Color(0xFF7B3F80), Color(0xFFF8E1D2)),
    INK(R.string.main__palette_ink, Color(0xFFCFC8BF), Color(0xFF6F6962), Color(0xFF1F1C1A), Color(0xFFF4F1EC));

    companion object {
        /** The palette whose main tone is [color], or null for a colour picked elsewhere. */
        fun of(color: Color): CloudPalette? = entries.firstOrNull { it.main == color }
    }
}

/**
 * Einstellungen (Entwurf 3, `Einstellungen.html`): the floating button and its colour, then the dictation
 * settings and the rest in two groups. Each row opens the existing settings screen behind it; everything
 * else is under "Alle Einstellungen".
 */
@Composable
internal fun SettingsTab() {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    Text(stringRes(R.string.main__tab_settings), style = Unisono.PageTitle, color = c.textPrimary)

    // System state the user changes in Android's own settings, read again whenever the app comes back.
    var serviceEnabled by remember { mutableStateOf(isOverlayServiceEnabled(context)) }
    var voiceInputEnabled by remember { mutableStateOf(isVoiceInputEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceEnabled = isOverlayServiceEnabled(context)
                voiceInputEnabled = isVoiceInputEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val floatingEnabled by prefs.dictate.floatingButtonEnabled.collectAsState()
    val color by prefs.dictate.floatingButtonColor.collectAsState()
    val palette = CloudPalette.of(color)
    UCard {
        val floatingLabel = stringRes(R.string.main__settings_floating)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(Routes.Settings.DictateFloatingButton) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CloudSwatch(palette ?: CloudPalette.LAGOON, size = 56.dp, mainOverride = color.takeIf { palette == null })
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(floatingLabel, style = Unisono.BodyStrong.copy(fontSize = 17.sp, lineHeight = 24.sp), color = c.textPrimary)
                Text(
                    stringRes(
                        if (serviceEnabled) R.string.main__settings_floating_on else R.string.main__settings_floating_service_off,
                    ),
                    style = Unisono.Label,
                    color = c.textSecondary,
                )
            }
            USwitch(
                checked = floatingEnabled,
                onCheckedChange = { on ->
                    scope.launch { prefs.dictate.floatingButtonEnabled.set(on) }
                    // Without the accessibility service the switch alone shows nothing; its screen explains it.
                    if (on && !serviceEnabled) navController.navigate(Routes.Settings.DictateFloatingButton)
                },
                contentDescription = floatingLabel,
            )
        }
        UDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringRes(R.string.main__settings_color),
                modifier = Modifier.weight(1f),
                style = Unisono.Body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                color = c.textPrimary,
            )
            for (p in CloudPalette.entries) {
                val label = stringRes(p.label)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(Unisono.Pill)
                        .selectable(
                            selected = p == palette,
                            role = Role.RadioButton,
                            onClick = { scope.launch { prefs.dictate.floatingButtonColor.set(p.main) } },
                        )
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    CloudSwatch(p, size = 28.dp, selected = p == palette)
                }
            }
        }
    }

    UGroupLabel(stringRes(R.string.main__settings_group_dictation))
    val languagesRaw by prefs.dictate.inputLanguages.collectAsState()
    val accounts by prefs.dictate.providerAccounts.collectAsState()
    val transcriptionId by prefs.dictate.transcriptionProviderId.collectAsState()
    val rewordingId by prefs.dictate.rewordingProviderId.collectAsState()
    val rewordingEnabled by prefs.dictate.rewordingEnabled.collectAsState()
    val recordMode by prefs.dictate.recordMode.collectAsState()
    UCard {
        UNavRow(
            title = stringRes(R.string.main__settings_languages),
            subtitle = remember(languagesRaw) {
                DictateLanguages.parseSelection(languagesRaw).joinToString(", ") { it.displayName() }
            },
            icon = UnisonoIcons.Globe,
            onClick = { navController.navigate(Routes.Settings.DictateLanguages) },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.main__settings_providers),
            subtitle = buildString {
                append(ProviderListing.displayNameOf(transcriptionId, accounts))
                if (rewordingEnabled) append(" · ").append(ProviderListing.displayNameOf(rewordingId, accounts))
            },
            icon = UnisonoIcons.Cloud,
            onClick = { navController.navigate(Routes.Settings.DictateProviders) },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.main__settings_recording),
            subtitle = stringRes(
                when (recordMode) {
                    DictateRecordMode.TAP -> R.string.dictate__record_mode_tap_short
                    DictateRecordMode.HOLD -> R.string.dictate__record_mode_hold_short
                },
            ),
            icon = UnisonoIcons.Wave,
            onClick = { navController.navigate(Routes.Settings.DictateRecording) },
        )
    }

    UGroupLabel(stringRes(R.string.main__settings_group_more))
    var showVoiceInput by remember { mutableStateOf(false) }
    var showBackup by remember { mutableStateOf(false) }
    UCard {
        UNavRow(
            title = stringRes(R.string.main__settings_transcribe),
            icon = UnisonoIcons.AudioFile,
            onClick = { context.startActivity(TranscribeShareActivity.pickIntent(context)) },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.main__settings_history),
            icon = UnisonoIcons.Clock,
            onClick = { navController.navigate(Routes.Settings.DictateHistory) },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.main__settings_voice_input),
            subtitle = stringRes(
                if (voiceInputEnabled) R.string.main__settings_voice_input_on else R.string.main__settings_voice_input_off,
            ),
            icon = UnisonoIcons.Keyboard,
            onClick = { showVoiceInput = true },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.main__settings_backup),
            icon = UnisonoIcons.Archive,
            onClick = { showBackup = true },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.main__settings_all),
            icon = UnisonoIcons.Gear,
            onClick = { navController.navigate(Routes.Settings.Home) },
        )
    }

    if (showVoiceInput) {
        AlertDialog(
            onDismissRequest = { showVoiceInput = false },
            title = { Text(stringRes(R.string.main__settings_voice_input)) },
            text = { Text(stringRes(R.string.main__settings_voice_input_dialog)) },
            confirmButton = {
                TextButton(onClick = {
                    showVoiceInput = false
                    context.startActivity(
                        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }) { Text(stringRes(R.string.main__settings_voice_input_open)) }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceInput = false }) { Text(stringRes(R.string.action__cancel)) }
            },
        )
    }
    if (showBackup) {
        AlertDialog(
            onDismissRequest = { showBackup = false },
            title = { Text(stringRes(R.string.main__settings_backup)) },
            confirmButton = {
                TextButton(onClick = {
                    showBackup = false
                    navController.navigate(Routes.Settings.Backup)
                }) { Text(stringRes(R.string.backup_and_restore__back_up__title)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackup = false
                    navController.navigate(Routes.Settings.Restore)
                }) { Text(stringRes(R.string.backup_and_restore__restore__title)) }
            },
        )
    }
}

/**
 * A still picture of the cloud in [palette]: a glassy sphere lit from the top left. [selected] draws the
 * ink ring of the chosen swatch; [mainOverride] tints it with a colour picked outside the five palettes.
 */
@Composable
private fun CloudSwatch(palette: CloudPalette, size: Dp, selected: Boolean = false, mainOverride: Color? = null) {
    val ring = LocalUnisono.current.action
    val gap = LocalUnisono.current.raised
    val main = mainOverride ?: palette.main
    Canvas(Modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val body = if (selected) r - 3.5.dp.toPx() else r
        drawCircle(
            brush = Brush.radialGradient(
                0f to palette.glow,
                0.35f to palette.light,
                0.75f to main,
                1f to palette.deep,
                center = Offset(center.x - body * 0.35f, center.y - body * 0.4f),
                radius = body * 1.6f,
            ),
            radius = body,
            center = center,
        )
        if (selected) {
            drawCircle(color = gap, radius = body + 1.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
            drawCircle(color = ring, radius = r - 0.75.dp.toPx(), center = center, style = Stroke(1.5.dp.toPx()))
        }
    }
}

/** Whether this app's voice input method is switched on in Android, which SwiftKey's microphone key uses. */
private fun isVoiceInputEnabled(context: Context): Boolean {
    val imm = context.getSystemService(InputMethodManager::class.java) ?: return false
    return runCatching { imm.enabledInputMethodList.any { it.packageName == context.packageName } }.getOrDefault(false)
}
