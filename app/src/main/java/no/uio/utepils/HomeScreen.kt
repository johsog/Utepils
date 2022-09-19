package no.uio.utepils

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import no.uio.utepils.data.*
import no.uio.utepils.dataclasses.Beverage
import no.uio.utepils.dataclasses.Forecast
import no.uio.utepils.ui.theme.UtepilsTheme
import java.text.SimpleDateFormat
import java.util.*

class HomeScreen: ComponentActivity() {
    private val viewModel = HomeScreenViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchLocation(this)
        setContent { MainView(viewModel) }
    }
}

@Composable
private fun MainView(mainViewModel: HomeScreenViewModel) {

    // Her observerer jeg variablene i ViewModelen, og når en av disse endrer verdi
    // oppdaterer hele viewet seg.
    val forecasts: List<Forecast>? by mainViewModel.forecasts.observeAsState()
    val selectedForecast: Forecast? by mainViewModel.selectedForecast.observeAsState()
    val location: LatLng? by mainViewModel.currentLocation.observeAsState()
    val dataStatus: DataSource.Status? by mainViewModel.dataStatus.observeAsState()
    val beverages: List<Beverage>? by mainViewModel.recommendations.observeAsState()

    // Når location ikke lenger er null og ikke er i nærheten av forrige API-resultat hentes
    // værdata på nytt.
    if (location != null && forecasts?.firstOrNull()?.location?.isNear(location) != true) mainViewModel.fetchData()

    // Vi har laget et spesialdesignet theme til appen slik at den skal se clean ut og støtte dark mode.
    UtepilsTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (dataStatus) {
                DataSource.Status.WAITING,
                DataSource.Status.FETCHING -> LoadingView(Data.TextStrings.loadingLabel())
                else -> {
                    // Vi valgte å benytte oss av en LazyColumn til å vise hovedsiden i tilfelle
                    // vi ønsket å vise veldig mange drikke-anbefalinger. På denne måten vil det
                    // ikke være like krevende å vise hundrevis av elementer i listen.
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            // Her viser vi hvilken dag som er valgt av brukeren.
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .padding(horizontal = 16.dp)) {
                                selectedForecast?.date?.let { date ->
                                    // Ved å bruke SimpleDateFormat med pattern og locale kan vi få
                                    // Norske navn på dager og måneder. Om flere språk skulle støttes
                                    // hadde man måttet lage et mer omfattende Locale-system.
                                    val format = SimpleDateFormat("EEEE, dd MMMM", Locale("NO"))
                                    Text(format.format(date).uppercase(),
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        // Dersom GPS er slått av eller telefonen ikke kan finne lokasjonen sin
                        // vises denne feilmeldingen.
                        if (dataStatus == DataSource.Status.NO_LOCATION) {
                            item {
                                Text("Vennligst skru på GPS og prøv igjen.", style = MaterialTheme.typography.body1)
                            }
                        }
                        // Dersom brukern trykker "Deny" når vi sprør om tilgang til location:
                        else if (dataStatus == DataSource.Status.LOCATION_ACCESS_DENIED) {
                            item {
                                Text("Vi må ha tilgang til lokasjonen din for å finne utepils-været der du er.\n\nVennligst start appen på nytt.",
                                    style = MaterialTheme.typography.body1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        // Dersom data ikke blir hentet er det oftets fordi man ikke har nettilgang
                        // eller er på en lokasjon som ikke støttes. Siden Nowcast kun støttes i
                        // Norden er dette også området appen støttes.
                        else if (dataStatus == DataSource.Status.FAILURE) {
                            item {
                                Text("Ingen nettverkstilgang eller så befinner du deg på en utsøttet lokasjon.", style = MaterialTheme.typography.body1)
                            }
                        }
                        // Om lokasjon og data hentes på korrekt måte vises utepils-resultatskjermen.
                        else if (dataStatus == DataSource.Status.SUCCESS) {
                            item {
                                forecasts?.let { DateSelector(it, selectedForecast!!, mainViewModel) }
                                UtepilsResult(selectedForecast!!)
                            }
                            // Dersom det er utepils-vær vises også drikke-anbefalinger basert på været.
                            if (selectedForecast?.isUtepils() == true) {
                                item {
                                    Text("Anbefalinger",
                                        style = MaterialTheme.typography.caption,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp))
                                }
                                // Så lenge "beverages" er null er ikke anbefalingene hentet enda.
                                // Disse hentes ikke før lokasjon og værdata er ferdig prossesert,
                                // og siden Vinmonopolet sin database er så stor tar dette litt tid.
                                // Derfor har vi en ProgressIndicator for å indikere at noe skjer i
                                // bakgrunnen.
                                if (beverages == null) {
                                    item {
                                        CircularProgressIndicator(color = MaterialTheme.colors.primary,
                                            modifier = Modifier.padding(vertical = 32.dp))
                                    }
                                } else {
                                    // Drikkevarene er sortert etter hvor godt de passer med været.
                                    // Den første vises med litt mer detaljer for å fremheve at dette
                                    // er det mest egnede produktet.
                                    beverages!!.firstOrNull()?.let {
                                        item {
                                            Divider(modifier = Modifier
                                                .padding(bottom = 8.dp)
                                                .padding(start = 16.dp))
                                            BeverageDetails(it)
                                        }
                                    }
                                    // Om man scroller videre kan man se flere anbefalinger.
                                    val recommendations = (beverages!!.filterIndexed { i, _ -> i != 0 })
                                    items(recommendations.size) { i ->
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Divider(modifier = Modifier.padding(start = 16.dp))
                                            BeverageListView(recommendations[i])
                                        }
                                    }
                                    item { 
                                        Divider(modifier = Modifier
                                            .padding(bottom = 16.dp)
                                            .padding(start = 16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelector(forecasts: List<Forecast>, selectedForecast: Forecast, viewModel: HomeScreenViewModel) {
    LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier =
    Modifier
        // Her har vi en horisontal scroll siden knappene er for brede for de fleste skjermer.
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
    ) {
        for (forecast in forecasts) {
            // Selve dato-knappen ligger inne i Forecast-objektet og får labelen sin bestemt via arv.
            forecast.ForecastButton(forecast == selectedForecast) {
                viewModel.changeForecast(forecast)
            }
        }
    }
}
/*
    Her vises resultatet av utepils-algoritmen. Symbolet fra MET brukes til å kjapt illustrere
    værforholdene. Her ønsker vi ikke å vise for mye detaljer om været.
 */
@Composable
private fun UtepilsResult(selectedForecast: Forecast) {
    Log.d("isUtepils", selectedForecast.toString())
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .aspectRatio(1f)
            .wrapContentWidth()
    ) {
        selectedForecast.weatherSymbolID?.let { symbolID ->
            Image(
                painter = painterResource(symbolID),
                contentDescription = "weathersymbol",
                modifier = Modifier
                    .size(88.dp)
            )
        }
        Text(Data.TextStrings.utepilsTitle(selectedForecast.isUtepils()),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        Text(Data.TextStrings.utepilsSubtitle(selectedForecast.isUtepils()),
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
    }
}

// En detaljrik oversikt over en drikkevare.
@Composable
fun BeverageDetails(beverage: Beverage) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        AsyncImage(model = beverage.imageURL, contentDescription = beverage.name, modifier = Modifier
            .padding(end = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxSize(0.33f)
            .background(Color.White)
            .padding(24.dp)
        )

        Column {
            beverage.kind?.let {
                Text(it.uppercase(), style = MaterialTheme.typography.subtitle2, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(beverage.name ?: "",
                style = MaterialTheme.typography.h3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            beverage.volume?.let {
                Text("$it liter", style = MaterialTheme.typography.subtitle2)
            }
            beverage.alcoholContent?.let {
                Text("$it %", style = MaterialTheme.typography.subtitle2)
            }
            Spacer(modifier = Modifier.size(0.dp, 32.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                beverage.price?.let {
                    Row(horizontalArrangement = Arrangement.End) {
                        val whole = it.toInt()
                        val decimals = ((it - whole).round(2) * 100).toInt()
                        Text("kr $whole",
                            style = MaterialTheme.typography.h2,
                            modifier = Modifier.padding(end = 2.dp))
                        Text("$decimals",
                            style = MaterialTheme.typography.h4)
                    }
                }
            }
            if (beverage.price != null && beverage.volume != null) {
                Text("kr ${(beverage.price!! / beverage.volume!!).round(2)} pr. liter", style = MaterialTheme.typography.subtitle2)
            }
        }
    }
}

// En plassbesparende oversikt over en drikkevare.
@Composable
private fun BeverageListView(beverage: Beverage) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(model = beverage.imageURL, contentDescription = null, modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .size(55.dp, 55.dp)
                .background(Color.White)
                .padding(4.dp)
            )
            Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                Text(beverage.name ?: "", style = MaterialTheme.typography.h5, maxLines = 2, overflow = TextOverflow.Ellipsis)
                beverage.kind?.let {
                    Text(it.uppercase(), style = MaterialTheme.typography.subtitle2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Text("kr ${beverage.price}", style = MaterialTheme.typography.body1)
    }
}

@Composable
private fun LoadingView(text: String = "Laster...") {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentSize(align = Alignment.Center)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colors.primary, modifier = Modifier.padding(bottom = 8.dp))
        Text(text, style = MaterialTheme.typography.body1, textAlign = TextAlign.Center)
    }
}

// Her er en preview som vi brukte til å opprette designet på appen, den funker ikke når dataene
// hentes fra API. Derfor hadde vi midlertidig eksempeldata i starten av prossessen for å gjøre
// design av UI og UX enklere med Compose sin Preview-funksjon.

/*
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DefaultPreview() {
    MainView(mainViewModel = HomeScreenViewModel())
}
 */