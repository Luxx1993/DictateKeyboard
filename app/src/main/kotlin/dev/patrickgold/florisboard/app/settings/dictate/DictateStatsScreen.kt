/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 * Copyright (C) 2026 Luxx1993 (diktat)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.app.settings.dictate

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.app.apptheme.LocalUnisono
import dev.patrickgold.florisboard.app.apptheme.Unisono
import dev.patrickgold.florisboard.app.main.UCard
import dev.patrickgold.florisboard.app.main.UDialog
import dev.patrickgold.florisboard.app.main.UDivider
import dev.patrickgold.florisboard.app.main.UNavRow
import dev.patrickgold.florisboard.app.main.UnisonoPage
import dev.patrickgold.florisboard.dictate.data.stats.DictateStats
import dev.patrickgold.jetpref.datastore.model.collectAsState
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import org.florisboard.lib.compose.stringRes

/**
 * Lifetime dictation statistics (issue #142) in the look of Entwurf 3: a hero "time saved" card, a grid of
 * stat cards, a 7-day activity chart, share and reset. Everything reads reactively from the [DictateStats]
 * prefs and updates live.
 */
@Composable
fun DictateStatsScreen() = UnisonoPage(title = stringRes(R.string.dictate__stats_title)) {
    val prefs by FlorisPreferenceStore
    val c = LocalUnisono.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val dictations by prefs.dictate.statsDictations.collectAsState()
    val words by prefs.dictate.statsWords.collectAsState()
    val chars by prefs.dictate.statsChars.collectAsState()
    val spokenSeconds by prefs.dictate.statsSpokenSeconds.collectAsState()
    val rewordings by prefs.dictate.statsRewordings.collectAsState()
    val firstUse by prefs.dictate.statsFirstUseEpochMs.collectAsState()
    val streakCurrent by prefs.dictate.statsStreakCurrent.collectAsState()
    val streakBest by prefs.dictate.statsStreakBest.collectAsState()
    val daily by prefs.dictate.statsDaily.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (dictations <= 0L) {
        UCard {
            Text(
                stringRes(R.string.dictate__stats_empty),
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                style = Unisono.Body,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        return@UnisonoPage
    }

    val savedSeconds = DictateStats.savedSeconds(words, spokenSeconds)
    val savedText = formatDuration(savedSeconds.toLong())
    UCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringRes(R.string.dictate__stats_time_saved), style = Unisono.Mono, color = c.textTertiary)
            Text(savedText, style = Unisono.StatNumber.copy(fontSize = 48.sp, lineHeight = 54.sp), color = c.textPrimary)
            Text(
                stringRes(R.string.dictate__stats_time_saved_sub),
                style = Unisono.Label,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }

    val avgWords = if (dictations > 0L) words.toDouble() / dictations else 0.0
    val paceWpm = if (spokenSeconds > 0L) words / (spokenSeconds / 60.0) else 0.0
    val cards = listOf(
        StatItem(formatCount(dictations), stringRes(R.string.dictate__stats_dictations)),
        StatItem(formatCount(words), stringRes(R.string.dictate__stats_words)),
        StatItem(formatDuration(spokenSeconds), stringRes(R.string.dictate__stats_spoken)),
        StatItem(formatDecimal(avgWords), stringRes(R.string.dictate__stats_avg_words)),
        StatItem("${formatDecimal(paceWpm)} ${stringRes(R.string.dictate__stats_pace_unit)}", stringRes(R.string.dictate__stats_pace)),
        StatItem(formatCount(chars), stringRes(R.string.dictate__stats_chars)),
        StatItem(formatCount(rewordings), stringRes(R.string.dictate__stats_rewordings)),
        StatItem(
            formatCount(streakCurrent.toLong()),
            stringRes(R.string.dictate__stats_streak),
            trailing = stringRes(R.string.dictate__stats_streak_best).format(streakBest),
        ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { item -> StatCard(item, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }

    ActivityCard(bars = DictateStats.activity(daily))

    UCard {
        val shareBody = stringRes(R.string.dictate__stats_share_text)
            .format(savedText, formatCount(dictations), formatCount(words))
        UNavRow(
            title = stringRes(R.string.dictate__stats_share),
            onClick = { shareStats(context, "$shareBody\n\n$PROJECT_URL") },
        )
        UDivider()
        UNavRow(
            title = stringRes(R.string.dictate__stats_reset),
            onClick = { showResetDialog = true },
            subtitle = if (firstUse > 0L) stringRes(R.string.dictate__stats_since).format(formatDate(firstUse)) else null,
        )
    }

    if (showResetDialog) {
        UDialog(
            title = stringRes(R.string.dictate__stats_reset_confirm_title),
            description = stringRes(R.string.dictate__stats_reset_confirm_msg),
            onDismiss = { showResetDialog = false },
        ) {
            UNavRow(
                title = stringRes(R.string.dictate__stats_reset),
                onClick = {
                    scope.launch { DictateStats.reset(prefs) }
                    showResetDialog = false
                },
            )
        }
    }
}

private data class StatItem(
    val value: String,
    val label: String,
    /** Small mono text after [value] on the same line (e.g. the best streak), so the card keeps its height. */
    val trailing: String? = null,
)

@Composable
private fun StatCard(item: StatItem, modifier: Modifier = Modifier) {
    val c = LocalUnisono.current
    UCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(item.label, style = Unisono.Mono, color = c.textTertiary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(item.value, style = Unisono.SectionTitle, color = c.textPrimary)
                if (item.trailing != null) {
                    Text(
                        "  ${item.trailing}",
                        style = Unisono.Mono,
                        color = c.textTertiary,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(bars: List<DictateStats.DayBar>) {
    val c = LocalUnisono.current
    UCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringRes(R.string.dictate__stats_activity), style = Unisono.Mono, color = c.textTertiary)
            val max = bars.maxOfOrNull { it.words }?.coerceAtLeast(1L) ?: 1L
            Row(
                modifier = Modifier.fillMaxWidth().height(84.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                bars.forEach { bar ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val frac = bar.words.toFloat() / max
                        val barHeight = if (bar.words <= 0L) 3.dp else (BAR_MAX_HEIGHT * frac).coerceAtLeast(8.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (bar.words > 0L) c.action else c.borderStrong),
                        )
                        Text(weekdayNarrow(bar.epochDay), style = Unisono.Mono, color = c.textTertiary)
                    }
                }
            }
        }
    }
}

// --- helpers --------------------------------------------------------------------------------------

private const val PROJECT_URL = "https://github.com/Luxx1993/DictateKeyboard"
private val BAR_MAX_HEIGHT = 64.dp

private fun shareStats(context: android.content.Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun formatCount(value: Long): String = NumberFormat.getIntegerInstance().format(value)

private fun formatDecimal(value: Double): String =
    NumberFormat.getInstance().apply { maximumFractionDigits = if (value < 100) 1 else 0 }.format(value)

/** Compact duration: `3h 12m`, `12m`, `45s`. */
private fun formatDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0L)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return when {
        h > 0 -> if (m > 0) "${h}h ${m}m" else "${h}h"
        m > 0 -> "${m}m"
        else -> "${sec}s"
    }
}

private fun formatDate(epochMs: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMs))

private fun weekdayNarrow(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
