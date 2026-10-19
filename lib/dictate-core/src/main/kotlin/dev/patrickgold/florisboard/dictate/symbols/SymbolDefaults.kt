/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.symbols

/**
 * What the user's own symbol pages hold before they change anything (issue #342): the built-in western
 * pages, key for key and with their long presses, so switching the pages on changes nothing until a key
 * is changed. The one difference is that the brackets no longer swap in right-to-left text, which the
 * built-in layout does with a direction selector; a user's key types what it shows.
 */
object SymbolDefaults {
    fun key(page: Int, index: Int): SymbolKey = PAGES[page][index]

    private fun t(text: String) = SymbolValue.Text(text)

    private fun k(text: String, vararg longPress: String) = SymbolKey(t(text), longPress.map(::t))

    private fun currency(slot: Int, vararg longPress: Int) =
        SymbolKey(SymbolValue.Currency(slot), longPress.map { SymbolValue.Currency(it) })

    private val PAGE_1: List<SymbolKey> = listOf(
        k("@"),
        k("#", "№"),
        currency(1, 2, 6, 3, 4, 5),
        k("%", "‰", "℅"),
        k("&"),
        k("-", "_", "⁻", "—", "–", "·"),
        k("+", "±", "⁺"),
        k("(", "<", "[", "{"),
        k(")", ">", "]", "}"),
        k("/"),

        k("*", "†", "★", "‡"),
        k("\"", "”", "„", "“", "«", "»"),
        k("'", "’", "‚", "‘", "‹", "›"),
        k(":", "⋮"),
        k(";"),
        k("!", "¡"),
        k("?", "¿", "‽"),
    )

    private val PAGE_2: List<SymbolKey> = listOf(
        k("~"),
        k("`"),
        k("|"),
        k("•", "♪", "♣", "♠", "♥", "♦"),
        k("√"),
        k("π", "Π", "ω", "α", "β", "Ω", "μ"),
        k("÷"),
        k("×"),
        k("¶", "§"),
        k("∆"),

        currency(5),
        currency(4),
        currency(3),
        currency(2),
        k("^", "↑", "←", "↓", "→"),
        k("°", "′", "″"),
        k("=", "≠", "∞", "≈"),
        k("{", "("),
        k("}", ")"),
        k("\\"),

        k("_"),
        k("©"),
        k("®"),
        k("™"),
        k("✓"),
        k("["),
        k("]"),
    )

    val PAGES: List<List<SymbolKey>> = listOf(PAGE_1, PAGE_2)

    init {
        for (page in PAGES.indices) {
            check(PAGES[page].size == CustomSymbols.keyCount(page)) { "Default page ${page + 1} has the wrong size" }
        }
    }
}

