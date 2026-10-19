/*
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.main

import android.view.View
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.ColorUtils
import dev.patrickgold.florisboard.dictate.ui.DictateAuroraOrbView
import dev.patrickgold.florisboard.dictate.ui.DictateLatticeSphereView
import dev.patrickgold.florisboard.dictate.ui.GlassDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.dictate.DictateFloatingButtonDesign
import dev.patrickgold.florisboard.dictate.ui.AudioReactiveCloudOrbView
import dev.patrickgold.florisboard.dictate.ui.CloudColors
import dev.patrickgold.florisboard.dictate.ui.CloudPalette
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import org.florisboard.lib.compose.stringRes

/** The five cloud palettes as swatches, the chosen one ringed; picking one sets the button's colour. */
@Composable
internal fun PaletteRow() {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val scope = rememberCoroutineScope()
    val color by prefs.dictate.floatingButtonColor.collectAsState()
    val palette = CloudPalette.of(color.toArgb())
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringRes(R.string.main__settings_color),
            modifier = Modifier.weight(1f),
            style = Unisono.Body.copy(fontWeight = FontWeight.SemiBold),
            color = c.textPrimary,
        )
        for (p in CloudPalette.entries) {
            val label = stringRes(p.label)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(Unisono.Pill)
                    .selectable(
                        selected = p == palette,
                        role = Role.RadioButton,
                        onClick = { scope.launch { prefs.dictate.floatingButtonColor.set(Color(p.main)) } },
                    )
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                CloudSwatch(p.colors, size = 34.dp, animated = false, selected = p == palette)
            }
        }
    }
}

/**
 * The floating button as it looks at rest, in the chosen colour: every style is drawn by the same view or
 * drawable the button itself uses, so a new colour shows at once in all of them.
 */
@Composable
internal fun BubblePreview(design: DictateFloatingButtonDesign, size: Dp) {
    val prefs by FlorisPreferenceStore
    val color by prefs.dictate.floatingButtonColor.collectAsState()
    LiveBubble(design, size, color.toArgb())
}

/** A style's live preview on a small paper disc, so its glow reads the same in light and dark. */
@Composable
internal fun StylePreview(design: DictateFloatingButtonDesign, size: Dp) {
    val prefs by FlorisPreferenceStore
    val color by prefs.dictate.floatingButtonColor.collectAsState()
    Box(
        Modifier.size(size).clip(Unisono.Pill).background(Unisono.Light.canvas),
        contentAlignment = Alignment.Center,
    ) {
        LiveBubble(design, size, color.toArgb())
    }
}

/**
 * One style at rest. [size] stands for the button's 64 dp window; the body inside it is 40 dp for the ring and
 * 48 dp for the others, as in the overlay. The glass styles use [GlassDrawable] in the palette's three tones,
 * the aurora and the lattice their own views, the cloud its shader.
 */
@Composable
internal fun LiveBubble(design: DictateFloatingButtonDesign, size: Dp, colorArgb: Int) {
    val palette = remember(colorArgb) { CloudPalette.forColor(colorArgb) }
    val px = with(LocalDensity.current) { size.toPx() }
    val body = if (design == DictateFloatingButtonDesign.RING) 40f / 64f else 48f / 64f
    when (design) {
        DictateFloatingButtonDesign.CLOUD -> CloudSwatch(palette, size = size, animated = true)
        DictateFloatingButtonDesign.AURORA -> AndroidView(
            factory = { ctx -> DictateAuroraOrbView(ctx).apply { bodyRadius = px * body / 2f } },
            update = { it.setMood(DictateAuroraOrbView.Mood.IDLE, colorArgb) },
            modifier = Modifier.size(size),
        )
        DictateFloatingButtonDesign.LATTICE -> AndroidView(
            factory = { ctx ->
                DictateLatticeSphereView(ctx, sizeScale = size.value / 64f).apply { bodyDiameter = px * body }
            },
            update = { it.setMode(DictateLatticeSphereView.Mode.WEB, colorArgb) },
            modifier = Modifier.size(size),
        )
        else -> {
            val core = size * body
            val glyph = if (ColorUtils.calculateLuminance(palette.o1 or 0xFF000000.toInt()) > 0.7) Color.Black else Color.White
            Box(Modifier.size(size), contentAlignment = Alignment.Center) {
                if (design == DictateFloatingButtonDesign.RING) {
                    Canvas(Modifier.size(size)) {
                        drawCircle(
                            color = Color(palette.o1),
                            radius = this.size.minDimension * 25f / 64f,
                            style = Stroke(this.size.minDimension * 3f / 64f),
                        )
                    }
                }
                AndroidView(
                    factory = { ctx -> View(ctx) },
                    update = { it.background = GlassDrawable(palette.o0, palette.o1, palette.o2) },
                    modifier = Modifier.size(core),
                )
                Icon(
                    painterResource(R.drawable.ic_dictate_overlay_mic),
                    contentDescription = null,
                    tint = glyph,
                    modifier = Modifier.size(core / 2),
                )
            }
        }
    }
}

/**
 * The cloud itself, drawn by the floating button's own view: [animated] for the preview, a single still
 * frame for a swatch. [selected] adds the ink ring of the chosen swatch.
 */
@Composable
internal fun CloudSwatch(colors: CloudColors, size: Dp, animated: Boolean, selected: Boolean = false) {
    val ring = LocalUnisono.current.action
    val gap = LocalUnisono.current.raised
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                AudioReactiveCloudOrbView(context).apply {
                    fullBleed = true
                    still = !animated
                    setMode(AudioReactiveCloudOrbView.Mode.IDLE)
                }
            },
            update = { it.setColors(colors) },
            // Room for the ring around the chosen one, the prototype's 2 px gap and 1.5 px line.
            modifier = Modifier.size(size - 7.dp),
        )
        if (selected) {
            Canvas(Modifier.size(size)) {
                val r = this.size.minDimension / 2f
                drawCircle(color = gap, radius = r - 2.5.dp.toPx(), style = Stroke(2.dp.toPx()))
                drawCircle(color = ring, radius = r - 0.75.dp.toPx(), style = Stroke(1.5.dp.toPx()))
            }
        }
    }
}

