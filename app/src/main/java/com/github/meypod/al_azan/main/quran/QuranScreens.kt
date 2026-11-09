package com.github.meypod.al_azan.main.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.data.audio.AdhanPreviewPlaybackService
import com.github.meypod.al_azan.core.data.content.Ayah
import com.github.meypod.al_azan.core.data.content.Surah
import com.github.meypod.al_azan.core.presentation.Amiri
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.core.presentation.navigation.NavigationController
import com.github.meypod.al_azan.core.presentation.navigation.Route
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

val ArabicStyle = TextStyle(fontFamily = Amiri, fontSize = 26.sp, lineHeight = 50.sp, textDirection = TextDirection.Rtl)

/** Translation in use: the saved choice, else the app language's default ("ur" for Urdu, otherwise "hi"). */
@Composable
fun translationLang(vm: ContentViewModel): String {
    val settings by vm.settings.collectAsStateWithLifecycle()
    return settings?.quranTranslation ?: stringResource(R.string.content_lang)
}

fun translationDirection(lang: String) = if (lang == "ur") TextDirection.Rtl else TextDirection.Content

@Composable
fun TranslationToggle(lang: String, onChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(end = 8.dp)) {
        FilterChip(selected = lang == "hi", onClick = { onChange("hi") }, label = { Text(stringResource(R.string.translation_hindi)) })
        FilterChip(selected = lang == "ur", onClick = { onChange("ur") }, label = { Text(stringResource(R.string.translation_urdu)) })
    }
}

@Composable
fun QuranListScreen(vm: ContentViewModel) {
    val surahs by vm.surahs.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val shade = LocalShadeColors.current
    ScreenScaffold(
        title = stringResource(R.string.tab_quran),
        onBackClick = {},
        navigationIcon = {},
        scrollable = false,
        contentPadding = PaddingValues(0.dp),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            val s = settings
            val last = s?.let { surahs.firstOrNull { it.surah == s.quranLastSurah } }
            if (s != null && last != null) {
                val ayah = s.quranLastAyah.coerceAtLeast(1)
                item(key = "continue") {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(shade.hero, RoundedCornerShape(20.dp))
                            .clickable { NavigationController.navigateTo(Route.Main.QuranReader(last.surah, ayah)) }
                            .padding(18.dp),
                    ) {
                        Text(stringResource(R.string.quran_continue), color = shade.heroFg, style = MaterialTheme.typography.labelLarge)
                        Text(
                            stringResource(R.string.quran_continue_detail, last.nameEn, ayah),
                            color = shade.heroFg,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            items(surahs, key = { it.surah }) { SurahRow(it) }
        }
    }
}

@Composable
private fun SurahRow(s: Surah) {
    val shade = LocalShadeColors.current
    Surface(
        onClick = { NavigationController.navigateTo(Route.Main.QuranReader(s.surah)) },
        color = shade.card,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(s.surah, 38)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(s.nameEn, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.quran_ayahs_n, s.ayahs.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(s.nameArabic, fontFamily = Amiri, fontSize = 22.sp, color = shade.accent)
        }
    }
}

@Composable
private fun NumberBadge(n: Int, sizeDp: Int) {
    val shade = LocalShadeColors.current
    Box(Modifier.size(sizeDp.dp).background(shade.pill, CircleShape), contentAlignment = Alignment.Center) {
        Text(n.toString(), style = MaterialTheme.typography.labelLarge, color = shade.accent)
    }
}

@OptIn(FlowPreview::class)
@Composable
fun QuranReaderScreen(route: Route.Main.QuranReader, vm: ContentViewModel) {
    val surahs by vm.surahs.collectAsStateWithLifecycle()
    val playingId by AdhanPreviewPlaybackService.playingId.collectAsStateWithLifecycle()
    val paused by AdhanPreviewPlaybackService.paused.collectAsStateWithLifecycle()
    val lang = translationLang(vm)
    val surah = surahs.firstOrNull { it.surah == route.surah }
    val isPlaying = playingId == ContentViewModel.playbackId(route.surah)
    ScreenScaffold(
        title = surah?.nameEn.orEmpty(),
        onBackClick = { NavigationController.navigateBack() },
        actions = { TranslationToggle(lang, vm::setTranslation) },
        scrollable = false,
        contentPadding = PaddingValues(0.dp),
        bottomBar = { if (isPlaying && surah != null) PlayerBar(surah.nameEn, paused) },
    ) {
        if (surah == null) return@ScreenScaffold
        // Item 0 is the header, so an ayah's number is also its list index.
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = route.ayah.coerceIn(0, surah.ayahs.size))
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .debounce(800)
                .collect { vm.saveLastRead(surah.surah, it.coerceAtLeast(1)) }
        }
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding)),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = 0) { ReaderHeader(surah) { vm.play(surah, 1) } }
            items(surah.ayahs, key = { it.ayah }) { a ->
                AyahCard(a, lang) {
                    vm.saveLastRead(surah.surah, a.ayah)
                    vm.play(surah, a.ayah)
                }
            }
        }
    }
}

@Composable
private fun ReaderHeader(s: Surah, onPlay: () -> Unit) {
    val shade = LocalShadeColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(shade.hero, RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(s.nameArabic, fontFamily = Amiri, fontSize = 34.sp, color = shade.heroFg)
        Text(stringResource(R.string.quran_ayahs_n, s.ayahs.size), color = shade.heroFg, style = MaterialTheme.typography.bodyMedium)
        FilledTonalButton(onClick = onPlay, modifier = Modifier.padding(top = 10.dp)) {
            Icon(painterResource(R.drawable.outline_play_arrow_24), contentDescription = null)
            Text(stringResource(R.string.quran_play_surah), modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun AyahCard(a: Ayah, lang: String, onPlayFrom: () -> Unit) {
    val shade = LocalShadeColors.current
    var showNotes by rememberSaveable { mutableStateOf(false) }
    val dir = translationDirection(lang)
    Surface(color = shade.card, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumberBadge(a.ayah, 32)
                Box(Modifier.weight(1f))
                IconButton(onClick = onPlayFrom) {
                    Icon(
                        painterResource(R.drawable.outline_play_arrow_24),
                        contentDescription = stringResource(R.string.quran_play_from_here),
                        tint = shade.accent,
                    )
                }
            }
            Text(a.arabic, style = ArabicStyle, modifier = Modifier.fillMaxWidth())
            Text(a.translation(lang), style = MaterialTheme.typography.bodyLarge.copy(textDirection = dir), modifier = Modifier.fillMaxWidth())
            val notes = a.footnotes(lang)
            if (!notes.isNullOrBlank()) {
                TextButton(onClick = { showNotes = !showNotes }) {
                    Text(stringResource(R.string.quran_footnotes) + if (showNotes) "  ▲" else "  ▼")
                }
                if (showNotes) {
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodySmall.copy(textDirection = dir),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBar(label: String, paused: Boolean) {
    val context = LocalContext.current
    Surface(color = LocalShadeColors.current.navBg, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = { AdhanPreviewPlaybackService.togglePause(context) }) {
                Icon(
                    painterResource(if (paused) R.drawable.outline_play_arrow_24 else R.drawable.outline_pause_24),
                    contentDescription = stringResource(if (paused) R.string.quran_resume else R.string.quran_pause),
                )
            }
            IconButton(onClick = { AdhanPreviewPlaybackService.stop(context) }) {
                Icon(painterResource(R.drawable.outline_stop_24), contentDescription = stringResource(R.string.stop))
            }
        }
    }
}
