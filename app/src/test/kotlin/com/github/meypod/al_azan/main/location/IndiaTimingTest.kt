package com.github.meypod.al_azan.main.location

import android.app.Application
import com.github.meypod.al_azan.core.data.repository.INDIA_PARAMETERS
import com.github.meypod.al_azan.core.domain.model.adhan.timesOrNull
import com.github.meypod.al_azan.core.domain.model.calculation.CalculationAdjustments
import com.github.meypod.al_azan.core.domain.model.calculation.CalculationLocationDetail
import com.github.meypod.al_azan.core.domain.usecase.GetShariaTimesUseCase
import io.github.meypod.adhan_kotlin.MidnightMethod
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.ZoneId
import java.util.TimeZone
import kotlin.math.abs
import kotlin.time.Instant

@Serializable
private data class AladhanRow(
    val name: String,
    val state: String,
    val lat: Double,
    val lng: Double,
    val date: String,
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String,
)

/**
 * Compares the app engine (Karachi + Hanafi, the India parameters) with Aladhan method=1 school=1 for
 * 50 Indian cities x 12 dates in 2026 (reference fetched once into test resources), and writes the table
 * to Namaz Tracker/screens/phase2/timing_test.txt.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class IndiaTimingTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val ist = ZoneId.of("Asia/Kolkata")

    private fun minutes(hhmm: String) = hhmm.substring(0, 2).toInt() * 60 + hhmm.substring(3, 5).toInt()

    private fun Instant.istMinutes(): Int {
        val t = java.time.Instant.ofEpochMilli(toEpochMilliseconds()).atZone(ist)
        return t.hour * 60 + t.minute
    }

    @Test
    fun `app times match Aladhan within 2 minutes`() {
        TimeZone.setDefault(TimeZone.getTimeZone(ist))
        val rows = json.decodeFromString<List<AladhanRow>>(
            javaClass.classLoader!!.getResource("aladhan_india_2026.json")!!.readText(),
        )
        val useCase = GetShariaTimesUseCase()
        val names = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
        val worst = IntArray(names.size)
        val out = StringBuilder()
        out.appendLine("App (adhan_kotlin, Karachi + Hanafi, Asia/Kolkata) minus Aladhan (method=1, school=1), minutes")
        out.appendLine("city | state | date | " + names.joinToString(" | "))
        for (r in rows) {
            val times = useCase(
                instant = Instant.parse("${r.date}T06:30:00Z"),
                calculationParameters = INDIA_PARAMETERS,
                calculationAdjustments = CalculationAdjustments(),
                arabicCalendar = "islamic",
                locationDetail = CalculationLocationDetail(lat = r.lat, long = r.lng),
                midnightMethod = MidnightMethod.SunsetToFajr,
            ).timesOrNull!!
            val app = listOf(times.fajr, times.sunrise, times.dhuhr, times.asr, times.maghrib, times.isha)
            val ref = listOf(r.Fajr, r.Sunrise, r.Dhuhr, r.Asr, r.Maghrib, r.Isha)
            val diffs = names.indices.map { app[it].istMinutes() - minutes(ref[it]) }
            diffs.forEachIndexed { i, d -> if (abs(d) > abs(worst[i])) worst[i] = d }
            out.appendLine("${r.name} | ${r.state} | ${r.date} | " + diffs.joinToString(" | ") { "%+d".format(it) })
        }
        val maxAbs = worst.maxOf { abs(it) }
        out.appendLine()
        out.appendLine("rows: ${rows.size}")
        out.appendLine("worst per prayer: " + names.indices.joinToString(", ") { "${names[it]} %+d".format(worst[it]) })
        out.appendLine("max |diff|: $maxAbs min -> ${if (maxAbs <= 2) "PASS" else "FAIL"} (limit 2)")
        File("../../screens/phase2").apply { mkdirs() }.resolve("timing_test.txt").writeText(out.toString())
        assertEquals(600, rows.size)
        assertTrue("max diff $maxAbs min", maxAbs <= 2)
    }

    @Test
    fun `gps fix snaps to the nearest bundled city`() {
        val cities = json.decodeFromString<List<IndiaCity>>(File("src/main/assets/india_cities.json").readText())
        assertEquals(3743, cities.size)
        // Charminar, Hyderabad
        val hyd = IndiaCities.nearest(cities, 17.3616, 78.4747)!!
        assertEquals("Hyderabad" to "Telangana", hyd.name to hyd.state)
        // Dadar and Gateway of India sit nearer GeoNames suburbs (Dharavi, Uran) but should read Mumbai
        assertEquals("Mumbai", IndiaCities.nearest(cities, 19.0178, 72.8478)!!.name)
        assertEquals("Mumbai", IndiaCities.nearest(cities, 18.9220, 72.8347)!!.name)
        // Satellite cities keep their own name
        assertEquals("Thane", IndiaCities.nearest(cities, 19.1863, 72.9756)!!.name)
        assertEquals("Noida", IndiaCities.nearest(cities, 28.5708, 77.3260)!!.name)
    }
}
