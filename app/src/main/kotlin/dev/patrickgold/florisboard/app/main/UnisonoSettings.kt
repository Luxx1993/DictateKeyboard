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

import androidx.compose.foundation.background
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.LocalNavController
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import org.florisboard.lib.compose.stringRes

/*
 * The building blocks of the settings pages behind the tabs, in the look of Entwurf 3: a page on paper with
 * a back arrow and the page headline, groups of rows in raised cards, and choices made in a card dialog
 * rather than a stock Material one.
 */

/** A settings page: back arrow, headline, optional subtitle, then [content] (cards and group labels). */
@Composable
fun UnisonoPage(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalUnisono.current
    val navController = LocalNavController.current
    Column(
        Modifier
            .fillMaxSize()
            .background(c.canvas)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { navController.popBackStack() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                UnisonoIcons.Arrow,
                contentDescription = stringRes(R.string.main__back),
                tint = c.textPrimary,
                modifier = Modifier.size(22.dp).rotate(180f),
            )
        }
        Column(Modifier.padding(bottom = 8.dp)) { UPageHeader(title, subtitle) }
        content()
    }
}

/** A row with a switch; the whole row toggles. */
@Composable
fun USwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = LocalUnisono.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = Unisono.BodyStrong.copy(lineHeight = 24.sp), color = c.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = Unisono.Label, color = c.textSecondary)
            }
        }
        USwitch(checked = checked, onCheckedChange = onCheckedChange, contentDescription = title)
    }
}

/** One option of a [UChoiceDialog]. */
class UChoice<T>(
    val value: T,
    val title: String,
    val subtitle: String? = null,
    val leading: (@Composable () -> Unit)? = null,
)

/**
 * A row that names a choice and its current value, and opens [UChoiceDialog] to change it. [summary]
 * overrides the subtitle, which is otherwise the chosen option's title.
 */
@Composable
fun <T> UChoiceRow(
    title: String,
    choices: List<UChoice<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    summary: String? = null,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        UNavRow(
            title = title,
            subtitle = summary ?: choices.firstOrNull { it.value == selected }?.title,
            onClick = { onOpenChange(true) },
        )
    }
    if (open) {
        UChoiceDialog(
            title = title,
            description = description,
            choices = choices,
            selected = selected,
            onSelect = {
                onSelect(it)
                onOpenChange(false)
            },
            onDismiss = { onOpenChange(false) },
        )
    }
}

/** A card dialog listing [choices]; tapping one chooses it. */
@Composable
fun <T> UChoiceDialog(
    title: String,
    choices: List<UChoice<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    description: String? = null,
) {
    UDialog(title = title, onDismiss = onDismiss, description = description) {
        choices.forEachIndexed { i, choice ->
            if (i > 0) UDivider()
            UChoiceOption(
                title = choice.title,
                subtitle = choice.subtitle,
                selected = choice.value == selected,
                leading = choice.leading,
                onClick = { onSelect(choice.value) },
            )
        }
    }
}

/** A selectable option row: an ink ring that fills when chosen, title, optional subtitle and image. */
@Composable
fun UChoiceOption(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    val c = LocalUnisono.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) c.action else c.borderStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(11.dp).clip(CircleShape).background(c.action))
        }
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(title, style = Unisono.BodyStrong.copy(lineHeight = 24.sp), color = c.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = Unisono.Label, color = c.textSecondary)
            }
        }
    }
}

/** A checkable row for lists where several can be on at once. */
@Composable
fun UCheckRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalUnisono.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(Unisono.RadiusMd)
                .background(if (checked) c.action else c.raised)
                .border(1.5.dp, if (checked) c.action else c.borderStrong, Unisono.RadiusMd),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Text("✓", style = Unisono.Caption, color = c.onAction)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = Unisono.BodyStrong.copy(lineHeight = 24.sp), color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = Unisono.Label, color = c.textSecondary)
        }
    }
}

/** A labelled slider inside a card, with the current value on the right. */
@Composable
fun USliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalUnisono.current
    Column(modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = Unisono.BodyStrong.copy(lineHeight = 24.sp),
                color = c.textPrimary,
            )
            Text(valueLabel, style = Unisono.Mono.copy(fontSize = 13.sp), color = c.textSecondary)
        }
        if (subtitle != null) {
            Text(subtitle, style = Unisono.Label, color = c.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = c.action,
                activeTrackColor = c.action,
                inactiveTrackColor = c.sunken,
                activeTickColor = c.onAction,
                inactiveTickColor = c.borderStrong,
            ),
        )
    }
}

/**
 * The dialog of the Unisono pages: a raised card with a headline, an optional explanation, [content] and
 * a close action. Choices take effect as they are made, so there is nothing to confirm.
 */
@Composable
fun UDialog(
    title: String,
    onDismiss: () -> Unit,
    description: String? = null,
    doneLabel: String = stringRes(R.string.main__done),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalUnisono.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .fillMaxWidth()
                .clip(Unisono.RadiusLg)
                .background(c.raised)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 18.dp),
        ) {
            Text(
                title,
                modifier = Modifier.padding(horizontal = 18.dp),
                style = Unisono.SectionTitle,
                color = c.textPrimary,
            )
            if (description != null) {
                Text(
                    description,
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 6.dp),
                    style = Unisono.Label,
                    color = c.textSecondary,
                )
            }
            Column(Modifier.padding(top = 10.dp)) { content() }
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    doneLabel,
                    modifier = Modifier
                        .clip(Unisono.Pill)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    style = Unisono.Caption.copy(fontWeight = FontWeight.Bold),
                    color = c.textPrimary,
                )
            }
        }
    }
}

/**
 * A labelled text field on a sunken pill, for use inside a [UDialog]. With [singleLine] off it becomes a
 * taller box of at least [minHeight] that grows with its text.
 */
@Composable
fun UTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minHeight: Dp = 110.dp,
    isError: Boolean = false,
) {
    val c = LocalUnisono.current
    val shape = if (singleLine) Unisono.Pill else Unisono.RadiusMd
    Column(
        Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = Unisono.Mono, color = if (isError) Color(0xFFB3443A) else c.textTertiary)
        Box(
            Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(44.dp) else Modifier.heightIn(min = minHeight))
                .clip(shape)
                .background(c.sunken)
                .then(if (isError) Modifier.border(1.dp, Color(0xFFB3443A), shape) else Modifier)
                .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
        ) {
            if (value.isEmpty()) Text(placeholder, style = Unisono.Body, color = c.textTertiary)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                textStyle = Unisono.Body.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.textPrimary),
            )
        }
    }
}

/** A pill chip; the [selected] one is filled with the action colour. */
@Composable
fun UChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalUnisono.current
    Text(
        label,
        modifier = Modifier
            .clip(Unisono.Pill)
            .background(if (selected) c.action else c.raised)
            .border(0.5.dp, if (selected) c.action else c.borderSubtle, Unisono.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = Unisono.Caption,
        color = if (selected) c.onAction else c.textPrimary,
    )
}

/** A pill button: [primary] is filled with the action colour, otherwise outlined. */
@Composable
fun UButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = true, enabled: Boolean = true) {
    val c = LocalUnisono.current
    val shape = Unisono.Pill
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .then(if (primary) Modifier.background(c.action) else Modifier.border(1.dp, c.borderStrong, shape))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = Unisono.Caption, color = if (primary) c.onAction else c.textPrimary)
    }
}

/** A note in a card with a coloured dot: red for a [warning], green otherwise. */
@Composable
fun UNotice(text: String, warning: Boolean = false) {
    val c = LocalUnisono.current
    UCard {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.padding(top = 6.dp)) { UDot(if (warning) Color(0xFFB3443A) else c.success) }
            Text(text, style = Unisono.Label, color = c.textPrimary, modifier = Modifier.weight(1f))
        }
    }
}
