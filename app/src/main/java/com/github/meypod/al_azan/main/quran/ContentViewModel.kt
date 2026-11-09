package com.github.meypod.al_azan.main.quran

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.meypod.al_azan.core.data.audio.AdhanPreviewPlaybackService
import com.github.meypod.al_azan.core.data.audio.surahAudioUri
import com.github.meypod.al_azan.core.data.content.BundledContent
import com.github.meypod.al_azan.core.data.content.Dua
import com.github.meypod.al_azan.core.data.content.Surah
import com.github.meypod.al_azan.core.domain.model.settings.Settings
import com.github.meypod.al_azan.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Shared by the Quran list, the reader and the Duas tab: bundled content plus reading prefs. */
@HiltViewModel
class ContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    val surahs: StateFlow<List<Surah>> = _surahs.asStateFlow()
    private val _duas = MutableStateFlow<List<Dua>>(emptyList())
    val duas: StateFlow<List<Dua>> = _duas.asStateFlow()
    val settings: StateFlow<Settings?> = settingsRepository.data.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _surahs.value = BundledContent.surahs(context)
            _duas.value = BundledContent.duas(context)
        }
    }

    fun saveLastRead(surah: Int, ayah: Int) {
        viewModelScope.launch { settingsRepository.update { it.copy(quranLastSurah = surah, quranLastAyah = ayah) } }
    }

    fun setTranslation(lang: String) {
        viewModelScope.launch { settingsRepository.update { it.copy(quranTranslation = lang) } }
    }

    fun play(surah: Surah, fromAyah: Int) {
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) { surahAudioUri(context, surah.surah, fromAyah) } ?: return@launch
            AdhanPreviewPlaybackService.play(context, uri, playbackId(surah.surah), surah.nameEn)
        }
    }

    companion object {
        fun playbackId(surah: Int) = "quran_%03d".format(surah)
    }
}
