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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** The keyboard's own fields behaving like text fields (issue #424). */
class FieldTextTest : FunSpec({

    test("typing goes in at the cursor, and over a marked stretch replaces it") {
        FieldText("Hllo", cursor = 1).insert("a") shouldBe FieldText("Hallo", 2)
        FieldText("Hallo Welt", cursor = 10, selection = 6..10).insert("du") shouldBe FieldText("Hallo du", 8)
        FieldText("abc").selectAll().insert("x") shouldBe FieldText("x", 1)
    }

    test("the word at the cursor is the run of letters it stands in or right after") {
        FieldText("Ich komme heute", cursor = 15).wordRange() shouldBe 10..15
        FieldText("Ich komme heute", cursor = 6).wordRange() shouldBe 4..9
        FieldText("Ich komme ", cursor = 10).wordRange() shouldBe 10..10
        FieldText("Hallo, Welt", cursor = 5).wordRange() shouldBe 0..5
        FieldText("don't", cursor = 5).wordRange() shouldBe 0..5
        FieldText("", cursor = 0).wordRange() shouldBe 0..0
    }
})
