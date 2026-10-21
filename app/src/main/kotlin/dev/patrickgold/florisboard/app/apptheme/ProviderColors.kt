/*
 * Copyright (C) 2026 Luxx1993
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.apptheme

import androidx.compose.ui.graphics.Color

/**
 * The colour of a provider's dot (browser app: `--pk` in css/app.css). The hue is the provider's brand colour from
 * its official logo, saturation and lightness are pulled to the muted tones of the Unisono design (sage, fjord,
 * rosewood). ElevenLabs is white and xAI is black as their marks are; both swap in the dark theme. Accounts of a
 * custom endpoint, Ollama and the on-device model have no brand and keep the neutral grey.
 */
object ProviderColors {

    private val brand: Map<String, Color> = mapOf(
        "elevenlabs" to Color(0xFFF7F4EE),
        "xai" to Color(0xFF1F1C1A),
        "openai" to Color(0xFF55B39C),
        "groq" to Color(0xFFBC6B5E),
        "openrouter" to Color(0xFF6565BB),
        "gemini" to Color(0xFF5B84BF),
        "anthropic" to Color(0xFFB27B68),
        "together" to Color(0xFF5881BE),
        "deepinfra" to Color(0xFF688DB2),
        "deepgram" to Color(0xFF5BB992),
        "soniox" to Color(0xFF6071A5),
        "azure" to Color(0xFF528EBC),
        "deepseek" to Color(0xFF5E6FC0),
        "siliconflow" to Color(0xFF7C5CBC),
        "scaleway" to Color(0xFF834FB7),
        "ovhcloud" to Color(0xFF5580AD),
        "cerebras" to Color(0xFFBA745D),
        "gladia" to Color(0xFF7664C0),
        "assemblyai" to Color(0xFF6170B2),
        "mistral" to Color(0xFFBC7559),
    )

    /** Dark theme: the dark tones are lifted, white and black swap. */
    private val darkOverrides: Map<String, Color> = mapOf(
        "xai" to Color(0xFFF2EEE8),
        "soniox" to Color(0xFF8996BC),
        "scaleway" to Color(0xFFA37CC9),
        "ovhcloud" to Color(0xFF81A1C2),
    )

    /** The dot colour for [providerId], or null when the provider has no brand colour. */
    fun dot(providerId: String, dark: Boolean): Color? =
        (if (dark) darkOverrides[providerId] else null) ?: brand[providerId]
}

/** The dot of [providerId] in this theme, grey for providers without a brand. */
fun UnisonoColors.providerDot(providerId: String): Color =
    ProviderColors.dot(providerId, isDark) ?: textTertiary
