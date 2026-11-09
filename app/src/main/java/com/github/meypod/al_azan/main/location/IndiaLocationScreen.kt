package com.github.meypod.al_azan.main.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import com.github.meypod.al_azan.R
import com.github.meypod.al_azan.core.presentation.components.ScreenScaffold
import com.github.meypod.al_azan.core.presentation.dialog.rememberLocationAccessHelperDialogs
import com.github.meypod.al_azan.core.presentation.navigation.NavigationController

@Composable
fun IndiaLocationScreen(
    uiState: IndiaLocationUiState,
    vm: IndiaLocationViewModel,
) {
    val back = {
        if (uiState.selectedState != null) vm.onStateSelect(null) else NavigationController.navigateBack()
    }
    BackHandler(enabled = uiState.selectedState != null) { vm.onStateSelect(null) }
    ScreenScaffold(
        title = uiState.selectedState ?: stringResource(R.string.location_title),
        onBackClick = back,
        scrollable = false,
    ) {
        IndiaLocationContent(uiState, vm, onPicked = { NavigationController.navigateBack() }, Modifier.weight(1f))
    }
}

/** Also used by the intro's location step, which advances instead of going back. */
@Composable
fun IndiaLocationContent(
    uiState: IndiaLocationUiState,
    vm: IndiaLocationViewModel,
    onPicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val requestLocation = rememberLocationAccessHelperDialogs(onPermissionGranted = vm::onLocate)
    val pick = { city: IndiaCity -> vm.onCitySelect(city, onPicked) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (val gps = uiState.gps) {
            is GpsState.Found ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(R.string.india_gps_confirm, gps.city.name, gps.city.state),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { pick(gps.city) }) { Text(stringResource(R.string.india_gps_yes)) }
                            OutlinedButton(onClick = vm::onPickManually) { Text(stringResource(R.string.india_pick_manually)) }
                        }
                    }
                }
            else ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { requestLocation(true) },
                        enabled = gps != GpsState.Locating && uiState.cities.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(painterResource(R.drawable.map_marker), contentDescription = null)
                        Text(
                            stringResource(
                                if (gps == GpsState.Locating) R.string.india_locating else R.string.india_use_my_location,
                            ),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    if (gps == GpsState.Failed) {
                        Text(
                            stringResource(R.string.india_gps_failed),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
        }
        OutlinedTextField(
            value = uiState.query,
            onValueChange = vm::onQueryChange,
            label = { Text(stringResource(R.string.india_search_city)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val q = uiState.query.trim()
        LazyColumn(Modifier.weight(1f)) {
            when {
                q.isNotEmpty() -> {
                    val hits = uiState.cities
                        .filter { (uiState.selectedState == null || it.state == uiState.selectedState) && it.name.contains(q, ignoreCase = true) }
                        .sortedBy { !it.name.startsWith(q, ignoreCase = true) }
                    items(hits) { city -> CityRow(city, showState = uiState.selectedState == null) { pick(city) } }
                }
                uiState.selectedState != null ->
                    items(uiState.cities.filter { it.state == uiState.selectedState }) { city ->
                        CityRow(city, showState = false) { pick(city) }
                    }
                else ->
                    items(uiState.states) { state ->
                        ListItem(
                            headlineContent = { Text(state) },
                            modifier = Modifier.clickable { vm.onStateSelect(state) },
                        )
                        HorizontalDivider()
                    }
            }
        }
    }
}

@Composable
private fun CityRow(city: IndiaCity, showState: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(city.name) },
        supportingContent = if (showState) ({ Text(city.state) }) else null,
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}
