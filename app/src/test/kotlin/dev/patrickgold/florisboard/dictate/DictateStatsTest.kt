/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate

import dev.patrickgold.florisboard.dictate.data.stats.DictateStats
import kotlin.test.Test
import kotlin.test.assertEquals

class DictateStatsTest {

    @Test
    fun wordCountHandlesWhitespaceSeparatedText() {
        assertEquals(0, DictateStats.wordCount(""))
        assertEquals(0, DictateStats.wordCount(" \n\t "))
        assertEquals(1, DictateStats.wordCount("hello"))
        assertEquals(2, DictateStats.wordCount("  hello   world  "))
        assertEquals(3, DictateStats.wordCount("one\ntwo\tthree"))
    }

    @Test
    fun wordCountTreatsUnspacedCjkAsOneToken() {
        assertEquals(1, DictateStats.wordCount("你好世界"))
    }

    @Test
    fun monthCountEndsWithItsMonth() {
        val oct31 = java.time.LocalDate.of(2026, 10, 31).toEpochDay()
        val month = DictateStats.monthIndex(oct31)
        assertEquals(2026 * 12 + 9, month)
        assertEquals(120L, DictateStats.wordsThisMonth(month, 120L, today = oct31))
        assertEquals(0L, DictateStats.wordsThisMonth(month, 120L, today = oct31 + 1))
    }

    @Test
    fun streakLapsesAfterAMissedDay() {
        val today = 20_000L
        assertEquals(4, DictateStats.currentStreak(today, 4, today))
        assertEquals(4, DictateStats.currentStreak(today - 1, 4, today))
        assertEquals(0, DictateStats.currentStreak(today - 2, 4, today))
    }
}
