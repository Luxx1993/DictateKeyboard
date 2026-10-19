/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.dictate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.main.UChoice
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UGroupLabel
import dev.patrickgold.florisboard.app.main.UPrefChoice
import dev.patrickgold.florisboard.app.main.UPrefSwitch
import dev.patrickgold.florisboard.app.main.UPrefText
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.app.settings.search.settingsSearchAnchor
import dev.patrickgold.florisboard.dictate.provider.DictateProxyType
import dev.patrickgold.jetpref.datastore.model.collectAsState
import org.florisboard.lib.compose.stringRes

/**
 * Network proxy settings, in the look of Entwurf 3. The configured proxy is applied globally to *every*
 * outbound provider call (transcription, rewording, model listing and the connection test), so all traffic
 * to the configured AI endpoints can be routed through it (roadmap section 5.6). Disabled by default; the
 * host/port/auth fields only take effect while the proxy toggle is on (see `dictateProxyConfig`).
 */
@Composable
fun DictateProxyScreen() = UnisonoPage(title = stringRes(R.string.dictate__proxy_title)) {
    val prefs by FlorisPreferenceStore
    val enabled by prefs.dictate.proxyEnabled.collectAsState()

    UCard {
        UPrefSwitch(
            prefs.dictate.proxyEnabled,
            title = stringRes(R.string.dictate__proxy_enabled_title),
            subtitle = stringRes(R.string.dictate__proxy_enabled_summary),
            modifier = Modifier.settingsSearchAnchor("dictate__proxy_enabled_title"),
        )
    }

    if (enabled) {
        UGroupLabel(stringRes(R.string.dictate__proxy_server_group))
        UCard {
            UPrefChoice(
                prefs.dictate.proxyType,
                title = stringRes(R.string.dictate__proxy_type_title),
                choices = listOf(
                    UChoice(DictateProxyType.HTTP, stringRes(R.string.dictate__proxy_type_http)),
                    UChoice(DictateProxyType.SOCKS5, stringRes(R.string.dictate__proxy_type_socks5)),
                ),
                modifier = Modifier.settingsSearchAnchor("dictate__proxy_type_title"),
            )
            UDivider()
            UPrefText(
                prefs.dictate.proxyHost,
                title = stringRes(R.string.dictate__proxy_host_title),
                placeholder = stringRes(R.string.dictate__proxy_host_placeholder),
                keyboardType = KeyboardType.Uri,
            )
            UDivider()
            UPrefText(
                prefs.dictate.proxyPort,
                title = stringRes(R.string.dictate__proxy_port_title),
                placeholder = stringRes(R.string.dictate__proxy_port_placeholder),
                keyboardType = KeyboardType.Number,
            )
        }

        UGroupLabel(stringRes(R.string.dictate__proxy_auth_group))
        UCard {
            UPrefText(
                prefs.dictate.proxyUsername,
                title = stringRes(R.string.dictate__proxy_username_title),
            )
            UDivider()
            UPrefText(
                prefs.dictate.proxyPassword,
                title = stringRes(R.string.dictate__proxy_password_title),
                isSecret = true,
            )
        }
    }
    // "Trust user certificates" used to sit here, for the whole app. It moved into each account's editor
    // (#383), so that a private CA trusted for one server is not trusted for every provider.
}
