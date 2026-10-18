/*
 * Copyright (C) 2026 Lukas Klenk (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.apptheme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.R

/**
 * The "Unisono" design system of Entwurf 3: warm paper and ink, Manrope and IBM Plex Mono, quiet surfaces
 * (docs/design/diktat-design.md §2). Actions are ink-coloured; there is no accent colour in buttons.
 */
@Immutable
data class UnisonoColors(
    val canvas: Color,
    val raised: Color,
    val sunken: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val action: Color,
    val onAction: Color,
    val providerElevenLabs: Color,
    val providerClaude: Color,
    val success: Color,
    val isDark: Boolean,
)

object Unisono {
    val Light = UnisonoColors(
        canvas = Color(0xFFF4F1EC),
        raised = Color(0xFFFFFFFF),
        sunken = Color(0xFFECE7E0),
        textPrimary = Color(0xFF1F1C1A),
        textSecondary = Color(0xFF5E5852),
        textTertiary = Color(0xFF6F6962),
        borderSubtle = Color(0x141F1C1A),
        borderStrong = Color(0x291F1C1A),
        action = Color(0xFF1F1C1A),
        onAction = Color(0xFFF4F1EC),
        providerElevenLabs = Color(0xFFD69A4E),
        providerClaude = Color(0xFFC07A7A),
        success = Color(0xFF357A5B),
        isDark = false,
    )

    val Dark = UnisonoColors(
        canvas = Color(0xFF121110),
        raised = Color(0xFF1B1A18),
        sunken = Color(0xFF2A2825),
        textPrimary = Color(0xFFF2EEE8),
        textSecondary = Color(0xFFA9A29A),
        textTertiary = Color(0xFF857E77),
        borderSubtle = Color(0x14F2EEE8),
        borderStrong = Color(0x29F2EEE8),
        action = Color(0xFFF2EEE8),
        onAction = Color(0xFF121110),
        providerElevenLabs = Color(0xFFD69A4E),
        providerClaude = Color(0xFFC07A7A),
        success = Color(0xFF5FA884),
        isDark = true,
    )

    val Manrope = FontFamily(
        Font(R.font.manrope_light, FontWeight.Light),
        Font(R.font.manrope_regular, FontWeight.Normal),
        Font(R.font.manrope_medium, FontWeight.Medium),
        Font(R.font.manrope_semibold, FontWeight.SemiBold),
        Font(R.font.manrope_bold, FontWeight.Bold),
    )

    val PlexMono = FontFamily(Font(R.font.ibm_plex_mono_regular, FontWeight.Normal))

    val RadiusMd = RoundedCornerShape(10.dp)
    val RadiusLg = RoundedCornerShape(16.dp)
    val Pill = RoundedCornerShape(999.dp)

    /** Page headline: all pages put theirs on the same edge (40 dp from the top). */
    val PageTitle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).em,
    )
    val PageSubtitle = TextStyle(fontFamily = Manrope, fontSize = 15.sp, lineHeight = 22.sp)
    val SectionTitle = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp)
    val Body = TextStyle(fontFamily = Manrope, fontSize = 15.sp, lineHeight = 21.sp)
    val BodyStrong = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp)
    val Label = TextStyle(fontFamily = Manrope, fontSize = 13.sp, lineHeight = 18.sp)
    val Caption = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp)
    val StatNumber = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Light, fontSize = 34.sp, lineHeight = 40.sp)
    val Mono = TextStyle(fontFamily = PlexMono, fontSize = 12.sp, lineHeight = 16.sp)
    val NavLabel = TextStyle(fontFamily = Manrope, fontSize = 12.sp, lineHeight = 16.sp)

    /** Material colours derived from the tokens, so screens not yet redrawn already sit on paper and ink. */
    fun colorScheme(c: UnisonoColors): ColorScheme {
        val outline = c.borderStrong.compositeOver(c.canvas)
        val outlineVariant = c.borderSubtle.compositeOver(c.canvas)
        return if (c.isDark) {
            darkColorScheme(
                primary = c.action, onPrimary = c.onAction,
                primaryContainer = c.sunken, onPrimaryContainer = c.textPrimary,
                secondary = c.textSecondary, onSecondary = c.canvas,
                secondaryContainer = c.sunken, onSecondaryContainer = c.textPrimary,
                tertiary = c.success, onTertiary = c.canvas,
                background = c.canvas, onBackground = c.textPrimary,
                surface = c.canvas, onSurface = c.textPrimary,
                surfaceVariant = c.sunken, onSurfaceVariant = c.textSecondary,
                surfaceContainerLowest = c.canvas, surfaceContainerLow = c.raised,
                surfaceContainer = c.raised, surfaceContainerHigh = c.sunken, surfaceContainerHighest = c.sunken,
                surfaceBright = c.raised, surfaceDim = c.canvas,
                outline = outline, outlineVariant = outlineVariant,
                inverseSurface = c.textPrimary, inverseOnSurface = c.canvas, inversePrimary = c.canvas,
            )
        } else {
            lightColorScheme(
                primary = c.action, onPrimary = c.onAction,
                primaryContainer = c.sunken, onPrimaryContainer = c.textPrimary,
                secondary = c.textSecondary, onSecondary = c.canvas,
                secondaryContainer = c.sunken, onSecondaryContainer = c.textPrimary,
                tertiary = c.success, onTertiary = c.canvas,
                background = c.canvas, onBackground = c.textPrimary,
                surface = c.canvas, onSurface = c.textPrimary,
                surfaceVariant = c.sunken, onSurfaceVariant = c.textSecondary,
                surfaceContainerLowest = c.raised, surfaceContainerLow = c.raised,
                surfaceContainer = c.raised, surfaceContainerHigh = c.sunken, surfaceContainerHighest = c.sunken,
                surfaceBright = c.raised, surfaceDim = c.sunken,
                outline = outline, outlineVariant = outlineVariant,
                inverseSurface = c.textPrimary, inverseOnSurface = c.canvas, inversePrimary = c.canvas,
            )
        }
    }

    /** Material typography in Manrope throughout. */
    val Typography: Typography = Typography().let { t ->
        fun TextStyle.m() = copy(fontFamily = Manrope)
        Typography(
            displayLarge = t.displayLarge.m().copy(fontWeight = FontWeight.Light),
            displayMedium = t.displayMedium.m().copy(fontWeight = FontWeight.Light),
            displaySmall = t.displaySmall.m().copy(fontWeight = FontWeight.Light),
            headlineLarge = t.headlineLarge.m().copy(fontWeight = FontWeight.Light),
            headlineMedium = t.headlineMedium.m().copy(fontWeight = FontWeight.Light),
            headlineSmall = t.headlineSmall.m().copy(fontWeight = FontWeight.Light),
            titleLarge = t.titleLarge.m(),
            titleMedium = t.titleMedium.m().copy(fontWeight = FontWeight.SemiBold),
            titleSmall = t.titleSmall.m().copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = t.bodyLarge.m(),
            bodyMedium = t.bodyMedium.m(),
            bodySmall = t.bodySmall.m(),
            labelLarge = t.labelLarge.m().copy(fontWeight = FontWeight.SemiBold),
            labelMedium = t.labelMedium.m().copy(fontWeight = FontWeight.SemiBold),
            labelSmall = t.labelSmall.m(),
        )
    }
}

/** The tokens of the theme currently in effect; set by [FlorisAppTheme]. */
val LocalUnisono = staticCompositionLocalOf { Unisono.Light }
