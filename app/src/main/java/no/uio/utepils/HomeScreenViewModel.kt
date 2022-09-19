package no.uio.utepils

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.utepils.data.DataSource
import no.uio.utepils.data.LocationHandler
import no.uio.utepils.data.isUtepils
import no.uio.utepils.dataclasses.Beverage
import no.uio.utepils.dataclasses.Forecast

/*
    Vi har designet appen etter MVVM-prinsippet slik at vi separerer UI, presentasjonslogikk og buisnesslogikk/data.
    I denne ViewModelen har vi samlet alle data som skal oppdatere UI-en i form av
    LiveData-objekter som er bundet til viewet.
 */
class HomeScreenViewModel: ViewModel() {
    val forecasts: MutableLiveData<List<Forecast>?> = MutableLiveData(emptyList())
    val dataStatus: MutableLiveData<DataSource.Status?> = MutableLiveData(DataSource.Status.WAITING)

    val selectedForecast: MutableLiveData<Forecast?> = MutableLiveData(null)
    val recommendations: MutableLiveData<List<Beverage>?> = MutableLiveData(null)

    val currentLocation: MutableLiveData<LatLng?> = MutableLiveData(null)
    private val locationListener: LocationListener = object : LocationListener {
        @Override
        override fun onLocationChanged(location: Location) {
            // Når man endrer lokasjon endres også currentLocation, som igjen endrer
            // UI-en siden den blir observert i HomeScreen.kt
            currentLocation.value = LatLng(location.latitude, location.longitude) }
        @Deprecated("Deprecated in Java")
        @Override
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        @Override
        override fun onProviderEnabled(provider: String) {}
        @Override
        override fun onProviderDisabled(provider: String) {}
    }

    // Her hentes lokasjon, men først må brukeren godta at appen får tilgang til enhetens lokasjon.
    fun fetchLocation(context: ComponentActivity) {
        // Vi bruker en Enum-status til å infomere UI-en om hva som foregår bak kulissene
        // slik at brukeren kan holdes oppdatert.
        dataStatus.value = DataSource.Status.FETCHING
        if (!LocationHandler.locationPermitted(context)) {
            LocationHandler.requestLocationAccess(context, this)
            return
        }
        LocationHandler.followLocation(context, locationListener)
    }

    // Denne funksjonen kaller på diverse funksjoner i DataSource, som asynkront henter data
    // fra API-ene.
    fun fetchData() {
        dataStatus.value = DataSource.Status.FETCHING
        if (currentLocation.value == null) dataStatus.value = DataSource.Status.NO_LOCATION
        else {
            CoroutineScope(Dispatchers.IO).launch {
                // Henter Nowcast og LocationForecast og samler dem i en liste. Denne listen
                // vises til brukeren slik at brukeren kan velge ønsket tidspunkt.
                val nowcast = DataSource.getNowcast(currentLocation.value!!)
                val forecasts = DataSource.getForecast(currentLocation.value!!)?.toMutableList()
                nowcast?.let { forecasts?.add(0, it) }

                // Henter anbefalinger så fort som mulig etter at værdataene er på plass.
                if (forecasts?.firstOrNull()?.isUtepils() == true) fetchRecommendations(forecasts.firstOrNull()?.forecast?.air_temperature)

                // Går tilbake i Main-threaden for å oppdatere UI-verdiene.
                CoroutineScope(Dispatchers.Main).launch {
                    this@HomeScreenViewModel.forecasts.value = forecasts
                    this@HomeScreenViewModel.selectedForecast.value = forecasts?.firstOrNull()

                    if (this@HomeScreenViewModel.forecasts.value.isNullOrEmpty()) dataStatus.value = DataSource.Status.FAILURE
                    else dataStatus.value = DataSource.Status.SUCCESS
                }
            }
        }
    }

    // Denne funksjonen henter drikkevare-anbefalinger og sorterer dem etter hvor godt i passer
    // med det valgte været. Vi baserer oss på temperatur til å anbefale drikke, siden
    // resten av været skal være stort sett skyfritt og vindstille.
    private fun fetchRecommendations(temp: Double?) {
        CoroutineScope(Dispatchers.IO).launch {
            DataSource.fetchBeverages()?.let { beverages ->
                launch(Dispatchers.Main) {
                    recommendations.value = beverages.sortedBy { (1 - it.matchRate(temp)) }.take(10)
                }
            }
        }
    }

    // Denne metoden kalles når brukeren velger en ny dato. Anbefalingene tømmes, drikkevarene
    // hentes fra cachen i DataSource, og de sorteres på nytt i fetchRecommendations-funksjonen.
    fun changeForecast(forecast: Forecast) {
        selectedForecast.value = forecast
        recommendations.value = null
        if (forecast.isUtepils()) fetchRecommendations(forecast.forecast?.air_temperature)
    }
}