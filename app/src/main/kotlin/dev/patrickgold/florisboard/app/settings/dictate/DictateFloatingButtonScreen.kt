/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.dictate

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.Routes
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.BubblePreview
import dev.patrickgold.florisboard.app.main.PaletteRow
import dev.patrickgold.florisboard.app.main.StylePreview
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UCheckRow
import dev.patrickgold.florisboard.app.main.UChoice
import dev.patrickgold.florisboard.app.main.UChoiceRow
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UGroupLabel
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.USwitch
import dev.patrickgold.florisboard.app.main.USwitchRow
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.dictate.DictateFloatingButtonAppScope
import dev.patrickgold.florisboard.dictate.DictateFloatingButtonDesign
import dev.patrickgold.florisboard.dictate.DictateFloatingButtonShowWhen
import dev.patrickgold.florisboard.dictate.DictateFloatingButtonSize
import dev.patrickgold.florisboard.dictate.overlay.BubbleMenuAction
import dev.patrickgold.florisboard.dictate.overlay.DictateAccessibilityService
import dev.patrickgold.florisboard.dictate.ui.CloudPalette
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.color.ColorMappings
import org.florisboard.lib.compose.stringRes

/**
 * Settings for the floating dictation button (issue #88), in the look of Entwurf 3. The in-app master
 * toggle hides/shows the bubble; the bubble only actually appears once the [DictateAccessibilityService] is
 * also enabled in the system accessibility settings, which the user reaches from here after a prominent
 * disclosure of what the service does. The service-enabled status is re-read on every resume so it
 * reflects changes made in the system settings. Everything below the setup only shows once both are on.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DictateFloatingButtonScreen() = UnisonoPage(title = stringRes(R.string.main__settings_floating)) {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val context = LocalContext.current
    val navController = LocalNavController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val enabled by prefs.dictate.floatingButtonEnabled.collectAsState()
    val design by prefs.dictate.floatingButtonDesign.collectAsState()
    val appScope by prefs.dictate.floatingButtonAppScope.collectAsState()
    val apps by prefs.dictate.floatingButtonApps.collectAsState()
    val appCount = apps.packages.size

    // Opening this screen clears the "New" badge on the Dictate settings entry.
    LaunchedEffect(Unit) { prefs.dictate.floatingButtonHintSeen.set(true) }

    var serviceEnabled by remember { mutableStateOf(isOverlayServiceEnabled(context)) }
    var micGranted by remember { mutableStateOf(isMicGranted(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceEnabled = isOverlayServiceEnabled(context)
                micGranted = isMicGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> micGranted = granted }

    var showDisclosure by remember { mutableStateOf(false) }
    var openDialog by remember { mutableStateOf<String?>(null) }

    // The button itself, and the master switch.
    UCard(Modifier.settingsSearchAnchor("dictate__floating_button_enable_title")) {
        val label = stringRes(R.string.dictate__floating_button_enable_title)
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BubblePreview(design, size = 56.dp)
            Column(Modifier.weight(1f)) {
                Text(label, style = Unisono.BodyStrong.copy(fontSize = 17.sp, lineHeight = 24.sp), color = c.textPrimary)
                Text(stringRes(R.string.dictate__floating_button_enable_summary), style = Unisono.Label, color = c.textSecondary)
            }
            USwitch(
                checked = enabled,
                onCheckedChange = { scope.launch { prefs.dictate.floatingButtonEnabled.set(it) } },
                contentDescription = label,
            )
        }
    }

    if (enabled) {
        // Setup first, right under the master toggle: the accessibility service (and mic) must be on before
        // any of the display/behavior options matter, so those are only shown once it is enabled.
        UGroupLabel(stringRes(R.string.dictate__floating_button_permission_group))
        UCard {
            Box(Modifier.settingsSearchAnchor("dictate__floating_button_service_title")) {
                UNavRow(
                    title = stringRes(R.string.dictate__floating_button_service_title),
                    subtitle = stringRes(
                        if (serviceEnabled) {
                            R.string.dictate__floating_button_service_enabled
                        } else {
                            R.string.dictate__floating_button_service_disabled
                        },
                    ),
                    onClick = { if (serviceEnabled) openAccessibilitySettings(context) else showDisclosure = true },
                )
            }
            // The bubble records through the same pipeline as the voice input, so it needs the mic permission.
            if (!micGranted) {
                UDivider()
                Box(Modifier.settingsSearchAnchor("dictate__floating_button_mic_title")) {
                    UNavRow(
                        title = stringRes(R.string.dictate__floating_button_mic_title),
                        subtitle = stringRes(R.string.dictate__floating_button_mic_summary),
                        onClick = { requestMic.launch(Manifest.permission.RECORD_AUDIO) },
                    )
                }
            }
        }
    }

    if (enabled && serviceEnabled) {
        UGroupLabel(stringRes(R.string.main__fb_group_look))
        UCard {
            UChoiceRow(
                title = stringRes(R.string.dictate__floating_button_design_title),
                description = stringRes(R.string.main__fb_preview_note),
                choices = listOf(
                    designChoice(DictateFloatingButtonDesign.CLOUD, R.string.dictate__floating_button_design_cloud, R.string.dictate__floating_button_design_cloud_summary),
                    designChoice(DictateFloatingButtonDesign.PILL, R.string.dictate__floating_button_design_pill, R.string.dictate__floating_button_design_pill_summary),
                    designChoice(DictateFloatingButtonDesign.RING, R.string.dictate__floating_button_design_ring, R.string.dictate__floating_button_design_ring_summary),
                    designChoice(DictateFloatingButtonDesign.ORB, R.string.dictate__floating_button_design_orb, R.string.dictate__floating_button_design_orb_summary),
                    designChoice(DictateFloatingButtonDesign.AURORA, R.string.dictate__floating_button_design_aurora, R.string.dictate__floating_button_design_aurora_summary),
                    designChoice(DictateFloatingButtonDesign.LATTICE, R.string.dictate__floating_button_design_lattice, R.string.dictate__floating_button_design_lattice_summary),
                ),
                selected = design,
                onSelect = { scope.launch { prefs.dictate.floatingButtonDesign.set(it) } },
                open = openDialog == "design",
                onOpenChange = { openDialog = if (it) "design" else null },
                modifier = Modifier.settingsSearchAnchor("dictate__floating_button_design_title"),
            )
            UDivider()
            Box(Modifier.settingsSearchAnchor("dictate__floating_button_color_title")) { PaletteRow() }
            UDivider()
            val size by prefs.dictate.floatingButtonSize.collectAsState()
            UChoiceRow(
                title = stringRes(R.string.dictate__floating_button_size_title),
                choices = listOf(
                    UChoice(DictateFloatingButtonSize.SMALL, stringRes(R.string.dictate__floating_button_size_small)),
                    UChoice(DictateFloatingButtonSize.MEDIUM, stringRes(R.string.dictate__floating_button_size_medium)),
                    UChoice(DictateFloatingButtonSize.LARGE, stringRes(R.string.dictate__floating_button_size_large)),
                ),
                selected = size,
                onSelect = { scope.launch { prefs.dictate.floatingButtonSize.set(it) } },
                open = openDialog == "size",
                onOpenChange = { openDialog = if (it) "size" else null },
                modifier = Modifier.settingsSearchAnchor("dictate__floating_button_size_title"),
            )
        }

        UGroupLabel(stringRes(R.string.main__fb_group_behaviour))
        UCard {
            // When it appears (issue #439), one choice where there used to be two switches about keyboards.
            val showWhen by prefs.dictate.floatingButtonShowWhen.collectAsState()
            UChoiceRow(
                title = stringRes(R.string.dictate__floating_button_show_when_title),
                choices = listOf(
                    UChoice(DictateFloatingButtonShowWhen.FIELD_SELECTED, stringRes(R.string.dictate__floating_button_show_when_field)),
                    UChoice(DictateFloatingButtonShowWhen.KEYBOARD_OPEN, stringRes(R.string.dictate__floating_button_show_when_keyboard)),
                ),
                selected = showWhen,
                onSelect = { scope.launch { prefs.dictate.floatingButtonShowWhen.set(it) } },
                open = openDialog == "showWhen",
                onOpenChange = { openDialog = if (it) "showWhen" else null },
                modifier = Modifier.settingsSearchAnchor("dictate__floating_button_show_when_title"),
            )
            UDivider()
            // Where it may appear at all (issue #392): a banking app that objects to overlays, or an app
            // that already has a microphone of its own.
            Box(Modifier.settingsSearchAnchor("dictate__floating_button_apps_title")) {
                UNavRow(
                    title = stringRes(R.string.dictate__floating_button_apps_title),
                    subtitle = when (appScope) {
                        DictateFloatingButtonAppScope.ALL ->
                            stringRes(R.string.dictate__floating_button_apps_summary_all)
                        DictateFloatingButtonAppScope.ONLY_SELECTED ->
                            stringRes(R.string.dictate__floating_button_apps_summary_only, "n" to appCount)
                        DictateFloatingButtonAppScope.EXCEPT_SELECTED ->
                            stringRes(R.string.dictate__floating_button_apps_summary_except, "n" to appCount)
                    },
                    onClick = { navController.navigate(Routes.Settings.DictateFloatingButtonApps) },
                )
            }
            UDivider()
            PrefSwitch(
                prefs.dictate.floatingButtonSnapToEdge, "dictate__floating_button_snap_title",
                R.string.dictate__floating_button_snap_title,
                R.string.dictate__floating_button_snap_summary_on, R.string.dictate__floating_button_snap_summary_off,
            )
            UDivider()
            PrefSwitch(
                prefs.dictate.floatingButtonAutoDim, "dictate__floating_button_auto_dim_title",
                R.string.dictate__floating_button_auto_dim_title,
                R.string.dictate__floating_button_auto_dim_summary_on, R.string.dictate__floating_button_auto_dim_summary_off,
            )
            UDivider()
            PrefSwitch(
                prefs.dictate.floatingButtonRememberPosition, "dictate__floating_button_remember_position_title",
                R.string.dictate__floating_button_remember_position_title,
                R.string.dictate__floating_button_remember_position_summary_on,
                R.string.dictate__floating_button_remember_position_summary_off,
            )
            UDivider()
            PrefSwitch(
                prefs.dictate.floatingButtonHaptic, "dictate__floating_button_haptic_title",
                R.string.dictate__floating_button_haptic_title,
                R.string.dictate__floating_button_haptic_summary_on, R.string.dictate__floating_button_haptic_summary_off,
            )
        }

        UGroupLabel(stringRes(R.string.main__fb_group_after))
        UCard {
            PrefSwitch(
                prefs.dictate.floatingButtonUndoEnabled, "dictate__floating_button_undo_title",
                R.string.dictate__floating_button_undo_title,
                R.string.dictate__floating_button_undo_summary_on, R.string.dictate__floating_button_undo_summary_off,
            )
            UDivider()
            PrefSwitch(
                prefs.dictate.floatingButtonCopyToClipboard, "dictate__floating_button_copy_to_clipboard_title",
                R.string.dictate__floating_button_copy_to_clipboard_title,
                R.string.dictate__floating_button_copy_to_clipboard_summary_on,
                R.string.dictate__floating_button_copy_to_clipboard_summary_off,
            )
            UDivider()
            // What holding the button offers besides the prompts (issue #408). One row rather than a switch
            // each: these are the contents of a single list, and the summary is the list as it stands.
            val menuActions = BubbleMenuAction.entries.filter { it.pref(prefs).collectAsState().value }
            Box(Modifier.settingsSearchAnchor("dictate__floating_button_menu_group")) {
                UNavRow(
                    title = stringRes(R.string.dictate__floating_button_menu_group),
                    subtitle = if (menuActions.isEmpty()) {
                        stringRes(R.string.quick_action__noop)
                    } else {
                        menuActions.map { stringRes(it.labelRes) }.joinToString()
                    },
                    onClick = { openDialog = "menu" },
                )
            }
        }

        if (openDialog == "menu") {
            UDialog(
                title = stringRes(R.string.dictate__floating_button_menu_group),
                onDismiss = { openDialog = null },
            ) {
                // Ticking takes effect at once; there is nothing here a second confirmation could protect.
                BubbleMenuAction.entries.forEachIndexed { i, action ->
                    if (i > 0) UDivider()
                    val pref = action.pref(prefs)
                    val checked by pref.collectAsState()
                    UCheckRow(
                        title = stringRes(action.labelRes),
                        checked = checked,
                        onCheckedChange = { scope.launch { pref.set(it) } },
                    )
                }
            }
        }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text(stringRes(R.string.dictate__floating_button_disclosure_title)) },
            text = { Text(stringRes(R.string.dictate__floating_button_disclosure_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDisclosure = false
                    openAccessibilitySettings(context)
                }) {
                    Text(stringRes(R.string.dictate__floating_button_disclosure_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) {
                    Text(stringRes(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun designChoice(design: DictateFloatingButtonDesign, titleRes: Int, summaryRes: Int) = UChoice(
    value = design,
    title = stringRes(titleRes),
    subtitle = stringRes(summaryRes),
    leading = { StylePreview(design, size = 48.dp) },
)

/** A switch row bound to [pref], with the summary for each state and its search anchor. */
@Composable
internal fun PrefSwitch(
    pref: PreferenceData<Boolean>,
    anchor: String,
    titleRes: Int,
    summaryOnRes: Int,
    summaryOffRes: Int = summaryOnRes,
) {
    val scope = rememberCoroutineScope()
    val value by pref.collectAsState()
    USwitchRow(
        title = stringRes(titleRes),
        subtitle = stringRes(if (value) summaryOnRes else summaryOffRes),
        checked = value,
        onCheckedChange = { scope.launch { pref.set(it) } },
        modifier = Modifier.settingsSearchAnchor(anchor),
    )
}

/** Whether the Dictate accessibility service is currently enabled in the system accessibility settings. */
internal fun isOverlayServiceEnabled(context: Context): Boolean {
    val flattened = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    // ComponentName picks up the running package (incl. the .debug suffix) automatically.
    val component = ComponentName(context, DictateAccessibilityService::class.java).flattenToString()
    return flattened.split(':').any { it.equals(component, ignoreCase = true) }
}

private fun isMicGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

internal fun openAccessibilitySettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
