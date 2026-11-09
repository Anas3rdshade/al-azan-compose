package com.github.meypod.al_azan.core.data.content

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Ayah(
    val ayah: Int,
    val arabic: String,
    @SerialName("transliteration_hi") val transliterationHi: String? = null,
    val hindi: String,
    @SerialName("hindi_footnotes") val hindiFootnotes: String? = null,
    val urdu: String,
    @SerialName("urdu_footnotes") val urduFootnotes: String? = null,
) {
    fun translation(lang: String) = if (lang == "ur") urdu else hindi

    fun footnotes(lang: String) = if (lang == "ur") urduFootnotes else hindiFootnotes
}

@Serializable
data class Surah(
    val surah: Int,
    @SerialName("name_arabic") val nameArabic: String,
    @SerialName("name_en") val nameEn: String,
    val ayahs: List<Ayah>,
)

@Serializable
data class Dua(
    val id: String,
    val category: String,
    @SerialName("title_en") val titleEn: String,
    val repeat: Int = 1,
    val arabic: String,
    val transliteration: String? = null,
    /** The Arabic words in Devanagari letters. */
    @SerialName("transliteration_hi") val transliterationHi: String? = null,
    val hindi: String,
    val urdu: String,
    /** Keys "hindi", "urdu", "arabic", "transliteration_hi";"draft_needs_native_reader" marks a meaning not yet reviewed. */
    @SerialName("review_status") val reviewStatus: Map<String, String> = emptyMap(),
    val reference: List<String> = emptyList(),
) {
    fun meaning(lang: String) = if (lang == "ur") urdu else hindi

    fun isDraft(lang: String) = reviewStatus[if (lang == "ur") "urdu" else "hindi"] == "draft_needs_native_reader"

    val isTransliterationHiDraft get() = reviewStatus["transliteration_hi"] == "draft_needs_native_reader"
}

@Serializable
private data class DuaFile(val duas: List<Dua>)

/** Read-once loader for the Quran and dua JSON bundled under `assets/content/`. Call off the main thread. */
object BundledContent {
    private val json = Json { ignoreUnknownKeys = true }
    private var surahs: List<Surah>? = null
    private var duas: List<Dua>? = null

    @Synchronized
    fun surahs(context: Context): List<Surah> = surahs ?: context.assets.list("content/quran").orEmpty()
        .filter { it.endsWith(".json") }
        .sorted()
        .map { f -> json.decodeFromString<Surah>(context.assets.open("content/quran/$f").bufferedReader().use { it.readText() }) }
        .also { surahs = it }

    @Synchronized
    fun duas(context: Context): List<Dua> = duas ?: json.decodeFromString<DuaFile>(
        context.assets.open("content/duas/duas.json").bufferedReader().use { it.readText() },
    ).duas.also { duas = it }
}
