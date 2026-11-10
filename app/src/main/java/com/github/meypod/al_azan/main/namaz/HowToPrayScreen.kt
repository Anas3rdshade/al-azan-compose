package com.github.meypod.al_azan.main.namaz

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.data.content.Dua
import com.github.meypod.al_azan.core.data.content.Surah
import com.github.meypod.al_azan.core.presentation.LocalShadeColors
import com.github.meypod.al_azan.core.presentation.components.BottomSelect
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.core.presentation.navigation.NavigationController
import com.github.meypod.al_azan.main.duas.DraftChip
import com.github.meypod.al_azan.main.quran.ArabicStyle
import com.github.meypod.al_azan.main.quran.ContentViewModel
import kotlinx.coroutines.launch

/** Step pager for P3 of 07_Salah_Guide_Plan.md; content from 07_Salah_Guide_Research.md and 09_Seated_Chair_Prayer_Research.md (Hanafi). */

internal enum class Prayer(@StringRes val label: Int, val rakats: Int, val loudRakats: Int) {
    FAJR(R.string.fajr, 2, 2),
    DHUHR(R.string.dhuhr, 4, 0),
    ASR(R.string.asr, 4, 0),
    MAGHRIB(R.string.maghrib, 3, 2),
    ISHA(R.string.isha, 4, 2),
    WITR(R.string.sg_witr, 3, 0),
    EID(R.string.sg_eid, 2, 2),
    JANAZAH(R.string.sg_janazah, 0, 0),
}

internal enum class Mode(@StringRes val label: Int) { STANDING(R.string.sg_standing), GROUND(R.string.sg_ground), CHAIR(R.string.sg_chair) }

internal enum class Pose { TAKBIR, QIYAM, RUKU, QAWMAH, SAJDAH, JALSA, QADAH, SALAM_R, SALAM_L }

internal enum class Rec {
    TAKBIR, THANA, THANA_JANAZAH, TAAWWUDH, TASMIYAH, FATIHA, AMEEN, IKHLAS, RUKU, TASMI, TAHMID, SAJDAH,
    TASHAHHUD, DUROOD, DUA, QUNOOT, JANAZAH_DUA, SALAM,
}

internal class PrayStep(
    val pose: Pose,
    @StringRes val title: Int,
    @StringRes val body: Int,
    val recs: List<Pair<Rec, Int>> = emptyList(),
    val rakat: Int = 0,
    @StringRes val note: Int? = null,
)

internal fun buildSteps(prayer: Prayer, women: Boolean, mode: Mode): List<PrayStep> {
    val seated = mode != Mode.STANDING
    fun g(men: Int, wom: Int) = if (women) wom else men
    val takbirBody = g(R.string.sg_takbir_m, R.string.sg_takbir_w)
    val salam = listOf(
        PrayStep(Pose.SALAM_R, R.string.sg_salam_r_t, R.string.sg_salam_r, listOf(Rec.SALAM to 1)),
        PrayStep(Pose.SALAM_L, R.string.sg_salam_l_t, R.string.sg_salam_l, listOf(Rec.SALAM to 1)),
    )
    val niyyah = PrayStep(Pose.QIYAM, R.string.sg_niyyah_t, R.string.sg_niyyah)
    if (prayer == Prayer.JANAZAH) return listOf(
        niyyah,
        PrayStep(Pose.TAKBIR, R.string.sg_j1_t, g(R.string.sg_j1_m, R.string.sg_j1_w), listOf(Rec.TAKBIR to 1, Rec.THANA_JANAZAH to 1)),
        PrayStep(Pose.QIYAM, R.string.sg_j2_t, R.string.sg_j2, listOf(Rec.TAKBIR to 1, Rec.DUROOD to 1)),
        PrayStep(Pose.QIYAM, R.string.sg_j3_t, R.string.sg_j3, listOf(Rec.TAKBIR to 1, Rec.JANAZAH_DUA to 1)),
        PrayStep(Pose.QIYAM, R.string.sg_j4_t, R.string.sg_j4, listOf(Rec.TAKBIR to 1)),
    ) + salam

    val n = prayer.rakats
    val steps = mutableListOf(niyyah, PrayStep(Pose.TAKBIR, R.string.sg_takbir_t, takbirBody, listOf(Rec.TAKBIR to 1), 1))
    for (r in 1..n) {
        val aloud = if (!women && r <= prayer.loudRakats) R.string.sg_aloud else R.string.sg_silent
        val surah = r <= 2 || prayer == Prayer.WITR
        if (r == 1) {
            steps += PrayStep(Pose.QIYAM, R.string.sg_thana_t, g(R.string.sg_thana_m, R.string.sg_thana_w), listOf(Rec.THANA to 1), r)
            if (prayer == Prayer.EID) steps += PrayStep(Pose.TAKBIR, R.string.sg_eid_t, R.string.sg_eid1, listOf(Rec.TAKBIR to 3), r)
            steps += PrayStep(Pose.QIYAM, R.string.sg_taawwudh_t, R.string.sg_taawwudh, listOf(Rec.TAAWWUDH to 1), r)
        }
        steps += PrayStep(
            Pose.QIYAM, R.string.sg_fatiha_t,
            when {
                r == 1 || (seated && surah) -> R.string.sg_fatiha
                surah -> R.string.sg_fatiha_next
                seated -> R.string.sg_fatiha_only_seated
                else -> R.string.sg_fatiha_only
            },
            listOf(Rec.TASMIYAH to 1, Rec.FATIHA to 1, Rec.AMEEN to 1), r, aloud,
        )
        if (surah) steps += PrayStep(Pose.QIYAM, R.string.sg_surah_t, R.string.sg_surah, listOf(Rec.IKHLAS to 1), r, aloud)
        if (prayer == Prayer.EID && r == 2) steps += PrayStep(Pose.TAKBIR, R.string.sg_eid_t, R.string.sg_eid2, listOf(Rec.TAKBIR to 4), r)
        if (prayer == Prayer.WITR && r == 3) steps += PrayStep(Pose.TAKBIR, R.string.sg_qunoot_t, g(R.string.sg_qunoot_m, R.string.sg_qunoot_w), listOf(Rec.TAKBIR to 1, Rec.QUNOOT to 1), r)
        steps += PrayStep(
            Pose.RUKU, R.string.sg_ruku_t,
            when (mode) { Mode.STANDING -> g(R.string.sg_ruku_m, R.string.sg_ruku_w); Mode.GROUND -> R.string.sg_ruku_ground; Mode.CHAIR -> R.string.sg_ruku_chair },
            listOf(Rec.RUKU to 3), r,
        )
        steps += PrayStep(Pose.QAWMAH, R.string.sg_qawmah_t, if (seated) R.string.sg_qawmah_seated else R.string.sg_qawmah, listOf(Rec.TASMI to 1, Rec.TAHMID to 1), r)
        val sajdahBody = when (mode) { Mode.STANDING -> g(R.string.sg_sajdah_m, R.string.sg_sajdah_w); Mode.GROUND -> R.string.sg_sajdah_ground; Mode.CHAIR -> R.string.sg_sajdah_chair }
        steps += PrayStep(Pose.SAJDAH, R.string.sg_sajdah_t, sajdahBody, listOf(Rec.SAJDAH to 3), r)
        steps += PrayStep(Pose.JALSA, R.string.sg_jalsa_t, if (mode == Mode.CHAIR) R.string.sg_jalsa_chair else g(R.string.sg_jalsa_m, R.string.sg_jalsa_w), listOf(Rec.TAKBIR to 1), r)
        steps += PrayStep(Pose.SAJDAH, R.string.sg_sajdah2_t, R.string.sg_sajdah2, listOf(Rec.SAJDAH to 3), r)
        if (r == 2 && n > 2) steps += PrayStep(Pose.QADAH, R.string.sg_qadah1_t, R.string.sg_qadah1, listOf(Rec.TASHAHHUD to 1), r)
    }
    steps += PrayStep(Pose.QADAH, R.string.sg_qadah2_t, R.string.sg_qadah2, listOf(Rec.TASHAHHUD to 1, Rec.DUROOD to 1, Rec.DUA to 1), n)
    return steps + salam.map { PrayStep(it.pose, it.title, it.body, it.recs, n) }
}

private val Standing = listOf(
    R.drawable.salah_men_takbir, R.drawable.salah_men_qiyam, R.drawable.salah_men_ruku, R.drawable.salah_men_qawmah, R.drawable.salah_men_sajdah,
    R.drawable.salah_men_jalsa, R.drawable.salah_men_qadah, R.drawable.salah_men_salam_right, R.drawable.salah_men_salam_left,
) to listOf(
    R.drawable.salah_women_takbir, R.drawable.salah_women_qiyam, R.drawable.salah_women_ruku, R.drawable.salah_women_qawmah, R.drawable.salah_women_sajdah,
    R.drawable.salah_women_jalsa, R.drawable.salah_women_qadah, R.drawable.salah_women_salam_right, R.drawable.salah_women_salam_left,
)

// Ground mode stands for the takbir when able (09_Seated_Chair_Prayer_Research.md case 1A), so it reuses the standing takbir figure.
private val Ground = listOf(
    R.drawable.salah_men_takbir, R.drawable.salah_men_ground_qiyam, R.drawable.salah_men_ground_ruku, R.drawable.salah_men_ground_qiyam, R.drawable.salah_men_ground_sajdah,
    R.drawable.salah_men_ground_tashahhud, R.drawable.salah_men_ground_tashahhud, R.drawable.salah_men_ground_salam, R.drawable.salah_men_ground_salam,
) to listOf(
    R.drawable.salah_women_takbir, R.drawable.salah_women_ground_qiyam, R.drawable.salah_women_ground_ruku, R.drawable.salah_women_ground_qiyam, R.drawable.salah_women_ground_sajdah,
    R.drawable.salah_women_ground_tashahhud, R.drawable.salah_women_ground_tashahhud, R.drawable.salah_women_ground_salam, R.drawable.salah_women_ground_salam,
)

private val Chair = listOf(
    R.drawable.salah_men_chair_takbir, R.drawable.salah_men_chair_qiyam, R.drawable.salah_men_chair_ruku, R.drawable.salah_men_chair_qiyam, R.drawable.salah_men_chair_sajdah,
    R.drawable.salah_men_chair_tashahhud, R.drawable.salah_men_chair_tashahhud, R.drawable.salah_men_chair_salam, R.drawable.salah_men_chair_salam,
) to listOf(
    R.drawable.salah_women_chair_takbir, R.drawable.salah_women_chair_qiyam, R.drawable.salah_women_chair_ruku, R.drawable.salah_women_chair_qiyam, R.drawable.salah_women_chair_sajdah,
    R.drawable.salah_women_chair_tashahhud, R.drawable.salah_women_chair_tashahhud, R.drawable.salah_women_chair_salam, R.drawable.salah_women_chair_salam,
)

@DrawableRes
private fun figure(pose: Pose, women: Boolean, mode: Mode): Int {
    val set = when (mode) { Mode.STANDING -> Standing; Mode.GROUND -> Ground; Mode.CHAIR -> Chair }
    return (if (women) set.second else set.first)[pose.ordinal]
}

/** One recitation: Arabic, Latin and Devanagari transliteration, and meanings in en/hi/ur. */
private class Recite(val arabic: String, val latin: String?, val deva: String?, val en: String, val hi: String, val ur: String) {
    fun translit(lang: String) = if (lang == "hi") deva ?: latin else latin ?: deva
    fun meaning(lang: String) = when (lang) { "hi" -> hi; "ur" -> ur; else -> en }
}

private val Footnote = Regex("""\s*\[\d+]""")

private fun surahText(s: Surah?, from: Int, latin: String, en: String): Recite? {
    val ayahs = s?.ayahs?.filter { it.ayah >= from } ?: return null
    fun join(f: (com.github.meypod.al_azan.core.data.content.Ayah) -> String?) = ayahs.mapNotNull(f).joinToString(" ") { it.replace(Footnote, "") }
    return Recite(join { it.arabic }, latin, join { it.transliterationHi }, en, join { it.hindi }, join { it.urdu })
}

private fun duaText(d: Dua?, en: String) = d?.let { Recite(it.arabic, it.transliteration, it.transliterationHi, en, it.hindi, it.urdu) }

private fun recitations(duas: List<Dua>, surahs: List<Surah>): Map<Rec, Recite> {
    val dua = duas.associateBy { it.id }
    val fatiha = surahs.find { it.surah == 1 }
    val bismillah = fatiha?.ayahs?.firstOrNull()
    return listOfNotNull(
        Rec.TAKBIR to Recite("اللَّهُ أَكْبَرُ", "Allahu akbar", "अल्लाहु अकबर", "Allah is the Greatest.", "अल्लाह सबसे बड़ा है।", "اللہ سب سے بڑا ہے۔"),
        duaText(dua["dua_016_in_salah"], "Glory be to You, O Allah, and all praise. Blessed is Your name, exalted is Your majesty, and there is no god but You.")?.let { Rec.THANA to it },
        Rec.THANA_JANAZAH to Recite(
            "سُبْحَانَكَ اللّٰهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَىٰ جَدُّكَ، وَجَلَّ ثَنَاؤُكَ، وَلَا إِلٰهَ غَيْرُكَ.",
            "Subhanaka Allahumma wa bihamdika wa tabarakasmuka wa ta'ala jadduka wa jalla thana'uka wa la ilaha ghayruk.",
            "सुब्हानकल्लाहुम्म व बि हम्दिक व तबारकस्मुक व तआला जद्दुक व जल्ल सनाउक व ला इलाह ग़ैरुक।",
            "Glory be to You, O Allah, and all praise. Blessed is Your name, exalted is Your majesty, great is Your praise, and there is no god but You.",
            "ऐ अल्लाह, तू पाक है और तेरी ही तारीफ़ है, तेरा नाम बरकत वाला है, तेरी शान बुलंद है, तेरी तारीफ़ बड़ी है और तेरे सिवा कोई माबूद नहीं।",
            "اے اللہ، تو پاک ہے اور تیری ہی تعریف ہے، تیرا نام بابرکت ہے، تیری شان بلند ہے، تیری ثنا بڑی ہے اور تیرے سوا کوئی معبود نہیں۔",
        ),
        Rec.TAAWWUDH to Recite(
            "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ", "A'udhu billahi minash-shaytanir-rajim", "अऊज़ु बिल्लाहि मिनश्शैतानिर्रजीम",
            "I seek refuge with Allah from the accursed Satan.", "मैं धुत्कारे हुए शैतान से अल्लाह की पनाह माँगता हूँ।", "میں شیطان مردود سے اللہ کی پناہ مانگتا ہوں۔",
        ),
        bismillah?.let {
            Rec.TASMIYAH to Recite(it.arabic, "Bismillahir-rahmanir-rahim", it.transliterationHi, "In the name of Allah, the Entirely Merciful, the Especially Merciful.", it.hindi, it.urdu.replace(Footnote, ""))
        },
        surahText(
            fatiha, 2,
            "Al-hamdu lillahi rabbil-'alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na'budu wa iyyaka nasta'in. Ihdinas-siratal-mustaqim. Siratal-ladhina an'amta 'alayhim, ghayril-maghdubi 'alayhim wa lad-dallin.",
            "All praise is due to Allah, Lord of the worlds, the Entirely Merciful, the Especially Merciful, Sovereign of the Day of Recompense. It is You we worship and You we ask for help. Guide us to the straight path, the path of those upon whom You have bestowed favour, not of those who have earned anger or of those who are astray.",
        )?.let { Rec.FATIHA to it },
        Rec.AMEEN to Recite("آمِينَ", "Ameen", "आमीन", "O Allah, accept.", "ऐ अल्लाह, क़बूल फ़रमा।", "اے اللہ، قبول فرما۔"),
        surahText(
            surahs.find { it.surah == 112 }, 1,
            "Qul huwallahu ahad. Allahus-samad. Lam yalid wa lam yulad. Wa lam yakul-lahu kufuwan ahad.",
            "Say, He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
        )?.let { Rec.IKHLAS to it },
        Rec.RUKU to Recite("سُبْحَانَ رَبِّيَ الْعَظِيمِ", "Subhana rabbiyal-'azim", "सुब्हान रब्बियल अज़ीम", "Glory is to my Lord, the Most Great.", "पाक है मेरा रब जो सबसे महान है।", "پاک ہے میرا رب جو عظمت والا ہے۔"),
        Rec.TASMI to Recite("سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ", "Sami'allahu liman hamidah", "समिअल्लाहु लिमन हमिदह", "Allah hears the one who praises Him.", "अल्लाह ने उसकी सुन ली जिसने उसकी तारीफ़ की।", "اللہ نے اس کی سن لی جس نے اس کی تعریف کی۔"),
        Rec.TAHMID to Recite("رَبَّنَا لَكَ الْحَمْدُ", "Rabbana lakal-hamd", "रब्बना लकल हम्द", "Our Lord, to You belongs all praise.", "ऐ हमारे रब, सारी तारीफ़ तेरे ही लिए है।", "اے ہمارے رب، تمام تعریف تیرے ہی لیے ہے۔"),
        Rec.SAJDAH to Recite("سُبْحَانَ رَبِّيَ الأَعْلَى", "Subhana rabbiyal-a'la", "सुब्हान रब्बियल आला", "Glory is to my Lord, the Most High.", "पाक है मेरा रब जो सबसे ऊँचा है।", "پاک ہے میرا رب جو سب سے بلند ہے۔"),
        duaText(dua["dua_020_in_salah"], "All greetings, prayers and good things are for Allah. Peace be upon you, O Prophet, and the mercy of Allah and His blessings. Peace be upon us and upon the righteous servants of Allah. I bear witness that there is no god but Allah, and I bear witness that Muhammad is His servant and messenger.")?.let { Rec.TASHAHHUD to it },
        duaText(dua["dua_021_in_salah"], "O Allah, send prayers on Muhammad and the family of Muhammad, as You sent prayers on Ibrahim and the family of Ibrahim; You are Praiseworthy, Glorious. O Allah, bless Muhammad and the family of Muhammad, as You blessed Ibrahim and the family of Ibrahim; You are Praiseworthy, Glorious.")?.let { Rec.DUROOD to it },
        duaText(dua["dua_022_in_salah"], "O Allah, I have wronged myself greatly and none forgives sins but You, so forgive me with forgiveness from You and have mercy on me. You are the Forgiving, the Merciful.")?.let { Rec.DUA to it },
        duaText(dua["dua_023_witr_qunut"], "O Allah, we seek Your help and Your forgiveness, we believe in You and rely on You, we praise You with all good, we thank You and are not ungrateful, and we abandon whoever disobeys You. O Allah, You alone we worship, to You we pray and prostrate, to You we hasten and strive. We hope for Your mercy and fear Your punishment; surely Your punishment overtakes the disbelievers.")?.let { Rec.QUNOOT to it },
        Rec.JANAZAH_DUA to Recite(
            "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا وَشَاهِدِنَا وَغَائِبِنَا وَصَغِيرِنَا وَكَبِيرِنَا وَذَكَرِنَا وَأُنْثَانَا، اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الإِسْلاَمِ وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الإِيمَانِ",
            "Allahummaghfir li hayyina wa mayyitina wa shahidina wa gha'ibina wa saghirina wa kabirina wa dhakarina wa unthana. Allahumma man ahyaytahu minna fa-ahyihi 'alal-islam, wa man tawaffaytahu minna fa-tawaffahu 'alal-iman.",
            "अल्लाहुम्मग़्फ़िर लि हय्यिना व मय्यितिना व शाहिदिना व ग़ाइबिना व सग़ीरिना व कबीरिना व ज़करिना व उन्साना, अल्लाहुम्म मन अह्यैतहू मिन्ना फ़-अह्यिही अलल इस्लाम, व मन तवफ़्फ़ैतहू मिन्ना फ़-तवफ़्फ़हू अलल ईमान।",
            "O Allah, forgive our living and our dead, those present and those absent, our young and our old, our men and our women. O Allah, whoever of us You keep alive, keep alive upon Islam, and whoever of us You take, take upon faith.",
            "ऐ अल्लाह, हमारे ज़िंदों और मुर्दों को, हाज़िर और ग़ायब को, छोटों और बड़ों को, मर्दों और औरतों को बख़्श दे। ऐ अल्लाह, हम में से जिसे तू ज़िंदा रखे उसे इस्लाम पर ज़िंदा रख, और जिसे मौत दे उसे ईमान पर मौत दे।",
            "اے اللہ، ہمارے زندوں اور مردوں کو، حاضر اور غائب کو، چھوٹوں اور بڑوں کو، مردوں اور عورتوں کو بخش دے۔ اے اللہ، ہم میں سے جسے تو زندہ رکھے اسے اسلام پر زندہ رکھ، اور جسے موت دے اسے ایمان پر موت دے۔",
        ),
        Rec.SALAM to Recite("السَّلاَمُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ", "As-salamu 'alaykum wa rahmatullah", "अस्सलामु अलैकुम व रहमतुल्लाह", "Peace and the mercy of Allah be upon you.", "तुम पर सलामती और अल्लाह की रहमत हो।", "تم پر سلامتی اور اللہ کی رحمت ہو۔"),
    ).toMap()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HowToPrayScreen(vm: ContentViewModel) {
    val duas by vm.duas.collectAsStateWithLifecycle()
    val surahs by vm.surahs.collectAsStateWithLifecycle()
    val recs = remember(duas, surahs) { recitations(duas, surahs) }
    val lang = LocalConfiguration.current.locales[0].language
    var prayer by rememberSaveable { mutableStateOf(Prayer.FAJR) }
    var women by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(Mode.STANDING) }
    val steps = remember(prayer, women, mode) { buildSteps(prayer, women, mode) }
    val pager = rememberPagerState { steps.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(prayer) { pager.scrollToPage(0) }
    val labels = Prayer.entries.associateWith { stringResource(it.label) }

    ScreenScaffold(
        title = stringResource(R.string.sg_title),
        onBackClick = { NavigationController.navigateBack() },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DraftChip()
        BottomSelect(
            options = Prayer.entries,
            selectedKey = prayer.name,
            optionKey = { it.name },
            optionLabel = { labels.getValue(it) },
            onSelect = { prayer = it },
            label = { Text(stringResource(R.string.sg_prayer)) },
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !women, onClick = { women = false }, label = { Text(stringResource(R.string.sg_men)) })
            FilterChip(selected = women, onClick = { women = true }, label = { Text(stringResource(R.string.sg_women)) })
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mode.entries.forEach { m -> FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(stringResource(m.label)) }) }
        }
        if (mode != Mode.STANDING || women) {
            Text(stringResource(R.string.sg_seated_draft), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            FilledTonalButton(enabled = pager.currentPage > 0, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) {
                Text(stringResource(R.string.sg_prev))
            }
            Text(
                stringResource(R.string.sg_step, pager.currentPage + 1, steps.size),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(enabled = pager.currentPage < steps.size - 1, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) {
                Text(stringResource(R.string.sg_next))
            }
        }
        HorizontalPager(state = pager, pageSpacing = 12.dp, verticalAlignment = Alignment.Top) { i ->
            StepCard(steps[i], prayer, women, mode, recs, lang)
        }
        Section(R.string.sg_special_title) {
            Text(stringResource(R.string.sg_special_method), style = MaterialTheme.typography.bodyMedium)
            stringArrayResource(R.array.sg_special_names).zip(stringArrayResource(R.array.sg_special_details)).forEach { (name, detail) ->
                Column {
                    Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StepCard(step: PrayStep, prayer: Prayer, women: Boolean, mode: Mode, recs: Map<Rec, Recite>, lang: String) {
    val shade = LocalShadeColors.current
    val dir = if (lang == "ur") TextDirection.Rtl else TextDirection.Content
    Surface(color = shade.card, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (step.rakat > 0 && prayer.rakats > 0) {
                Text(stringResource(R.string.sg_rakat, step.rakat, prayer.rakats), style = MaterialTheme.typography.labelLarge, color = shade.gold)
            }
            Text(stringResource(step.title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = shade.accent)
            Image(
                painterResource(figure(step.pose, women, mode)),
                contentDescription = stringResource(step.title),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(240.dp).background(shade.pill, RoundedCornerShape(12.dp)).padding(8.dp),
            )
            Text(stringResource(step.body), style = MaterialTheme.typography.bodyLarge)
            step.note?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            step.recs.forEach { (key, times) ->
                val r = recs[key] ?: return@forEach
                Surface(color = shade.pill, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (times > 1) Text("×$times", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = shade.gold)
                        Text(r.arabic, style = ArabicStyle, modifier = Modifier.fillMaxWidth())
                        r.translit(lang)?.let { Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
                        Text(
                            r.meaning(lang),
                            style = MaterialTheme.typography.bodySmall.copy(textDirection = dir),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
