/*
 * Copyright (C) 2026 Luxx1993
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.provider

import java.util.Locale

/**
 * List prices for speech recognition in euros per hour of audio, so the model picker can show what a model
 * costs. Pay-as-you-go prices from each provider's own price page, read on 2026-10-21 and converted at
 * 1 EUR = 1.1215 USD (Gladia, Scaleway and OVHcloud list in euros). Speaker labels and custom vocabulary
 * add a surcharge at some providers and are not included.
 *
 * Only what was found is listed; a model without an entry shows no price rather than a guess. [approx]
 * marks figures that are calculated from token prices or come from third-party sources.
 */
object ModelPrices {

    data class Price(val eurPerHour: Double, val approx: Boolean = false)

    private fun p(value: Double, approx: Boolean = false) = Price(value, approx)

    /** File transcription, keyed `provider id|model id` (lower case). */
    private val batch: Map<String, Price> = mapOf(
        "openrouter|openai/whisper-large-v3-turbo" to p(0.01),
        "openrouter|openai/whisper-large-v3" to p(0.03),
        "openrouter|nvidia/parakeet-tdt-0.6b-v3" to p(0.08),
        "openrouter|microsoft/mai-transcribe-2" to p(0.09),
        "groq|whisper-large-v3-turbo" to p(0.04),
        "groq|whisper-large-v3" to p(0.10),
        "ovhcloud|whisper-large-v3-turbo" to p(0.05),
        "ovhcloud|whisper-large-v3" to p(0.15),
        "scaleway|whisper-large-v3" to p(0.18),
        "together|openai/whisper-large-v3" to p(0.08),
        "together|nvidia/parakeet-tdt-0.6b-v3" to p(0.08),
        "xai|grok-voice-transcribe-2.0" to p(0.09, approx = true),
        "soniox|stt-async-v5" to p(0.09),
        "azure|mai-transcribe-2" to p(0.09),
        "azure|mai-transcribe-1.5" to p(0.32),
        "assemblyai|universal-2" to p(0.13),
        "assemblyai|universal-3-5-pro" to p(0.19),
        "assemblyai|universal-3-5-pro${ProviderRegistry.ASSEMBLYAI_SYNC_SUFFIX}" to p(0.40, approx = true),
        "gladia|solaria-3" to p(0.16),
        "gladia|solaria-1" to p(0.27),
        "openai|gpt-4o-mini-transcribe" to p(0.16),
        "openai|gpt-transcribe" to p(0.24, approx = true),
        "openai|gpt-4o-transcribe" to p(0.32),
        "openai|gpt-4o-transcribe-diarize" to p(0.32),
        "mistral|voxtral-mini-latest" to p(0.19, approx = true),
        "mistral|voxtral-mini-transcribe-3-0" to p(0.19),
        "mistral|voxtral-small-transcribe-3-0" to p(0.21),
        "elevenlabs|scribe_v2" to p(0.20),
        "deepgram|nova-3" to p(0.23),
        "gemini|gemini-3.5-transcribe" to p(0.27, approx = true),
        "gemini|gemini-3.6-flash" to p(0.23, approx = true),
        "gemini|gemini-3.8-flash" to p(0.23, approx = true),
        "gemini|gemini-3.5-flash-lite" to p(0.06, approx = true),
    )

    /** Live transcription, which providers usually bill higher than files. */
    private val realtime: Map<String, Price> = mapOf(
        "soniox|stt-rt-v5" to p(0.11),
        "assemblyai|universal-streaming-english" to p(0.13),
        "assemblyai|universal-streaming-multilingual" to p(0.13),
        "assemblyai|universal-3-6-pro" to p(0.40),
        "xai|grok-voice-transcribe-2.0" to p(0.18),
        "deepgram|nova-2" to p(0.31),
        "deepgram|nova-3" to p(0.41, approx = true),
        "deepgram|flux-general-en" to p(0.35, approx = true),
        "deepgram|flux-general-multi" to p(0.35, approx = true),
        "elevenlabs|scribe_v2_realtime" to p(0.35),
        "mistral|voxtral-mini-transcribe-realtime-3-0" to p(0.43),
        "mistral|voxtral-mini-transcribe-realtime-2602" to p(0.43, approx = true),
        "gladia|solaria-1" to p(0.68),
    )

    fun batchPrice(providerId: String, model: String): Price? = batch[key(providerId, model)]

    fun realtimePrice(providerId: String, model: String): Price? = realtime[key(providerId, model)]

    /** `0,160 €/h` (2 to 4 decimals by amount), or `ca. 0,400 €/h` for a calculated figure; null when nothing is known. */
    fun format(price: Price?, locale: Locale = Locale.getDefault()): String? {
        price ?: return null
        // Small amounts get more decimals so the cheap models stay tellable apart.
        val decimals = when {
            price.eurPerHour >= 1.0 -> 2
            price.eurPerHour >= 0.1 -> 3
            else -> 4
        }
        val number = String.format(locale, "%.${decimals}f", price.eurPerHour)
        return (if (price.approx) "ca. " else "") + "$number €/h"
    }

    private fun key(providerId: String, model: String) = "$providerId|${model.trim().lowercase()}"
}
