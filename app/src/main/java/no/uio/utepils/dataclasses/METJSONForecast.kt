package no.uio.utepils.dataclasses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import no.uio.utepils.data.DataSource
import no.uio.utepils.data.Data
import no.uio.utepils.data.isSameDate
import no.uio.utepils.data.toDate
import no.uio.utepils.ui.theme.accentColor
import no.uio.utepils.ui.theme.tetiaryLabel
import okhttp3.internal.toImmutableList
import java.util.*

// Her har vi laget en egen klasse for å håndtere værmeldinger for hele dagen.
// Siden vi kun skal vise et gjennomsnitt er det lettere å samle alt i ett objekt.
open class Forecast (
    val date: Date,
    val location: LatLng,
    val forecast: METJSONForecast.ForecastTimeInstant?,
    val weatherSummary: METJSONForecast.WeatherPeriod?,
    val sunrise: METSunrise
) {
    // Her har vi laget noen metoder og computing-variables som gjør det lettere å hente ut
    // data fra objektet.
    protected open val buttonText get() = "$dayOfMonth"
    private val dayOfMonth by lazy {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_MONTH]
    }
    val weatherSymbolID get() = Data.getSymbol(weatherSummary?.summary?.symbol_code)

    // Dette er knappen representerer dette objektet på hoveskjermen i Jetpack Compose.
    @Composable
    fun ForecastButton(isSelected: Boolean, size: Dp = 48.dp, onClick: () -> Unit) {
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(!isSelected, onClick = onClick)
                .background(
                    color = if (isSelected) MaterialTheme.colors.accentColor
                    else MaterialTheme.colors.tetiaryLabel
                )
                .size(size)) {
            Text(buttonText.lowercase(), style = MaterialTheme.typography.h3, color = Color.White)
        }
    }

    // En toString() som gjør det lett å se innholdet i et Forecast-objekt.
    override fun toString(): String {
        var string = "---------- ${javaClass.simpleName.uppercase()} ----------\n"
        string += "$date\n"
        weatherSummary?.summary?.symbol_code?.let { string += "Symbol: $it\n" }
        forecast?.air_temperature?.let { string += "Temperature: $it cº\n" }
        forecast?.cloud_area_fraction?.let { string += "Clouds: $it %\n" }
        forecast?.cloud_area_fraction_high?.let { string += "High clouds: $it %\n" }
        forecast?.cloud_area_fraction_medium?.let { string += "Medium clouds: $it %\n" }
        forecast?.cloud_area_fraction_low?.let { string += "Low clouds: $it %\n" }
        forecast?.fog_area_fraction?.let { string += "Fog: $it %\n" }
        forecast?.wind_speed?.let { string += "Wind: $it m/s\n" }
        forecast?.wind_speed_of_gust?.let { string += "Gust: $it m/s\n" }
        forecast?.precipitation_rate?.let { string += "Precipitation rate: $it mm/h" }

        return string
    }
}

// Nowcast inneholder de samme verdiene som Forecast, så derfor valgte vi å bare arve fra Forecast.
class Nowcast(metjsonForecast: METJSONForecast, sunrise: METSunrise): Forecast(
    date = metjsonForecast.properties.timeseries.firstOrNull()?.date!!,
    location = metjsonForecast.geometry.latLng,
    forecast = metjsonForecast.properties.timeseries.firstOrNull()?.data?.instant?.details,
    weatherSummary = metjsonForecast.properties.timeseries.firstOrNull()?.data?.next_1_hours ?:
        metjsonForecast.properties.timeseries.firstOrNull()?.data?.next_6_hours ?:
        metjsonForecast.properties.timeseries.firstOrNull()?.data?.next_12_hours
    ,
    sunrise = sunrise
) {
    // Siden nowcast skal representere "her og nå" endrer vi teksten på knappen til "Nå".
    override val buttonText get() = "Nå"
}

class METJSONForecast (
    // private val type: String = "Feature",
    val geometry: PointGeometry,
    val properties: Forecast
) {
    // Denne metoden går gjennom alle timestepsene i selg selv og returnerer en liste
    // med Forecast-objekter som representerer gjennomsnittet av være for en dag.
    // Dette er gjort ved å ta gjennomsnittet av alle verdiene i hver time-step for hver dag,
    // og legge de til i ett nytt Forecast-objekt.
    suspend fun getForecasts(): List<no.uio.utepils.dataclasses.Forecast> {
        val forecast: MutableList<no.uio.utepils.dataclasses.Forecast> = mutableListOf()

        // Lager en liste med ForecastTimeSteps for hver kommende dag.
        val tmp: MutableList<ForecastTimeStep> = mutableListOf()
        for (forecastTimeStep in properties.timeseries) {
            forecastTimeStep.date?.let { date ->
                val sunrise = DataSource.fetchSunrise(geometry.latLng, date)

                if (sunrise != null) {
                    // Legger til alle TimeSteps som er mens sola er oppe.
                    if (sunrise.isDay(date) && !date.isSameDate(Date())) {
                        forecastTimeStep.let { tmp.add(it) }
                    } else if (tmp.isNotEmpty()) {
                        // Finner en 12-timers varsel som dekker nesten hele dagen for
                        // å få et representabelt symbol.
                        var avg = tmp[tmp.size / 3]
                        if (avg.data.next_12_hours?.summary?.symbol_code == null) {
                            for (forc in tmp) {
                                forc.data.next_12_hours?.summary?.symbol_code?.let {
                                    avg = forc
                                }
                            }
                        }
                        // Oppretter et nytt Forecast-objekt i lista.
                        // Dette objekter holder gjennomsnittlige data for den gitte dagen.
                        forecast.add(
                            Forecast(
                                date = avg.date!!,
                                location = geometry.latLng,
                                forecast = averageForcasts(tmp),
                                weatherSummary = avg.data.next_12_hours,
                                sunrise = sunrise
                            )
                        )
                        tmp.clear()
                    }
                }
            }
        }
        return forecast.toImmutableList()
    }

    // Her tar vi gjennomsnittet av en liste med værmeldings-timeSteps, og returnerer en
    // ForecastTimeInstant.
    private fun averageForcasts(forecasts: List<ForecastTimeStep>): ForecastTimeInstant {
        var air_pressure_at_sea_level = 0.0
        var air_temperature = 0.0
        var cloud_area_fraction = 0.0
        var cloud_area_fraction_high = 0.0
        var cloud_area_fraction_low = 0.0
        var cloud_area_fraction_medium = 0.0
        var dew_point_temperature = 0.0
        var fog_area_fraction = 0.0
        var precipitation_rate = 0.0
        var relative_humidity = 0.0
        var wind_from_direction = 0.0
        var wind_speed = 0.0
        var wind_speed_of_gust = 0.0

        for (forecast in forecasts) {
            forecast.data.instant.details?.air_pressure_at_sea_level?.let { air_pressure_at_sea_level += it }
            forecast.data.instant.details?.air_temperature?.let { air_temperature += it }
            forecast.data.instant.details?.cloud_area_fraction?.let { cloud_area_fraction += it }
            forecast.data.instant.details?.cloud_area_fraction_high?.let { cloud_area_fraction_high += it }
            forecast.data.instant.details?.cloud_area_fraction_low?.let { cloud_area_fraction_low += it }
            forecast.data.instant.details?.cloud_area_fraction_medium?.let { cloud_area_fraction_medium += it }
            forecast.data.instant.details?.dew_point_temperature?.let { dew_point_temperature += it }
            forecast.data.instant.details?.fog_area_fraction?.let { fog_area_fraction += it }
            forecast.data.instant.details?.precipitation_rate?.let { precipitation_rate += it }
            forecast.data.instant.details?.relative_humidity?.let { relative_humidity += it }
            forecast.data.instant.details?.wind_from_direction?.let { wind_from_direction += it }
            forecast.data.instant.details?.wind_speed?.let { wind_speed += it }
            forecast.data.instant.details?.wind_speed_of_gust?.let { wind_speed_of_gust += it }
        }

        return ForecastTimeInstant(
            air_pressure_at_sea_level = air_pressure_at_sea_level / forecasts.size,
            air_temperature = air_temperature / forecasts.size,
            cloud_area_fraction = cloud_area_fraction / forecasts.size,
            cloud_area_fraction_high = cloud_area_fraction_high / forecasts.size,
            cloud_area_fraction_low = cloud_area_fraction_low / forecasts.size,
            cloud_area_fraction_medium = cloud_area_fraction_medium / forecasts.size,
            dew_point_temperature = dew_point_temperature / forecasts.size,
            fog_area_fraction = fog_area_fraction / forecasts.size,
            precipitation_rate = precipitation_rate / forecasts.size,
            relative_humidity = relative_humidity / forecasts.size,
            wind_from_direction = wind_from_direction / forecasts.size,
            wind_speed = wind_speed / forecasts.size,
            wind_speed_of_gust = wind_speed_of_gust / forecasts.size
        )
    }

    // Her er resten av dataklassene for MET-modellen.
    data class Forecast(val meta: WeatherModel, val timeseries: List<ForecastTimeStep>)
    data class WeatherModel(val units: ForecastUnits, val updated_at: String, val radar_coverage: String?)
    data class ForecastUnits (
        val air_pressure_at_sea_level: String?,
        val air_temperature: String?,
        val air_temperature_max: String?,
        val air_temperature_min: String?,
        val cloud_area_fraction: String?,
        val cloud_area_fraction_high: String?,
        val cloud_area_fraction_low: String?,
        val cloud_area_fraction_medium: String?,
        val dew_point_temperature: String?,
        val fog_area_fraction: String?,
        val precipitation_amount: String?,
        val precipitation_amount_max: String?,
        val precipitation_amount_min: String?,
        val probability_of_precipitation: String?,
        val probability_of_thunder: String?,
        val relative_humidity: String?,
        val ultraviolet_index_clear_sky_max: String?,
        val wind_from_direction: String?,
        val wind_speed: String?,
        val wind_speed_of_gust: String?
    )
    data class ForecastTimeStep(val data: WeatherData, val time: String) { val date get() = time.toDate("yyyy-MM-dd'T'HH:mm:ss'Z'") }
    data class WeatherData(
        val instant: WeatherInstant,
        val next_12_hours: WeatherPeriod?,
        val next_1_hours: WeatherPeriod?,
        val next_6_hours: WeatherPeriod?
        )
    data class WeatherInstant(val details: ForecastTimeInstant?)
    data class WeatherPeriod(val details: ForecastTimePeriod, val summary: ForecastSummary)
    data class ForecastTimeInstant (
        var air_pressure_at_sea_level: Double?,
        var air_temperature: Double?,
        var cloud_area_fraction: Double?,
        var cloud_area_fraction_high: Double?,
        var cloud_area_fraction_low: Double?,
        var cloud_area_fraction_medium: Double?,
        var dew_point_temperature: Double?,
        var fog_area_fraction: Double?,
        var precipitation_rate: Double?,
        var relative_humidity: Double?,
        var wind_from_direction: Double?,
        var wind_speed: Double?,
        var wind_speed_of_gust: Double?
    )
    data class ForecastTimePeriod (
        val air_temperature_max: Double?,
        val air_temperature_min: Double?,
        val precipitation_amount: Double?,
        val precipitation_amount_max: Double?,
        val precipitation_amount_min: Double?,
        val probability_of_precipitation: Double?,
        val probability_of_thunder: Double?,
        val ultraviolet_index_clear_sky_max: Double?
    )
    data class ForecastSummary(val symbol_code: String, val symbol_confidence: String?)

    data class PointGeometry(val type: String = "Point", private val coordinates: List<Double>) {
        // coordinates = [longitude, latitude, altitude]. All numbers in decimal.

        // En enkel metode for å hente ut en LatLng, som vi bruker for koordinater i systemet.
        val latLng: LatLng get() = LatLng(coordinates[1], coordinates[0])
    }
}
