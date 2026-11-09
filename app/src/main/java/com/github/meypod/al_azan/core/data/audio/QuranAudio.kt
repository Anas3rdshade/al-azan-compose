package com.github.meypod.al_azan.core.data.audio

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Joins a surah's per-ayah MP3s from `assets/content/audio/quran/ar.alafasy/` into one cached file, because
 * [android.media.MediaPlayer] cannot open asset URIs and [AdhanPreviewPlaybackService] plays a single source.
 * The bundled files are tag-free CBR frame streams (tools/reencode_audio.py), so they concatenate cleanly.
 * [fromAyah] > 1 joins only that ayah onward into its own file; only the latest such partial is kept.
 */
fun surahAudioUri(
    context: Context,
    surah: Int,
    fromAyah: Int = 1,
): Uri? {
    val dirOut = File(context.cacheDir, "quran_audio")
    val name = if (fromAyah > 1) "%03d_%03d.mp3".format(surah, fromAyah) else "%03d.mp3".format(surah)
    val out = File(dirOut, name)
    if (!out.exists()) {
        val dir = "content/audio/quran/ar.alafasy"
        val prefix = "%03d_".format(surah)
        val parts = context.assets.list(dir).orEmpty()
            .filter { it.startsWith(prefix) && it.removePrefix(prefix).removeSuffix(".mp3").toIntOrNull()?.let { a -> a >= fromAyah } == true }
            .sorted()
        if (parts.isEmpty()) return null
        dirOut.mkdirs()
        if (fromAyah > 1) dirOut.listFiles { f -> f.name.length == 11 }?.forEach { it.delete() }
        val tmp = File(out.path + ".tmp")
        tmp.outputStream().use { sink -> parts.forEach { context.assets.open("$dir/$it").use { src -> src.copyTo(sink) } } }
        tmp.renameTo(out)
    }
    return Uri.fromFile(out)
}
