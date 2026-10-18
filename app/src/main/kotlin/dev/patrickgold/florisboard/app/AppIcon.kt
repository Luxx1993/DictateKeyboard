/*
 * Copyright (C) 2026 Lukas Klenk (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Colour of the launcher icon "Welle um die Welt" (docs/design/diktat-design.md §5). The icon has no
 * background, so it is white for dark wallpapers and black for light ones. Android cannot pick one by the
 * wallpaper, so the user does; white is the default.
 */
enum class AppIconColor {
    WHITE,
    BLACK,
}

/**
 * The launcher entry is one of two activity aliases that differ only in their icon. Exactly one is enabled;
 * hiding the icon (Android 9 and older only) disables both.
 */
object AppIcon {
    private const val ALIAS_WHITE = "dev.patrickgold.florisboard.SettingsLauncherAlias"
    private const val ALIAS_BLACK = "dev.patrickgold.florisboard.SettingsLauncherAliasBlack"

    fun apply(context: Context, color: AppIconColor, visible: Boolean = true) {
        setEnabled(context, ALIAS_WHITE, visible && color == AppIconColor.WHITE)
        setEnabled(context, ALIAS_BLACK, visible && color == AppIconColor.BLACK)
    }

    private fun setEnabled(context: Context, alias: String, enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        val component = ComponentName(context, alias)
        // Writing the state the component already has still makes some launchers redraw the icon.
        if (context.packageManager.getComponentEnabledSetting(component) == state) return
        context.packageManager.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
    }
}
