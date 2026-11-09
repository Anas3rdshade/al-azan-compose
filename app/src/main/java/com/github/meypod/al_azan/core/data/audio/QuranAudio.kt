package com.github.meypod.al_azan.core.data.audio

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Joins a surah's per-ayah MP3s from `assets/content/audio/quran/ar.alafasy/` into one cached file, because
 * [android.media.MediaPlayer] cannot open asset URIs and [AdhanPreviewPlaybackService] plays a single source.
 * Raw MP3 frame streams concatenate cleanly; the per-file ID3 tags in between are skipped by the decoder.
 */
fun surahAudioUri(
    context: Context,
    surah: Int,
): Uri? {
    val out = File(context.cacheDir, "quran_audio/%03d.mp3".format(surah))
    if (!out.exists()) {
        val dir = "content/audio/quran/ar.alafasy"
        val prefix = "%03d_".format(surah)
        val parts = context.assets.list(dir).orEmpty().filter { it.startsWith(prefix) }.sorted()
        if (parts.isEmpty()) return null
        out.parentFile?.mkdirs()
        val tmp = File(out.path + ".tmp")
        tmp.outputStream().use { sink -> parts.forEach { context.assets.open("$dir/$it").use { src -> src.copyTo(sink) } } }
        tmp.renameTo(out)
    }
    return Uri.fromFile(out)
}
