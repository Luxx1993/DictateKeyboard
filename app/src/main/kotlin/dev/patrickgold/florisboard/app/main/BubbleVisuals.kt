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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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

/** The preview image of [design] at rest, drawn after the code (see docs/design/diktat-design.md §4). */
internal fun previewRes(design: DictateFloatingButtonDesign): Int = when (design) {
    DictateFloatingButtonDesign.CLOUD -> R.drawable.preview_bubble_cloud
    DictateFloatingButtonDesign.PILL -> R.drawable.preview_bubble_pill
    DictateFloatingButtonDesign.RING -> R.drawable.preview_bubble_ring
    DictateFloatingButtonDesign.ORB -> R.drawable.preview_bubble_orb
    DictateFloatingButtonDesign.AURORA -> R.drawable.preview_bubble_aurora
    DictateFloatingButtonDesign.LATTICE -> R.drawable.preview_bubble_lattice
}

/**
 * The floating button as it looks at rest: the live cloud in the chosen palette for the cloud, otherwise
 * the style's preview image (in Lagune).
 */
@Composable
internal fun BubblePreview(design: DictateFloatingButtonDesign, size: Dp) {
    val prefs by FlorisPreferenceStore
    val color by prefs.dictate.floatingButtonColor.collectAsState()
    if (design == DictateFloatingButtonDesign.CLOUD) {
        CloudSwatch(CloudPalette.forColor(color.toArgb()), size = size, animated = true)
    } else {
        StylePreview(design, size)
    }
}

/** A style's preview image on a small paper disc, so its soft shadow reads the same in light and dark. */
@Composable
internal fun StylePreview(design: DictateFloatingButtonDesign, size: Dp) {
    Box(
        Modifier.size(size).clip(Unisono.Pill).background(Unisono.Light.canvas),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(previewRes(design)), contentDescription = null, modifier = Modifier.size(size))
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

