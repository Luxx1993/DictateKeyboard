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

package dev.patrickgold.florisboard.app.apptheme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.patrickgold.florisboard.app.AppTheme

/**
 * The app's theme: the Unisono tokens of Entwurf 3 (see [Unisono]) in light or dark, mapped onto Material
 * so that every screen sits on paper and ink. The AMOLED variants keep the dark tokens on true black.
 */
@Composable
fun FlorisAppTheme(
    theme: AppTheme,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (theme) {
        AppTheme.AUTO, AppTheme.AUTO_AMOLED -> systemDark
        AppTheme.DARK, AppTheme.AMOLED_DARK -> true
        AppTheme.LIGHT -> false
    }
    val amoled = theme == AppTheme.AMOLED_DARK || (theme == AppTheme.AUTO_AMOLED && systemDark)
    val tokens = when {
        amoled -> Unisono.Dark.copy(canvas = Color.Black)
        darkTheme -> Unisono.Dark
        else -> Unisono.Light
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalUnisono provides tokens) {
        MaterialTheme(
            colorScheme = Unisono.colorScheme(tokens),
            typography = Unisono.Typography,
            content = content,
        )
    }
}
