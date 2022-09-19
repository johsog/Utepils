package no.uio.utepils.data

import com.google.android.gms.maps.model.LatLng
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

// Denne funksjonen runder desimaltall til x antall desimaler.
fun Double.round(decimals: Int = 1): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

// Denne hjelpefunksjonen sjekker om to LatLng-lokasjonsobjekter er nærme hverandre. Denne er veldig
// kjekk å bruke for å sjekke om en nærliggende lokasjon allerede har blitt hentet data fra.
fun LatLng.isNear(other: LatLng?): Boolean {
    return this.latitude.round(3) == other?.latitude?.round(3) &&
            this.longitude.round(3) == other.longitude.round(3)
}

// Denne funksjonen konverterer en streng til et Date-objekt basert på formatet som sendes med i arguementet.
fun String?.toDate(pattern: String) = this?.let { SimpleDateFormat(pattern, Locale.getDefault()).parse(it) }

// Denne funksjonen sjekker om to Date-objekter er på damme dato.
fun Date.isSameDate(other: Date?): Boolean {
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    if (other != null) return dateFormat.format(this) == dateFormat.format(other)
    return false
}
