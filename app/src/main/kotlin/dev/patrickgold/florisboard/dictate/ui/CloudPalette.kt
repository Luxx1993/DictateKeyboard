/*
 * Copyright (C) 2026 Lukas Klenk (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.ui

import androidx.annotation.StringRes
import dev.patrickgold.florisboard.R

/**
 * The colours of the cloud (docs/design/diktat-design.md §4): [o0] is the ground of the sphere, [o1] to [o4]
 * the four smoke blobs drifting in it. The floating button stores one colour, the palette's main tone [o1];
 * [forColor] turns that back into the whole palette.
 */
class CloudColors(val o0: Int, val o1: Int, val o2: Int, val o3: Int, val o4: Int)

enum class CloudPalette(@StringRes val label: Int, val colors: CloudColors) {
    LAGOON(R.string.main__palette_lagoon, CloudColors(0xFF9AD7E6.i, 0xFF3FA9C8.i, 0xFF1E6F8E.i, 0xFFDDF4EF.i, 0xFF86D3B7.i)),
    PERIWINKLE(R.string.main__palette_periwinkle, CloudColors(0xFFB8C7F9.i, 0xFF7A8FFB.i, 0xFF5C63FB.i, 0xFFE3EBFE.i, 0xFFC9B8F9.i)),
    SAGE(R.string.main__palette_sage, CloudColors(0xFFB5D9A8.i, 0xFF6BA86F.i, 0xFF2F6A52.i, 0xFFEDF3CF.i, 0xFFD2E48F.i)),
    SUNSET(R.string.main__palette_sunset, CloudColors(0xFFEDBFCF.i, 0xFFC96F9D.i, 0xFF7B3F80.i, 0xFFF8E1D2.i, 0xFFEBA57E.i)),
    INK(R.string.main__palette_ink, CloudColors(0xFFCFC8BF.i, 0xFF6F6962.i, 0xFF1F1C1A.i, 0xFFF4F1EC.i, 0xFFA59E96.i));

    /** The tone the floating button stores for this palette. */
    val main: Int get() = colors.o1

    companion object {
        /** The standard palette. */
        val DEFAULT = LAGOON

        /** Dictate's old default button colour, which never was a choice of its own: it reads as the standard. */
        private const val LEGACY_DEFAULT = 0xFF30B7E6.toInt()

        /** The palette stored as [argb], or null for a colour picked outside the five. */
        fun of(argb: Int): CloudPalette? = entries.firstOrNull { it.main == argb }

        /**
         * The colours for a stored button colour: its palette, or for any other colour a palette mixed from
         * it the way the five are built (lighter ground, deep tone, pale highlight).
         */
        fun forColor(argb: Int): CloudColors {
            if (argb == LEGACY_DEFAULT) return DEFAULT.colors
            of(argb)?.let { return it.colors }
            return CloudColors(
                o0 = mix(argb, WHITE, 0.45f),
                o1 = argb or 0xFF000000.toInt(),
                o2 = mix(argb, BLACK, 0.40f),
                o3 = mix(argb, WHITE, 0.82f),
                o4 = mix(argb, WHITE, 0.30f),
            )
        }

        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val BLACK = 0xFF000000.toInt()

        private fun mix(a: Int, b: Int, t: Float): Int {
            fun ch(shift: Int): Int {
                val x = (a shr shift) and 0xFF
                val y = (b shr shift) and 0xFF
                return (x + (y - x) * t).toInt().coerceIn(0, 255)
            }
            return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }
    }
}

private val Long.i: Int get() = toInt()
