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


/**
 * Where a finished dictation/rewording is written, and how the focused field is read. Abstracts the
 * editor so the same dictation engine ([DictateController]) can output through the keyboard's own
 * InputConnection when Dictate is the active IME, or — for the floating overlay (issue #88) — through
 * an AccessibilityService-injected field, where no InputConnection is available.
 *
 * The IME-backed implementation ([ImeDictationSink]) mirrors the original direct EditorInstance usage
 * exactly, so routing every output through a sink is behavior-neutral for the keyboard path.
 */
interface DictationSink {
    /**
     * Inserts [text] at the cursor, replacing the active selection if any. Returns whether the write
     * actually landed: the keyboard path always succeeds, but the accessibility/overlay path can fail
     * silently on some app fields (Compose/WebView), so callers can avoid flashing a false success (#156).
     */
    fun commitText(text: String, verify: Boolean = true): Boolean

    /** The currently selected text, or empty when nothing is selected. */
    fun selectedText(): String

    /** The full text of the focused field (used by the "rework the whole field" path). */
    fun fullText(): String

    /** Selects the whole field so a subsequent [commitText] replaces its content. */
    fun selectAll()

    /**
     * Up to [length] characters in front of the cursor (in front of the selection, when there is one),
     * or null when the field cannot be read. Empty means the cursor sits at the start of the field —
     * the two are kept apart because only the second is a fact. Used to space a dictation against the
     * text it lands next to ([TranscriptJoin.separatorBefore]).
     */
    fun textBeforeCursor(length: Int): String?

    /**
     * Presses Enter / triggers the editor action (auto-enter, roadmap 10.1). Returns whether the field
     * accepted it — the keyboard dispatches a real key event and always does, while the overlay can only
     * *ask* the field, and an app that implements no editor action simply refuses (issue #278).
     */
    fun performEnter(): Boolean

    /**
     * Removes the last inserted [text] from the field again (undo, issue #133). Only deletes when the
     * text immediately before the cursor still matches [text], so the user's own edits are never eaten.
     * Returns true when the field accepted the removal.
     */
    fun deleteLastText(text: String): Boolean

    /**
     * Live real-time dictation preview (issue #128): reflect [newText] (the growing transcript) in the
     * field, applying only the minimal diff from the [prevText] already shown — so streaming words appear
     * in place, committed to the field. The overlay path has no cheap in-place update, so it skips the
     * preview and shows nothing until [commitDictationFinal].
     */
    fun setDictationPreview(newText: String, prevText: String)

    /**
     * Finalize: replace the [prevText] preview with the finished/reworded [finalText] (minimal diff).
     * Returns whether the field took it — the realtime path used to skip the insert-failure check
     * entirely, so a swallowed write ended in a green check (issue #277).
     */
    fun commitDictationFinal(finalText: String, prevText: String): Boolean

    /**
     * Remove the [prevText] preview entirely (a realtime recording was cancelled / fell back to batch) —
     * unless the field no longer [holds][holdsPreview] it: text the user has edited is theirs (issue #421).
     */
    fun clearDictationPreview(prevText: String)

    /**
     * Whether the field still ends, at the cursor, with [prevText] — the live preview as it was last
     * written. False once the user has edited it, moved the cursor away or cleared the field (issue #421):
     * every preview write is a diff against [prevText], and against a field that no longer holds it the
     * diff deletes the user's own words and types back the ones they removed.
     *
     * True when the field cannot be read, which is what every write assumed before. Paths that keep no
     * preview text of their own in a field answer true.
     */
    fun holdsPreview(prevText: String): Boolean = true
}
