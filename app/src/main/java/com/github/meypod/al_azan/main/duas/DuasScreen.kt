package com.github.meypod.al_azan.main.duas

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.data.content.Dua
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.main.quran.ArabicStyle
import com.github.meypod.al_azan.main.quran.ContentViewModel
import com.github.meypod.al_azan.main.quran.TranslationToggle
import com.github.meypod.al_azan.main.quran.translationDirection
import com.github.meypod.al_azan.main.quran.translationLang

@StringRes
private fun categoryName(key: String): Int = when (key) {
    "morning_evening" -> R.string.dua_cat_morning_evening
    "morning" -> R.string.dua_cat_morning
    "evening" -> R.string.dua_cat_evening
    "after_fajr_maghrib" -> R.string.dua_cat_after_fajr_maghrib
    "adhan" -> R.string.dua_cat_adhan
    "wudu" -> R.string.dua_cat_wudu
    "masjid" -> R.string.dua_cat_masjid
    "in_salah" -> R.string.dua_cat_in_salah
    "after_salah" -> R.string.dua_cat_after_salah
    "witr_qunut" -> R.string.dua_cat_witr_qunut
    "eating" -> R.string.dua_cat_eating
    "iftar" -> R.string.dua_cat_iftar
    "leaving_home" -> R.string.dua_cat_leaving_home
    "entering_home" -> R.string.dua_cat_entering_home
    "lavatory" -> R.string.dua_cat_lavatory
    "sleep" -> R.string.dua_cat_sleep
    "waking_up" -> R.string.dua_cat_waking_up
    else -> R.string.tab_duas
}

@Composable
fun DuasScreen(vm: ContentViewModel) {
    val duas by vm.duas.collectAsStateWithLifecycle()
    val lang = translationLang(vm)
    // English UI shows the English title; in Hindi/Urdu the localized category header gives the context.
    val showTitle = LocalConfiguration.current.locales[0].language == "en"
    var query by rememberSaveable { mutableStateOf("") }
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    val names = duas.map { it.category }.distinct().associateWith { stringResource(categoryName(it)) }
    val q = query.trim()
    val groups = duas
        .filter { picked == null || it.category == picked }
        .filter { d ->
            q.isEmpty() || listOfNotNull(d.titleEn, d.transliteration, d.transliterationHi, d.meaning(lang), names[d.category])
                .any { it.contains(q, ignoreCase = true) }
        }
        .groupBy { it.category }
    ScreenScaffold(
        title = stringResource(R.string.tab_duas),
        onBackClick = {},
        navigationIcon = {},
        actions = { TranslationToggle(lang, vm::setTranslation) },
        scrollable = false,
        contentPadding = PaddingValues(0.dp),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding)),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.dua_search)) },
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(painterResource(R.drawable.baseline_close_24), stringResource(R.string.dua_search_clear))
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = picked == null, onClick = { picked = null }, label = { Text(stringResource(R.string.dua_all)) }) }
                    items(names.keys.toList()) { c ->
                        FilterChip(selected = picked == c, onClick = { picked = if (picked == c) null else c }, label = { Text(names.getValue(c)) })
                    }
                }
            }
            if (groups.isEmpty()) {
                item(key = "none") {
                    Text(stringResource(R.string.dua_no_match), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
                }
            }
            groups.forEach { (category, list) ->
                item(key = "h_$category") {
                    Text(
                        stringResource(categoryName(category)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalShadeColors.current.accent,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(list, key = { it.id }) { DuaCard(it, lang, showTitle) }
            }
        }
    }
}

@Composable
private fun DuaCard(d: Dua, lang: String, showTitle: Boolean) {
    val shade = LocalShadeColors.current
    var count by rememberSaveable { mutableIntStateOf(0) }
    var showRef by rememberSaveable { mutableStateOf(false) }
    var showMeaning by rememberSaveable { mutableStateOf(false) }
    val dir = translationDirection(lang)
    Surface(color = shade.card, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showTitle) Text(d.titleEn, style = MaterialTheme.typography.titleSmall)
            Text(d.arabic, style = ArabicStyle, modifier = Modifier.fillMaxWidth())
            d.transliterationHi?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())
                if (d.isTransliterationHiDraft) DraftChip()
            }
            d.transliteration?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { showMeaning = !showMeaning }) {
                Text(stringResource(R.string.dua_meaning) + if (showMeaning) "  ▲" else "  ▼")
            }
            if (showMeaning) {
                Text(d.meaning(lang), style = MaterialTheme.typography.bodyLarge.copy(textDirection = dir), modifier = Modifier.fillMaxWidth())
                if (d.isDraft(lang)) DraftChip()
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { count++ }) {
                    Text(
                        stringResource(R.string.dua_tap_to_count) + "  " + stringResource(R.string.dua_count, count, d.repeat),
                        fontWeight = if (count >= d.repeat) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                if (count > 0) TextButton(onClick = { count = 0 }) { Text(stringResource(R.string.reset)) }
            }
            if (d.reference.isNotEmpty()) {
                TextButton(onClick = { showRef = !showRef }) {
                    Text(stringResource(R.string.dua_reference) + if (showRef) "  ▲" else "  ▼")
                }
                if (showRef) {
                    Text(d.reference.joinToString("\n\n"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DraftChip() {
    Text(
        stringResource(R.string.dua_draft_label),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(LocalShadeColors.current.pill, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}
