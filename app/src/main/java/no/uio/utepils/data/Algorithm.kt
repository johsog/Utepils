package no.uio.utepils.data

import android.util.Log
import no.uio.utepils.dataclasses.Forecast
import java.util.*
import kotlin.math.max
import kotlin.math.min

// Dette er algorimen som brukes for å bestemme om det er utepilsvær eller ikke.
// Vi har valgt å legge denne i en egen fil fordi den er så stor, og er en kjernefunksjon i appen.
// Denne er stappfull av print-setninger vi har brukt for å justere algoritmen basert på værobservasjoner.
fun Forecast.isUtepils(): Boolean {
    // Limit er den høyest verdien raten kan ha for å være utepilsvær, vi har funnet ut at
    // 55% er en god balanse.
    val limit = 0.55

    // Vi begynner med en utepils-rate på 1.0 altså 100%.
    var totalRate = 1.0

    // Regn er ganske viktig, så jo nærmere været er 5 mm i timen, jo lavere blir nedbørsraten.
    Log.d("isUtepils", "----------  RAIN  ----------")
    val pr = forecast?.precipitation_rate
    if (pr != null) {
        val maxRain = 5 // mm/h
        val precipitationRate = max(0.0, (maxRain - pr) / maxRain)
        // Vi multipliserer nedbørsraten med totalraten.
        if (pr > maxRain) totalRate *= precipitationRate
        Log.d("isUtepils", "PrecipitationRate: $precipitationRate")
    } else {
        Log.d("isUtepils", "No precipitation data")
    }


    // Skyer er også veldig viktig, og vi har data for høye, medium og lave skyer.
    // Etter mange ulike tester har vi funnet ut at gjennomsnittet av disse verdiene
    // gir mest korrekt resultat.
    Log.d("isUtepils", "---------- CLOUDS ----------")
    var cloudRate = 1.0
    if (forecast?.cloud_area_fraction_high != null &&
        forecast.cloud_area_fraction_medium != null &&
        forecast.cloud_area_fraction_low != null) {
        val highCloudRate = (1000 - forecast.cloud_area_fraction_high!!.times(10)).div(1000)
        val mediumCloudRate = (1000 - forecast.cloud_area_fraction_medium!!.times(10)).div(1000)
        val lowCloudRate = (1000 - forecast.cloud_area_fraction_low!!.times(10)).div(1000)

        cloudRate = listOf(highCloudRate, mediumCloudRate, lowCloudRate).average().round(2)

        Log.d("isUtepils", "highCloudRate: $highCloudRate")
        Log.d("isUtepils", "mediumCloudRate: $mediumCloudRate")
        Log.d("isUtepils", "lowCloudRate: $lowCloudRate")
    } else if (forecast?.cloud_area_fraction != null) cloudRate = (1000 - forecast.cloud_area_fraction!!.times(10)).div(1000)
    else Log.d("isUtepils", "No cloud data")

    Log.d("isUtepils", "cloudRate: $cloudRate")
    totalRate *= cloudRate


    // Tåke er også et viktig aspekt med utepils, og fungerer på ganske lik måte som de andre verdiene.
    Log.d("isUtepils", "----------- FOG ------------")
    var fogRate = 1.0
    if (forecast?.fog_area_fraction != null) fogRate = (1000 - forecast.fog_area_fraction!!.times(10)).div(1000).round(2)
    else Log.d("isUtepils", "No fog data")

    Log.d("isUtepils", "fogRate: $fogRate")
    totalRate *= fogRate


    // Vindhastigheten har vi satt til max 15 m/s, og siden limiten står på 0.55 så vil den reelle
    // maksimale vindhastigheten ved optimale forhold være 7.5 m/s. Som defineres av MET som laber bris.
    Log.d("isUtepils", "----------- WIND -----------")
    val maxWindSpeed = 15 // m/s
    var windRate = 1.0
    if (forecast?.wind_speed != null) windRate = 1 - (forecast.wind_speed!! / maxWindSpeed)
    else Log.d("isUtepils", "No wind data")

    Log.d("isUtepils", "windRate: $windRate")
    totalRate *= windRate


    // Temperatur er også en viktig del, så jo kaldere det er under 0 grader, jo lavere blir
    // temperatur-raten.
    Log.d("isUtepils", "------- TEMPERATURE --------")
    var tempRate = 1.0
    if (forecast?.air_temperature != null) tempRate = 1 - ((-1 * min(0.0, forecast.air_temperature!!) / 100))
    else Log.d("isUtepils", "No temperature data")

    Log.d("isUtepils", "tempRate: $tempRate")
    totalRate *= tempRate

    // Dersom man ser på dagen i dag, så må vi sjekke om solen er oppe.
    // Dette gjør vi med Sunrise API-et til MET. Denne kan videreutvikles ved å
    // ha en rate som følger sola, så utepils-raten reduseres når sola nærmer seg å gå ned.
    if (sunrise.sunrise?.isSameDate(Date()) == true) {
        Log.d("isUtepils", "--------- DAYTIME ----------")
        Log.d("isUtepils", "Sunrise: ${sunrise.sunrise}")
        Log.d("isUtepils", "Current: ${Date()}")
        Log.d("isUtepils", "Sunset: ${sunrise.sunset}")

        if (sunrise.isDay()) {
            Log.d("isUtepils", "Det er dag!")
        } else {
            Log.d("isUtepils", "Det er natt!")
            totalRate = 0.0
        }
    }

    Log.d("isUtepils", "---------- TOTAL -----------")
    Log.d("isUtepils", "Total utepilsrate: ${(totalRate * 100).toInt()}%")
    if (totalRate > limit) Log.d("isUtepils", "Ja, det blir utepils!")
    else Log.d("isUtepils", "Nei, det blir ikke utepils!")

    // Til slutt sammenlikner vi raten med limiten for å returnere en boolean for om det er
    // utepils eller ikke.
    return totalRate > limit
}