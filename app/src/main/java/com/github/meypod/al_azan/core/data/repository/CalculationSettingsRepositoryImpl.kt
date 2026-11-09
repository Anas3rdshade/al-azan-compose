package com.github.meypod.al_azan.core.data.repository

import com.github.meypod.al_azan.core.domain.model.calculation.CalculationSettings
import com.github.meypod.al_azan.core.domain.repository.CalculationSettingsRepository
import com.github.meypod.al_azan.core.util.storage.MMKVDataStore
import io.github.meypod.adhan_kotlin.CalculationMethod
import io.github.meypod.adhan_kotlin.Madhab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** India-only build: every reader gets Karachi + Hanafi Asr, whatever was stored or restored. */
val INDIA_PARAMETERS = CalculationMethod.KARACHI.parameters.copy(madhab = Madhab.HANAFI)

private fun CalculationSettings.forIndia() = copy(parameters = INDIA_PARAMETERS)

class CalculationSettingsRepositoryImpl(
    private val calcSettingsDatastore: MMKVDataStore<CalculationSettings>,
) : CalculationSettingsRepository {
    override val data: Flow<CalculationSettings>
        get() = calcSettingsDatastore.data.map { it.forIndia() }

    override suspend fun fetch(): CalculationSettings = calcSettingsDatastore.data.value.forIndia()

    override suspend fun update(transform: (t: CalculationSettings) -> CalculationSettings) {
        calcSettingsDatastore.update { transform(it.forIndia()).forIndia() }
    }
}
