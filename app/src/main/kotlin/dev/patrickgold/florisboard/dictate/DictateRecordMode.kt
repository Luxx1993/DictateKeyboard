/*
 * Copyright (C) 2026 Lukas Klenk (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate

/**
 * How the floating button records, chosen in Einstellungen › Aufnahme.
 *
 * [TAP]: tap to start, tap again to stop and send; holding the button opens the prompt menu.
 * [HOLD]: press and hold to record, release to send; a short tap opens the prompt menu instead.
 */
enum class DictateRecordMode {
    TAP,
    HOLD;
}
