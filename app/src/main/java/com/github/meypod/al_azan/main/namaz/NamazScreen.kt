package com.github.meypod.al_azan.main.namaz

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.github.meypod.al_azan.core.presentation.dialog.rememberLocationAccessHelperDialogs
import com.github.meypod.al_azan.core.util.android.LocationUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.domain.model.counter.DEFAULT_COUNTER_IDS
import com.github.meypod.al_azan.core.domain.model.counter.COUNTER_ID_FAST
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.core.presentation.components.BottomSelect
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.core.presentation.navigation.NavigationController
import com.github.meypod.al_azan.core.presentation.navigation.Route
import com.github.meypod.al_azan.main.counter.CounterUiAction
import com.github.meypod.al_azan.main.counter.CounterUiState
import com.github.meypod.al_azan.main.counter.counterDisplayLabel
import com.github.meypod.al_azan.main.duas.DraftChip

/** One row of the Hanafi rakat table; cells copied from 03_Reviewer_Check_Sheet.html (★ muakkadah, ☆ ghair-muakkadah). */
private class RakatRow(
    @StringRes val name: Int,
    val sunnatBefore: String,
    val fard: String,
    val sunnatAfter: String,
    val witr: String,
    val nafl: String,
    @StringRes val note: Int? = null,
)

private val RakatTable = listOf(
    RakatRow(R.string.fajr, "2★", "2", "–", "–", "–"),
    RakatRow(R.string.dhuhr, "4★", "4", "2★", "–", "2"),
    RakatRow(R.string.asr, "4☆", "4", "–", "–", "–"),
    RakatRow(R.string.maghrib, "–", "3", "2★", "–", "2"),
    RakatRow(R.string.isha, "4☆", "4", "2★", "3", "2", R.string.namaz_note_isha),
    RakatRow(R.string.namaz_jumuah, "4", "2", "‎4 + 2", "–", "2", R.string.namaz_note_jumuah), // LRM keeps "4 + 2" in order under RTL
)

private val TasbihTargets = listOf(33, 34, 99, 100)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NamazScreen(
    counters: CounterUiState,
    onCounterAction: (CounterUiAction) -> Unit,
) {
    ScreenScaffold(
        title = stringResource(R.string.tab_namaz),
        onBackClick = {},
        navigationIcon = {},
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Section(R.string.namaz_rakat_title) {
            DraftChip()
            RakatHeader()
            RakatTable.forEach { RakatLine(it) }
            Text(stringResource(R.string.namaz_rakat_legend), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Section(R.string.faraiz_title) {
            Text(stringResource(R.string.faraiz_intro), style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FaraizTopics.forEachIndexed { i, t ->
                    FilledTonalButton(onClick = { NavigationController.navigateTo(Route.Main.Faraiz(i)) }) {
                        Text(stringResource(t))
                    }
                }
            }
        }
        Section(R.string.qibla) {
            Text(stringResource(R.string.namaz_qibla_desc), style = MaterialTheme.typography.bodyMedium)
            FilledTonalButton(onClick = { NavigationController.navigateTo(Route.Main.Qibla) }) {
                Text(stringResource(R.string.namaz_open))
            }
        }
        Section(R.string.namaz_mosques_title) { MosquesNearMe() }
        Section(R.string.counter) {
            // The fork's Qada counter (MMKV JSON via CounterRepository); the fast counter stays on its own screen.
            counters.counters.filter { it.id in DEFAULT_COUNTER_IDS && it.id != COUNTER_ID_FAST }.forEach { c ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(counterDisplayLabel(c), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    FilledTonalIconButton(onClick = { onCounterAction(CounterUiAction.OnDecrement(c.id)) }) { Text("−") }
                    Text(
                        c.count.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(56.dp),
                    )
                    FilledTonalIconButton(onClick = { onCounterAction(CounterUiAction.OnIncrement(c.id)) }) { Text("+") }
                }
            }
            TextButton(onClick = { NavigationController.navigateTo(Route.Main.Counter) }) {
                Text(stringResource(R.string.namaz_all_counters))
            }
        }
        Section(R.string.namaz_tasbih) { Tasbih() }
    }
}

@Composable
internal fun Section(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    val shade = LocalShadeColors.current
    Surface(color = shade.card, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = shade.accent)
            content()
        }
    }
}

private val RakatCols = listOf(
    R.string.namaz_col_sunnat,
    R.string.namaz_col_fard,
    R.string.namaz_col_sunnat,
    R.string.namaz_col_witr,
    R.string.namaz_col_nafl,
)
private val RakatNameWidth = 72.dp

/** Column labels once; the two Sunnat columns read as before/after by their place around Fard. */
@Composable
private fun RakatHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Spacer(Modifier.width(RakatNameWidth))
        RakatCols.forEach {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** One prayer on one line: aligned chips, Fard filled, ★ tonal, the rest outlined; empty cells stay blank. */
@Composable
private fun RakatLine(r: RakatRow) {
    val shade = LocalShadeColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(shade.pill)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(r.name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.width(RakatNameWidth),
            )
            listOf(r.sunnatBefore, r.fard, r.sunnatAfter, r.witr, r.nafl).forEachIndexed { i, count ->
                Box(Modifier.weight(1f).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                    if (count != "–") {
                        val fard = i == 1
                        val shape = RoundedCornerShape(10.dp)
                        Text(
                            count,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            color = if (fard) shade.card else shade.accent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .then(
                                    when {
                                        fard -> Modifier.background(shade.accent)
                                        '★' in count -> Modifier.background(shade.accent.copy(alpha = 0.16f))
                                        else -> Modifier.border(1.dp, shade.line, shape)
                                    },
                                )
                                .padding(vertical = 6.dp),
                        )
                    }
                }
            }
        }
        r.note?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun Tasbih() {
    var count by rememberSaveable { mutableIntStateOf(0) }
    var target by rememberSaveable { mutableIntStateOf(33) }
    BottomSelect(
        options = TasbihTargets,
        selectedKey = target.toString(),
        optionKey = { it.toString() },
        optionLabel = { it.toString() },
        onSelect = { target = it },
        label = { Text(stringResource(R.string.namaz_tasbih_target)) },
        modifier = Modifier.fillMaxWidth(),
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FilledTonalButton(onClick = { count++ }, shape = CircleShape, modifier = Modifier.size(160.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(count.toString(), fontSize = 44.sp, fontWeight = if (count >= target) FontWeight.Bold else FontWeight.Normal)
                Text(stringResource(R.string.dua_count, count % target, target) + if (count >= target) "  ×${count / target}" else "")
            }
        }
        Text(stringResource(R.string.dua_tap_to_count), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
        if (count > 0) TextButton(onClick = { count = 0 }) { Text(stringResource(R.string.reset)) }
    }
}

/** Hands the search to Google Maps (or any geo: app), so the app itself needs no INTERNET permission. See 08_Nearby_Mosques_Research.md. */
@Composable
private fun MosquesNearMe() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searching by remember { mutableStateOf(false) }
    fun open(lat: Double?, lng: Double?) {
        val geo = if (lat != null && lng != null) "geo:$lat,$lng?q=mosque" else "geo:0,0?q=mosque"
        // Google Maps first: a bare geo: intent also offers Ola, Zomato, Zoom etc. in the chooser.
        val tries = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse(geo)).setPackage("com.google.android.apps.maps"),
            Intent(Intent.ACTION_VIEW, Uri.parse(geo)),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=mosque")),
        )
        for (intent in tries) {
            try {
                context.startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
    val trigger = rememberLocationAccessHelperDialogs(
        onPermissionGranted = { forceFresh ->
            scope.launch {
                searching = true
                try {
                    val loc = LocationUtils.requestCurrentLocation(context, forceFresh = forceFresh).getOrNull()
                    open(loc?.latitude, loc?.longitude) // no fix: Maps falls back to its own location
                } finally {
                    searching = false
                }
            }
        },
    )
    Text(stringResource(R.string.namaz_mosques_desc), style = MaterialTheme.typography.bodyMedium)
    FilledTonalButton(onClick = { trigger(true) }, enabled = !searching) {
        Text(stringResource(if (searching) R.string.namaz_mosques_locating else R.string.namaz_mosques_open))
    }
}
