/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.data.stats

import dev.patrickgold.florisboard.app.FlorisPreferenceModel
import java.time.LocalDate

/**
 * Lifetime dictation statistics (issue #142). All state lives in dedicated, never-auto-reset JetPref
 * prefs (see [FlorisPreferenceModel.Dictate]); this object is the single write path so the keyboard and
 * the floating button feed the same counters. Reads happen reactively in `DictateStatsScreen`.
 */
object DictateStats {

    /** Days shown by the activity chart / kept in the rolling per-day store. */
    const val DAILY_WINDOW_DAYS = 7

    /** Average phone typing speed (words per minute) — the baseline for the "time saved" estimate. */
    const val TYPING_WPM = 40.0

    /** A single day's word count, for the activity chart. */
    data class DayBar(val epochDay: Long, val words: Long)


    /** Estimated seconds saved versus typing the same words on a keyboard. */
    fun savedSeconds(words: Long, spokenSeconds: Long): Double =
        (words / TYPING_WPM * 60.0 - spokenSeconds).coerceAtLeast(0.0)

    /**
     * Records one successful dictation: bumps the counters, stamps first-use, extends the day streak and
     * folds today's word count into the rolling 7-day store. Blank output is ignored (nothing was typed).
     */
    suspend fun recordDictation(
        prefs: FlorisPreferenceModel,
        text: String,
        spokenSeconds: Long,
        nowMs: Long = System.currentTimeMillis(),
        today: Long = LocalDate.now().toEpochDay(),
    ) {
        if (text.isBlank()) return
        val d = prefs.dictate
        val words = wordCount(text).toLong()
        d.statsDictations.set(d.statsDictations.get() + 1)
        d.statsWords.set(d.statsWords.get() + words)
        d.statsChars.set(d.statsChars.get() + text.length)
        d.statsSpokenSeconds.set(d.statsSpokenSeconds.get() + spokenSeconds.coerceAtLeast(0L))
        if (d.statsFirstUseEpochMs.get() == 0L) d.statsFirstUseEpochMs.set(nowMs)
        updateStreak(d, today)
        addDailyWords(d, today, words)
    }

    /** Records that one AI rewording/prompt pass ran. */
    suspend fun recordRewording(prefs: FlorisPreferenceModel) {
        val d = prefs.dictate
        d.statsRewordings.set(d.statsRewordings.get() + 1)
    }

    /** Clears every statistic back to its default (user-triggered from the stats screen). */
    suspend fun reset(prefs: FlorisPreferenceModel) {
        val d = prefs.dictate
        d.statsDictations.set(0L)
        d.statsWords.set(0L)
        d.statsChars.set(0L)
        d.statsSpokenSeconds.set(0L)
        d.statsRewordings.set(0L)
        d.statsFirstUseEpochMs.set(0L)
        d.statsLastDayEpoch.set(0L)
        d.statsStreakCurrent.set(0)
        d.statsStreakBest.set(0)
        d.statsDaily.set("")
    }

    /** Whitespace-delimited word count; CJK text (no spaces) collapses to a single token, which is fine. */
    fun wordCount(text: String): Int {
        var count = 0
        var inWord = false
        for (ch in text) {
            if (ch.isWhitespace()) {
                inWord = false
            } else if (!inWord) {
                count++
                inWord = true
            }
        }
        return count
    }

    /**
     * Words per day for the last [DAILY_WINDOW_DAYS] days, oldest → newest and aligned to [today] so the
     * chart always shows a fixed, gap-filled window (missing days = 0).
     */
    fun activity(raw: String, today: Long = LocalDate.now().toEpochDay()): List<DayBar> {
        val map = parseDaily(raw)
        return (0 until DAILY_WINDOW_DAYS).map { i ->
            val day = today - (DAILY_WINDOW_DAYS - 1 - i)
            DayBar(day, map[day] ?: 0L)
        }
    }

    private suspend fun updateStreak(d: FlorisPreferenceModel.Dictate, today: Long) {
        val last = d.statsLastDayEpoch.get()
        val current = when {
            last == today -> d.statsStreakCurrent.get().coerceAtLeast(1)
            last == today - 1L -> d.statsStreakCurrent.get() + 1
            else -> 1
        }
        d.statsStreakCurrent.set(current)
        if (current > d.statsStreakBest.get()) d.statsStreakBest.set(current)
        d.statsLastDayEpoch.set(today)
    }

    private suspend fun addDailyWords(d: FlorisPreferenceModel.Dictate, today: Long, words: Long) {
        val map = parseDaily(d.statsDaily.get()).toMutableMap()
        map[today] = (map[today] ?: 0L) + words
        val cutoff = today - (DAILY_WINDOW_DAYS - 1)
        d.statsDaily.set(serializeDaily(map.filterKeys { it >= cutoff }))
    }

    private fun parseDaily(raw: String): Map<Long, Long> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(';').mapNotNull { entry ->
            val parts = entry.split(':')
            val day = parts.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
            val words = parts.getOrNull(1)?.toLongOrNull() ?: return@mapNotNull null
            day to words
        }.toMap()
    }

    private fun serializeDaily(map: Map<Long, Long>): String =
        map.entries.sortedBy { it.key }.joinToString(";") { "${it.key}:${it.value}" }
}
