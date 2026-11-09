package com.github.meypod.al_azan.main.location

import android.content.Context
import com.github.meypod.al_azan.core.domain.model.calculation.CalculationLocationDetail
import com.github.meypod.al_azan.core.domain.model.geo.CityGeoInfo
import com.github.meypod.al_azan.core.domain.model.geo.CountryGeoInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sqrt

/** One row of assets/india_cities.json (GeoNames IN, places with population >= 15k, CC BY 4.0). */
@Serializable
data class IndiaCity(
    val name: String,
    val state: String,
    val lat: Double,
    val lng: Double,
    val pop: Int = 0,
) {
    fun toLocationDetail() =
        CalculationLocationDetail(
            lat = lat,
            long = lng,
            city = CityGeoInfo(name = name, names = name, lat = lat, long = lng, country = "IN"),
            country = CountryGeoInfo(code = "IN", names = "India", name = "India"),
            state = state,
        )
}

object IndiaCities {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): List<IndiaCity> =
        context.assets.open("india_cities.json").bufferedReader().use {
            json.decodeFromString<List<IndiaCity>>(it.readText())
        }

    /**
     * Snaps a GPS fix to a bundled city by distance / sqrt(population), so Dadar reads "Mumbai" and not the
     * GeoNames suburb Dharavi, while Thane and Noida keep their own names. Equirectangular is plenty here.
     */
    fun nearest(cities: List<IndiaCity>, lat: Double, lng: Double): IndiaCity? {
        val k = cos(Math.toRadians(lat))
        return cities.minByOrNull { hypot((it.lng - lng) * k, it.lat - lat) / sqrt(maxOf(it.pop, 1).toDouble()) }
    }
}
