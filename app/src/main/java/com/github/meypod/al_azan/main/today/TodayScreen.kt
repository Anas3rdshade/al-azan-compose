package com.github.meypod.al_azan.main.today

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.domain.model.adhan.Prayer
import com.github.meypod.al_azan.core.domain.model.settings.SupportedLocales
import com.github.meypod.al_azan.core.domain.util.addDaysTimeZoneAware
import com.github.meypod.al_azan.core.domain.util.formatInstant
import com.github.meypod.al_azan.core.presentation.Amiri
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.main.home.HomeUiAction
import com.github.meypod.al_azan.main.home.HomeUiState
import android.icu.text.DateFormat
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private val QuickLocales = listOf("en", "hi", "ur")

private val FivePrayers =listOf(Prayer.Fajr, Prayer.Dhuhr, Prayer.Asr, Prayer.Maghrib, Prayer.Isha)

@StringRes
private fun Prayer.arabicName(): Int =
    when (this) {
        Prayer.Fajr -> R.string.prayer_ar_fajr
        Prayer.Dhuhr -> R.string.prayer_ar_dhuhr
        Prayer.Asr -> R.string.prayer_ar_asr
        Prayer.Maghrib -> R.string.prayer_ar_maghrib
        else -> R.string.prayer_ar_isha
    }

@Composable
fun TodayScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
) {
    val shade = LocalShadeColors.current
    val fg = MaterialTheme.colorScheme.onBackground
    val timeSkeleton = if (uiState.is24Hour) "HH:mm" else "h:mm a"
    fun time(instant: kotlin.time.Instant) =
        formatInstant(instant, uiState.locale, "gregorian", timeSkeleton, uiState.numberingSystem)

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        // Header: place on the left, Hijri over Gregorian on the right.
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            val detail = uiState.location?.locationDetail
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onAction(HomeUiAction.OnLocationTextClick) },
            ) {
                Text(
                    detail?.label?.takeIf { it.isNotBlank() } ?: detail?.city?.toString() ?: detail?.toCoordsString().orEmpty(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = fg,
                )
                (detail?.state ?: detail?.country)?.let { Text(it.toString(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatInstant(
                        addDaysTimeZoneAware(uiState.viewingInstant, uiState.hijriDateAdjustment),
                        "ar",
                        uiState.arabicCalendar,
                        DateFormat.YEAR_MONTH_DAY,
                    ),
                    fontFamily = Amiri,
                    fontSize = 15.sp,
                    color = fg,
                )
                Text(
                    formatInstant(uiState.viewingInstant, uiState.locale, "gregorian", "EEEdMMM", uiState.numberingSystem),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Quick switch between the app's three target languages; the full list stays in Settings.
            var langMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { langMenu = true }) {
                    Icon(painterResource(R.drawable.globe), contentDescription = stringResource(R.string.language), tint = shade.accent, modifier = Modifier.size(22.dp))
                }
                DropdownMenu(expanded = langMenu, onDismissRequest = { langMenu = false }) {
                    QuickLocales.mapNotNull { v -> SupportedLocales.find { it.value == v } }.forEach { l ->
                        DropdownMenuItem(
                            text = { Text(l.label, fontWeight = if (uiState.locale.startsWith(l.value)) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                langMenu = false
                                onAction(HomeUiAction.OnChangeLanguage(l.value))
                            },
                        )
                    }
                }
            }
            val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            IconButton(onClick = { onAction(HomeUiAction.OnSetDarkTheme(!dark)) }) {
                Icon(
                    painterResource(if (dark) R.drawable.outline_light_mode_24 else R.drawable.outline_dark_mode_24),
                    contentDescription = stringResource(R.string.today_toggle_theme),
                    tint = shade.accent,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val next = uiState.nextShariaTime
        val times = uiState.shariaTimes
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(140.dp, 140.dp, 24.dp, 24.dp))
                .background(shade.hero)
                .padding(top = 34.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.today_next_prayer).uppercase(),
                fontSize = 12.sp,
                letterSpacing = 0.08.em,
                color = shade.heroFg.copy(alpha = 0.8f),
            )
            if (next != null) {
                Text(stringResource(next.prayer.stringRes), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = shade.heroFg)
                if (next.prayer in FivePrayers) {
                    Text(stringResource(next.prayer.arabicName()), fontFamily = Amiri, fontSize = 22.sp, color = shade.heroFg)
                }
                Text(uiState.countdownText, fontSize = 46.sp, fontWeight = FontWeight.ExtraBold, color = shade.heroFg)
                Text(
                    stringResource(R.string.today_left_until, time(next.prayerTime)),
                    fontSize = 13.sp,
                    color = shade.heroFg.copy(alpha = 0.75f),
                )
                // Elapsed share of the gap between the previous of the five prayers and the next one.
                val now = Clock.System.now()
                val prev = times?.let { t -> FivePrayers.map(t::forPrayer).lastOrNull { it <= now } ?: (t.isha - 1.days) }
                val fraction = if (prev != null && next.prayerTime > prev) {
                    ((now - prev) / (next.prayerTime - prev)).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }
                Box(
                    Modifier
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x407F7F7F)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(shade.accent),
                    )
                }
            } else {
                Text("--:--", fontSize = 46.sp, fontWeight = FontWeight.ExtraBold, color = shade.heroFg)
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FivePrayers.forEach { prayer ->
                val prayed = prayer in uiState.prayedToday
                val isNext = next?.prayer == prayer
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(shade.card)
                        .then(if (isNext) Modifier.border(2.dp, shade.accent, RoundedCornerShape(20.dp)) else Modifier)
                        .clickable { onAction(HomeUiAction.OnTogglePrayed(prayer)) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .then(
                                if (prayed) {
                                    Modifier.background(shade.accent)
                                } else {
                                    Modifier.border(2.dp, shade.muted, RoundedCornerShape(6.dp))
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (prayed) Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(prayer.stringRes), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = fg, modifier = Modifier.weight(1f))
                    Text(
                        times?.let { time(it.forPrayer(prayer)) } ?: "--:--",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                        color = fg,
                    )
                    val adhanOn = prayer in uiState.adhanOn
                    IconButton(onClick = { onAction(HomeUiAction.OnToggleAdhan(prayer)) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painterResource(R.drawable.bell_solid),
                            contentDescription = stringResource(if (adhanOn) R.string.today_adhan_on else R.string.today_adhan_off),
                            tint = fg,
                            modifier = Modifier
                                .size(18.dp)
                                .alpha(if (adhanOn) 1f else 0.45f),
                        )
                    }
                }
            }
        }
    }
}
