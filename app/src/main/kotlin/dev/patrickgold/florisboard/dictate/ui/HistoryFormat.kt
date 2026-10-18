/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.ui


fun formatHistoryDuration(seconds: Long): String? = when {
    seconds <= 0L -> null
    seconds < 60L -> "0:${seconds.toString().padStart(2, '0')}"
    else -> "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

fun formatHistorySize(bytes: Long): String? = when {
    bytes <= 0L -> null
    bytes < 1_000_000L -> "${(bytes / 1000L).coerceAtLeast(1L)} KB"
    else -> String.format("%.1f MB", bytes / 1_000_000.0)
}
