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
        "openrouter|openai/whisper-large-v3-turbo" to p(0.0096),
        "openrouter|openai/whisper-large-v3" to p(0.0257),
        "openrouter|nvidia/parakeet-tdt-0.6b-v3" to p(0.0802),
        "openrouter|microsoft/mai-transcribe-2" to p(0.0892),
        "openrouter|openai/gpt-transcribe" to p(0.2407),
        "openrouter|elevenlabs/scribe-v2" to p(0.1958),
        "openrouter|x-ai/grok-stt-1.0" to p(0.0899),
        "openrouter|mistralai/voxtral-mini-transcribe" to p(0.1605),
        "openrouter|google/gemini-3.5-transcribe" to p(0.2675, approx = true),
        "openrouter|deepgram/nova-3" to p(0.2311, approx = true),
        "openrouter|assemblyai/universal-3-6-pro" to p(0.2664, approx = true),
        "groq|whisper-large-v3-turbo" to p(0.0357),
        "groq|whisper-large-v3" to p(0.099),
        "ovhcloud|whisper-large-v3-turbo" to p(0.046),
        "ovhcloud|whisper-large-v3" to p(0.147),
        "scaleway|whisper-large-v3" to p(0.18),
        "together|openai/whisper-large-v3" to p(0.0802),
        "together|nvidia/parakeet-tdt-0.6b-v3" to p(0.0802),
        "xai|grok-voice-transcribe-2.0" to p(0.0892, approx = true),
        "soniox|stt-async-v5" to p(0.087, approx = true),
        "azure|mai-transcribe-2" to p(0.0892),
        "azure|mai-transcribe-1.5" to p(0.321),
        "assemblyai|universal-2" to p(0.1337),
        "assemblyai|universal-3-5-pro" to p(0.1872),
        "assemblyai|universal-3-5-pro${ProviderRegistry.ASSEMBLYAI_SYNC_SUFFIX}" to p(0.4012, approx = true),
        "gladia|solaria-3" to p(0.16),
        "gladia|solaria-1" to p(0.27),
        "openai|gpt-4o-mini-transcribe" to p(0.1605),
        "openai|gpt-transcribe" to p(0.2407, approx = true),
        "openai|gpt-4o-transcribe" to p(0.321),
        "openai|gpt-4o-transcribe-diarize" to p(0.321),
        "mistral|voxtral-mini-latest" to p(0.1872, approx = true),
        "mistral|voxtral-mini-transcribe-3-0" to p(0.1872),
        "mistral|voxtral-small-transcribe-3-0" to p(0.214),
        "elevenlabs|scribe_v2" to p(0.1962),
        "deepgram|nova-3" to p(0.23),
        "gemini|gemini-3.5-transcribe" to p(0.2675, approx = true),
        "gemini|gemini-3.6-flash" to p(0.2318, approx = true),
        "gemini|gemini-3.8-flash" to p(0.2318, approx = true),
        "gemini|gemini-3.5-flash-lite" to p(0.058, approx = true),
    )

    /** Live transcription, which providers usually bill higher than files. */
    private val realtime: Map<String, Price> = mapOf(
        "soniox|stt-rt-v5" to p(0.107),
        "assemblyai|universal-streaming-english" to p(0.1337),
        "assemblyai|universal-streaming-multilingual" to p(0.1337),
        "assemblyai|universal-3-6-pro" to p(0.4012),
        "xai|grok-voice-transcribe-2.0" to p(0.1783, approx = true),
        "deepgram|nova-2" to p(0.3121),
        "deepgram|nova-3" to p(0.2568, approx = true),
        "deepgram|flux-general-en" to p(0.3477, approx = true),
        "deepgram|flux-general-multi" to p(0.4173),
        "elevenlabs|scribe_v2_realtime" to p(0.3477),
        "mistral|voxtral-mini-transcribe-realtime-3-0" to p(0.428),
        "mistral|voxtral-mini-transcribe-realtime-2602" to p(0.428, approx = true),
        "gladia|solaria-1" to p(0.68),
    )

    fun batchPrice(providerId: String, model: String): Price? = batch[key(providerId, model)]

    fun realtimePrice(providerId: String, model: String): Price? = realtime[key(providerId, model)]

    /**
     * `0,16 €/h`, `0,015 €/h` or `0,0096 €/h`: the exact figure with 2 to 4 decimals, trailing zeros dropped.
     * `ca. 0,40 €/h` for a calculated figure; null when nothing is known.
     */
    fun format(price: Price?, locale: Locale = Locale.getDefault()): String? {
        price ?: return null
        val number = String.format(locale, "%.4f", price.eurPerHour).let { text ->
            val separator = text.indexOfFirst { it == ',' || it == '.' }
            text.trimEnd('0').let { if (it.length - separator - 1 < 2) text.take(separator + 3) else it }
        }
        return (if (price.approx) "ca. " else "") + "$number €/h"
    }

    private fun key(providerId: String, model: String) = "$providerId|${model.trim().lowercase()}"
}
