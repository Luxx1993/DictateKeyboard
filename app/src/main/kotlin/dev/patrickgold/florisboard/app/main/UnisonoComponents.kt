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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import androidx.compose.foundation.selection.toggleable

/*
 * The building blocks of Entwurf 3, measured off the HTML references in docs/design/entwurf-3/. Cards are
 * the raised surface with the hairline of `--elev-1` instead of a shadow; rows inside a card are split by
 * an inset divider.
 */

/** The headline every tab opens with, and its optional subtitle. */
@Composable
fun UPageHeader(title: String, subtitle: String? = null, onTitleClick: (() -> Unit)? = null) {
    val c = LocalUnisono.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            modifier = if (onTitleClick != null) {
                Modifier.clip(Unisono.RadiusMd).clickable(onClick = onTitleClick)
            } else {
                Modifier
            },
            style = Unisono.PageTitle,
            color = c.textPrimary,
        )
        if (subtitle != null) {
            Text(subtitle, style = Unisono.PageSubtitle, color = c.textSecondary)
        }
    }
}

/** A raised card with the hairline border of `--elev-1`. */
@Composable
fun UCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalUnisono.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Unisono.RadiusLg)
            .background(c.raised)
            .border(0.5.dp, c.borderSubtle, Unisono.RadiusLg),
        content = content,
    )
}

/** The inset line between two rows of a card. */
@Composable
fun UDivider() {
    Box(
        Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalUnisono.current.borderSubtle),
    )
}

/** A section heading: 18 sp semibold, with something small on the right if needed. */
@Composable
fun USectionTitle(text: String, trailing: (@Composable RowScope.() -> Unit)? = null) {
    val c = LocalUnisono.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = Unisono.BodyStrong.copy(fontSize = 18.sp, lineHeight = 26.sp),
            color = c.textPrimary,
        )
        trailing?.invoke(this)
    }
}

/** The small label above a group of rows on the settings tab ("Diktat", "Mehr"). */
@Composable
fun UGroupLabel(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 4.dp),
        style = Unisono.Caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.02.em),
        color = LocalUnisono.current.textSecondary,
    )
}

/** A row that leads somewhere: icon, title, optional subtitle, chevron. */
@Composable
fun UNavRow(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalUnisono.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = Unisono.BodyStrong.copy(lineHeight = 24.sp), color = c.textPrimary)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = Unisono.Label,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            UChevron()
        }
    }
}

@Composable
fun UChevron() {
    Icon(
        UnisonoIcons.Chevron,
        contentDescription = null,
        tint = LocalUnisono.current.textTertiary,
        modifier = Modifier.size(16.dp),
    )
}

/** The ink switch of Entwurf 3: 52 × 32, a pill in ink when on. */
@Composable
fun USwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val c = LocalUnisono.current
    val track by animateColorAsState(if (checked) c.action else c.borderStrong, label = "switch-track")
    val thumbX by animateDpAsState(if (checked) 24.dp else 4.dp, label = "switch-thumb")
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(Unisono.Pill)
            .background(track)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(
            Modifier
                .offset(x = thumbX, y = 4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) c.onAction else c.raised),
        )
    }
}

/** The 7 dp dot that marks a provider by its colour. */
@Composable
fun UDot(color: Color) {
    Box(
        Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/** Space a tab leaves under its last item so nothing ends flush against the navigation bar. */
@Composable
fun UBottomSpace() {
    Box(Modifier.height(24.dp).width(1.dp))
}
