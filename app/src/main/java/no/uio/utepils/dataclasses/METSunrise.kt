package no.uio.utepils.dataclasses

import no.uio.utepils.data.toDate
import java.util.*

// Dataklasser for Sunrise API.
class METSunrise(private val location: Location?) {
    // Her har vi laget noen metoder og computing-variables som gjør det lettere å hente ut
    // data fra objektet.
    fun isDay(date: Date = Date()): Boolean = date.after(sunrise) && date.before(sunset)
    val sunrise get() = location?.time?.firstOrNull()?.sunrise?.time.toDate("yyyy-MM-dd'T'HH:mm:ssZ")
    val sunset get() = location?.time?.firstOrNull()?.sunset?.time.toDate("yyyy-MM-dd'T'HH:mm:ssZ")

    data class Location(val height: String?, val latitude: String?, val longitude: String?, val time: List<Instant>?)
    data class Sunrise(val desc: String?, val time: String?)
    data class Sunset(val desc: String?, val time: String?)
    data class Instant(val date: String?, val sunrise: Sunrise?, val sunset: Sunset?)
}