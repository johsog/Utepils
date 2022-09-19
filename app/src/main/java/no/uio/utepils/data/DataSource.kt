package no.uio.utepils.data

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.uio.utepils.dataclasses.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/*
    DataSource-klassen håndterer alt som har med API og datainnhenting å gjøre.
    Vi tar i bruk 3 ulike API-er fra MET, i tillegg til Vinmonopolets API som vi har
    fått tilgang til. I tilleg har vi laget vårt eget butikk-API som vi bruker for å
    integrere varer som kun finnes i butikk. Denne er fåreløpig ikke så stor, siden
    funksjonene i applikasjonen er viktigere å bruke tid på for dette prosjektet.
 */
class DataSource {
    companion object {
        private val gson = Gson()

        // Her er en liten samling variabler som brukes som en minne-cache, på denne måten
        // slipper vi å gjøre unødvendige API-kall.
        private var lastLocation: LatLng? = null
        private var nowcast: Nowcast? = null
        private var forecast: List<Forecast>? = null
        private var locationForecast: METJSONForecast? = null
        private var sunrise: METSunrise? = null
        private var beverages: List<Beverage>? = null

        // Denne funksjonen henter MET-apiet Nowcast og returnerer et Nowcast-objekt.
        // Siden dette er en suspend funksjon må denne kalles på i en coroutine slik at
        // kallet skjer asynkront.
        suspend fun getNowcast(location: LatLng): Nowcast? {
            // Først sjekker vi om det finnes et nowcast-objekt i cachen, og om det gjelder
            // samme sted som et evt. eksisterende objekt. I såfall returneres dette så vi
            // slipper å gjøre et unødvendig kall.
            if (nowcast != null && location.isNear(nowcast?.location)) return nowcast

            val nowcast = fetchMET(location, API.Nowcast)
            val locationForecast = fetchMET(location, API.LocationForecast)
            val sunrise = fetchSunrise(location)
            if (sunrise == null || nowcast == null) return null

            if (locationForecast != null) {
                val nowcastDetails = nowcast.properties.timeseries.firstOrNull()?.data?.instant?.details
                val locationForecastDetails = locationForecast.properties.timeseries.firstOrNull()?.data?.instant?.details

                // Siden Nowcast ikke inneholer sky-data må dette hentes fra LocationForecast-APIet
                // og legges inn i Nowcast-objektet. Skydataene er for den neste timen, så de er
                // så oppdaterte som vi kan få de.
                nowcastDetails?.cloud_area_fraction = locationForecastDetails?.cloud_area_fraction
                nowcastDetails?.cloud_area_fraction_high = locationForecastDetails?.cloud_area_fraction_high
                nowcastDetails?.cloud_area_fraction_medium = locationForecastDetails?.cloud_area_fraction_medium
                nowcastDetails?.cloud_area_fraction_low = locationForecastDetails?.cloud_area_fraction_low
            }

            // Her oppretter vi Nowcast-objektet og lagrer det i cachen-før vi returnerer det.
            // Dette objektet har vi designet selv for å slippe å operere med de avanserte MET-klassene.
            Nowcast(nowcast, sunrise).let {
                Companion.nowcast = it
                return it
            }
        }

        // Denne funksjonen henter data fra LocationForecast-APIet og
        // legger i/henter fra cache når nødvendig.
        suspend fun getForecast(location: LatLng): List<Forecast>? {
            if (forecast != null && location.isNear(forecast?.firstOrNull()?.location)) return forecast

            fetchMET(location, API.LocationForecast)?.getForecasts()?.let {
                forecast = it
                return it
            }
            return null
        }

        // Siden Nowcast og LocationForecast er så like, og bruker omtrent samme klassestruktur
        // fant vi ut at det var enklest å kombinere API-hentingen i samme funksjon.
        private suspend fun fetchMET(location: LatLng, api: API): METJSONForecast? {
            if (locationForecast != null &&
                api == API.LocationForecast &&
                location.isNear(locationForecast?.geometry?.latLng))
                return locationForecast

            val url = api.getURL(location)
            try {
                val response = Fuel.get(url).awaitString()
                val forecast = gson.fromJson(response, METJSONForecast::class.java)
                Log.d("fetchMETAPI", "Fetched ${api.name}")
                lastLocation = location
                if (api == API.LocationForecast) locationForecast = forecast

                return forecast
            } catch(exception: Exception) {
                Log.d("fetchMETAPI", exception.message ?: "")
            }
            return null
        }

        // Sunrise-apiet var deromot ganske annerledes, så det trengte en egen funksjon.
        suspend fun fetchSunrise(location: LatLng, date: Date = Date()): METSunrise? {
            if (sunrise?.sunrise?.isSameDate(date) == true && location.isNear(lastLocation) ) return sunrise

            // Vi bruker SimpleDateFormat til å lage strenger på formen som argumentene til Sunrise krever.
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("NO"))
            val offsetFormat: DateFormat = SimpleDateFormat("Z", Locale("NO"))

            // For å matche formatet til MET måtte vi lure inn et kolin i tidssonen.
            val offsetString = StringBuilder(offsetFormat.format(date)).insert(3, ":").toString()

            val url = "https://in2000-apiproxy.ifi.uio.no/weatherapi/sunrise/2.0/.json" +
                      "?date=${dateFormat.format(date)}" +
                      "&lat=${location.latitude}" +
                      "&lon=${location.longitude}" +
                      "&offset=${offsetString}"

            try {
                val response = Fuel.get(url).awaitString()
                val sunrise = gson.fromJson(response, METSunrise::class.java)
                Log.d("fetchSunrise", "SUCCESS! ${dateFormat.format(date)}")

                Companion.sunrise = sunrise
                lastLocation = location

                return sunrise
            } catch(exception: Exception) {
                Log.d("fetchSunriseError", exception.message ?: "")
            }
            return null
        }

        // Denne funksjonen henter data fra butikk-apien vår og Vinmonopolets API. Siden disse
        // bruker samme klasser er det lett å legge innholdet fra begge i samme liste.
        suspend fun fetchBeverages(): List<Beverage>? {
            if (beverages != null) return beverages
            val url = API.Vinmonopolet.getURL(search = "\"Øl\"")
            try {
                // Vi hoster butikk-JSON-filen via Discord siden dette er en kjapp og enkel måte å
                // simulere et API-kall. Denne løsningen er selvfølgelig sub-optimal for apper
                // som skal releases.
                val storeBeerURL = "https://cdn.discordapp.com/attachments/953630063427989524/976784433380220978/store_beer.json"
                val storeResponse = Fuel.get(storeBeerURL).awaitString()

                // Her hvor vi henter data fra Vinmonopolet må vi legge en nøkkel i headeren,
                // Vi bestemte oss bare for å hardkode den inn, siden dette kun er til et
                // skoleprosjekt. Denne burde noko blitt lagret på et litt tryggere sted om appen
                // skulle deployes på PlayStore.
                // Fjernet vinmonopolet api-nøkkel for å legge ut på github
                val list: MutableList<Beverage> =(gson.fromJson(storeResponse, object : TypeToken<List<Beverage>?>() {}.type))
                beverages = list
                Log.d("fetchBeverages", "Fetched beverages")
                return list
            } catch (exception: Exception) {
                Log.d("fetchBeveragesError", exception.message ?: "")
            }
            return null
        }
    }

    // Denne enum-klassen blir brukt til å oppdatere Viewet på hva som foregår i ViewModel og DataSource.
    enum class Status { WAITING, FETCHING, SUCCESS, FAILURE, NO_LOCATION, LOCATION_ACCESS_DENIED }
}

// Vi har også laget en enum-klasse for å holde på url-ene til de ulike api-ene.
private enum class API(val string: String) {
    LocationForecast("locationforecast"),
    Nowcast("nowcast"),
    Vinmonopolet("vinmonopolet");

    fun getURL(coordinates: LatLng): String {
        return "https://in2000-apiproxy.ifi.uio.no/weatherapi/$string/2.0/complete?lat=${coordinates.latitude}&lon=${coordinates.longitude}"
    }

    fun getURL(maxResults: Int = 5000, search: String? = null): String {
        var url = "https://apis.vinmonopolet.no/press-products/v1/details-normal"
        url += "?maxResults=$maxResults"
        search?.let { url += "&freeText=${it.replace(' ', '_')}" }
        return url
    }
}