package com.github.meypod.al_azan.main.namaz

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.core.presentation.navigation.NavigationController
import com.github.meypod.al_azan.main.duas.DraftChip

/** Route.Main.Faraiz(topic) indexes this list. Content: Namaz Tracker/07_Faraiz_Research.md. */
internal val FaraizTopics = listOf(R.string.faraiz_wuzu, R.string.faraiz_ghusl, R.string.faraiz_tayammum, R.string.faraiz_namaz)

private class Step(val pic: Pic, val farz: Boolean = false)

private val WuzuSteps = listOf(
    Step(Pic.Jug), Step(Pic.Hands), Step(Pic.Mouth), Step(Pic.Nose), Step(Pic.Face, true),
    Step(Pic.Arm, true), Step(Pic.HeadTop, true), Step(Pic.Ears), Step(Pic.Foot, true), Step(Pic.Khilal),
)
private val GhuslSteps = listOf(Step(Pic.Hands), Step(Pic.Jug), Step(Pic.Jug), Step(Pic.Mouth, true), Step(Pic.Nose, true), Step(Pic.Body, true))
private val TayammumSteps = listOf(Step(Pic.None, true), Step(Pic.None), Step(Pic.Strike), Step(Pic.Face, true), Step(Pic.Strike2), Step(Pic.Arm, true))
// gold = the arkan: takbeer-e-tahreema, qiyam, ruku, sajda, qada-e-akheerah
private val Positions = listOf(
    Step(Pic.Takbeer, true), Step(Pic.Qiyam, true), Step(Pic.Ruku, true), Step(Pic.Qawma),
    Step(Pic.Sajda, true), Step(Pic.Jalsa), Step(Pic.Qada, true), Step(Pic.Salam),
)

@Composable
fun FaraizScreen(topic: Int) {
    ScreenScaffold(
        title = stringResource(FaraizTopics[topic]),
        onBackClick = { NavigationController.navigateBack() },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DraftChip()
        when (topic) {
            0 -> {
                KulliCallout()
                Steps(R.string.faraiz_steps, R.array.faraiz_wuzu_steps, WuzuSteps)
                Items(R.string.faraiz_wuzu_farz_title, R.array.faraiz_wuzu_farz, farz = true, note = R.string.faraiz_wuzu_basis)
                Items(R.string.faraiz_wuzu_sunnat_title, R.array.faraiz_wuzu_sunnat)
                Items(R.string.faraiz_wuzu_breaks_title, R.array.faraiz_wuzu_breaks)
                Items(R.string.faraiz_wuzu_not_breaks_title, R.array.faraiz_wuzu_not_breaks)
            }
            1 -> {
                KulliCallout()
                Steps(R.string.faraiz_steps, R.array.faraiz_ghusl_steps, GhuslSteps)
                DontMiss()
                Items(R.string.faraiz_ghusl_farz_title, R.array.faraiz_ghusl_farz, farz = true)
                Items(R.string.faraiz_ghusl_sunnat_title, R.array.faraiz_ghusl_sunnat)
            }
            2 -> {
                Section(R.string.faraiz_tayammum_when_title) { Body(R.string.faraiz_tayammum_when) }
                Steps(R.string.faraiz_tayammum_steps_title, R.array.faraiz_tayammum_steps, TayammumSteps)
                Items(R.string.faraiz_tayammum_farz_title, R.array.faraiz_tayammum_farz, farz = true)
                Section(R.string.faraiz_tayammum_breaks_title) { Body(R.string.faraiz_tayammum_breaks) }
            }
            else -> {
                Positions()
                Items(R.string.faraiz_shuroot_title, R.array.faraiz_shuroot, note = R.string.faraiz_shuroot_note)
                Items(R.string.faraiz_arkan_title, R.array.faraiz_arkan, farz = true)
                Items(R.string.faraiz_wajibat_title, R.array.faraiz_wajibat, intro = R.string.faraiz_wajibat_note)
            }
        }
    }
}

@Composable
private fun Body(@StringRes text: Int) = Text(stringResource(text), style = MaterialTheme.typography.bodyMedium)

@Composable
private fun KulliCallout() {
    val shade = LocalShadeColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(shade.gold.copy(alpha = 0.12f))
            .border(1.5.dp, shade.gold, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(R.string.faraiz_callout_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = shade.gold)
        Body(R.string.faraiz_callout)
    }
}

@Composable
private fun Picture(pic: Pic, farz: Boolean, modifier: Modifier) {
    val shade = LocalShadeColors.current
    val ink = MaterialTheme.colorScheme.onSurface
    val water = MaterialTheme.colorScheme.onSurfaceVariant
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(shade.pill)
            .padding(6.dp),
    ) { drawPic(pic, ink, if (farz) shade.gold else ink, shade.gold, water, measurer) }
}

@Composable
private fun FarzTag() {
    val shade = LocalShadeColors.current
    Text(
        stringResource(R.string.faraiz_farz_tag),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = shade.gold,
        modifier = Modifier
            .border(1.dp, shade.gold, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun Steps(@StringRes title: Int, @ArrayRes captions: Int, steps: List<Step>) {
    val text = stringArrayResource(captions)
    Section(title) {
        Text(stringResource(R.string.faraiz_legend), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        steps.forEachIndexed { i, s ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (s.pic != Pic.None) Picture(s.pic, s.farz, Modifier.size(92.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${i + 1}. ${text[i]}", style = MaterialTheme.typography.bodyMedium)
                    if (s.farz) FarzTag()
                }
            }
        }
    }
}

@Composable
private fun Items(@StringRes title: Int, @ArrayRes items: Int, farz: Boolean = false, @StringRes note: Int? = null, @StringRes intro: Int? = null) {
    val shade = LocalShadeColors.current
    Section(title) {
        intro?.let { Body(it) }
        stringArrayResource(items).forEachIndexed { i, item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${i + 1}.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (farz) shade.gold else shade.accent)
                Text(item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
        note?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun DontMiss() {
    var ticked by rememberSaveable { mutableIntStateOf(0) } // bit i = item i checked
    Section(R.string.faraiz_ghusl_dont_miss) {
        Picture(Pic.DontMiss, false, Modifier.size(150.dp).align(Alignment.CenterHorizontally))
        stringArrayResource(R.array.faraiz_ghusl_dont_miss_items).forEachIndexed { i, item ->
            val bit = 1 shl i
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = ticked and bit != 0, onCheckedChange = { ticked = ticked xor bit })
                Text("${i + 1}. $item", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Positions() {
    val text = stringArrayResource(R.array.faraiz_positions)
    Section(R.string.faraiz_positions_title) {
        Text(stringResource(R.string.faraiz_legend), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Positions.withIndex().chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { (i, s) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Picture(s.pic, s.farz, Modifier.fillMaxWidth().aspectRatio(1f))
                        Text("${i + 1}. ${text[i]}", style = MaterialTheme.typography.bodySmall)
                        if (s.farz) FarzTag()
                    }
                }
            }
        }
    }
}

