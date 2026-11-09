package com.github.meypod.al_azan.main.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.github.meypod.al_azan.core.domain.model.favorite_location.StaticFavoriteLocation
import com.github.meypod.al_azan.core.domain.repository.CalculationSettingsRepository
import com.github.meypod.al_azan.core.domain.repository.FavoriteLocationsRepository
import com.github.meypod.al_azan.core.util.android.LocationUtils
import com.github.meypod.al_azan.worker.TRAVEL_MODE_WORK_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface GpsState {
    data object Idle : GpsState
    data object Locating : GpsState
    data class Found(val city: IndiaCity) : GpsState
    data object Failed : GpsState
}

data class IndiaLocationUiState(
    val cities: List<IndiaCity> = emptyList(),
    val states: List<String> = emptyList(),
    val query: String = "",
    val selectedState: String? = null,
    val gps: GpsState = GpsState.Idle,
)

@HiltViewModel
class IndiaLocationViewModel
@Inject constructor(
    private val favoriteLocationsRepository: FavoriteLocationsRepository,
    private val calculationSettingsRepository: CalculationSettingsRepository,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IndiaLocationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cities = withContext(Dispatchers.IO) { IndiaCities.load(context) }
            _uiState.update { s -> s.copy(cities = cities, states = cities.map { it.state }.distinct()) }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onStateSelect(state: String?) = _uiState.update { it.copy(selectedState = state, query = "") }

    fun onPickManually() = _uiState.update { it.copy(gps = GpsState.Idle) }

    /** Called once location permission is granted: one fix, snapped to the nearest bundled city. */
    fun onLocate(forceFresh: Boolean) {
        _uiState.update { it.copy(gps = GpsState.Locating) }
        viewModelScope.launch {
            val fix = LocationUtils.requestCurrentLocation(context, forceFresh = forceFresh).getOrNull()
            val city = fix?.let { IndiaCities.nearest(_uiState.value.cities, it.latitude, it.longitude) }
            _uiState.update { it.copy(gps = if (city != null) GpsState.Found(city) else GpsState.Failed) }
        }
    }

    fun onCitySelect(city: IndiaCity, onDone: () -> Unit) {
        viewModelScope.launch {
            // One fixed slot replaces any older picker choice instead of piling up favorites.
            favoriteLocationsRepository.update { list ->
                list.filterNot { it.id == LOCATION_ID } +
                    StaticFavoriteLocation(id = LOCATION_ID, locationDetail = city.toLocationDetail())
            }
            calculationSettingsRepository.update { it.copy(locationId = LOCATION_ID) }
            WorkManager.getInstance(context).cancelUniqueWork(TRAVEL_MODE_WORK_NAME)
            onDone()
        }
    }

    companion object {
        const val LOCATION_ID = "india_city"
    }
}
