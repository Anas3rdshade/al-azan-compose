package com.github.meypod.al_azan.main.namaz

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Section(R.string.qibla) {
            Text(stringResource(R.string.namaz_qibla_desc), style = MaterialTheme.typography.bodyMedium)
            FilledTonalButton(onClick = { NavigationController.navigateTo(Route.Main.Qibla) }) {
                Text(stringResource(R.string.namaz_open))
            }
        }
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
private fun Section(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    val shade = LocalShadeColors.current
    Surface(color = shade.card, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = shade.accent)
            content()
        }
    }
}

@Composable
private fun RakatCells(name: String, cells: List<String>, header: Boolean) {
    val style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(name, style = style, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
        cells.forEach { Text(it, style = style, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun RakatHeader() {
    RakatCells(
        "",
        listOf(R.string.namaz_col_sunnat_before, R.string.namaz_col_fard, R.string.namaz_col_sunnat_after, R.string.namaz_col_witr, R.string.namaz_col_nafl)
            .map { stringResource(it) },
        header = true,
    )
    HorizontalDivider(color = LocalShadeColors.current.line)
}

@Composable
private fun RakatLine(r: RakatRow) {
    RakatCells(stringResource(r.name), listOf(r.sunnatBefore, r.fard, r.sunnatAfter, r.witr, r.nafl), header = false)
    r.note?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
