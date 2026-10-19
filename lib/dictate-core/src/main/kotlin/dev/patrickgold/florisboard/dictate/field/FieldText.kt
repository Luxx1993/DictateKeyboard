/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.field

/**
 * The text of one of the keyboard's own fields — the translate bar, the emoji, GIF, sticker and clipboard
 * searches (issue #424) — with its cursor and an optional marked stretch.
 *
 * These fields have no InputConnection: the keyboard keeps their text itself and draws it. Everything an
 * app's editor would do for them — insert at the cursor, take a suggestion, undo an autocorrection, show a
 * live dictation — is done here, as plain functions on a value, so each rule exists once and is tested.
 *
 * [selection] is `start..end` with `end` **exclusive** (an [IntRange] is used for its two numbers, not for
 * iteration); it is `null` when nothing is marked.
 */
data class FieldText(
    val text: String,
    val cursor: Int = text.length,
    val selection: IntRange? = null,
) {
    init {
        require(cursor in 0..text.length) { "cursor $cursor outside 0..${text.length}" }
    }

    /** The marked text, or empty. */
    val selectedText: String
        get() = selection?.let { text.substring(it.first, it.last) }.orEmpty()

    /** [insertion] typed at the cursor, over the marked stretch if there is one. */
    fun insert(insertion: String): FieldText {
        val (from, to) = selection?.let { it.first to it.last } ?: (cursor to cursor)
        return FieldText(text.replaceRange(from, to, insertion), from + insertion.length)
    }

    /** The whole text marked, so the next [insert] replaces it (what "select all" means in an editor). */
    fun selectAll(): FieldText = copy(selection = 0..text.length)

    /**
     * The word the cursor stands in or right after, as `start..end` with `end` exclusive — empty
     * (`start == end == cursor`) between words. A word is letters, digits and combining marks (a
     * Devanagari vowel sign is a mark, not a letter), joined by apostrophes and hyphens; a punctuation
     * mark after it is not part of it.
     */
    fun wordRange(): IntRange {
        var start = cursor
        while (start > 0 && isWordChar(text[start - 1])) start--
        var end = cursor
        while (end < text.length && isWordChar(text[end])) end++
        return start..end
    }


}

