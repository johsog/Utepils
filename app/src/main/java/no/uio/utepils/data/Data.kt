package no.uio.utepils.data

import no.uio.utepils.R
import kotlin.math.round

// Denne klassen er en samling av strenger som brukes i appen, i tillegg til en oversetting av
// MET-symbol-streng til et ikon i drawable-mappen.
class Data {
    companion object {
        fun getSymbol(name: String?): Int? {
            if (name == null) return null
            return when (name) {
                "clearsky_day" -> R.drawable.clearsky_day
                "clearsky_night" -> R.drawable.clearsky_night
                "clearsky_polartwilight" -> R.drawable.clearsky_polartwilight
                "cloudy" -> R.drawable.cloudy
                "fair_day" -> R.drawable.fair_day
                "fair_night" -> R.drawable.fair_night
                "fair_polartwilight" -> R.drawable.fair_polartwilight
                "fog" -> R.drawable.fog
                "heavyrain" -> R.drawable.heavyrain
                "heavyrainandthunder" -> R.drawable.heavyrainandthunder
                "heavyrainshowers_day" -> R.drawable.heavyrainshowers_day
                "heavyrainshowers_night" -> R.drawable.heavyrainshowers_night
                "heavyrainshowers_polartwilight" -> R.drawable.heavyrainshowers_polartwilight
                "heavyrainshowersandthunder_day" -> R.drawable.heavyrainshowersandthunder_day
                "heavyrainshowersandthunder_night" -> R.drawable.heavyrainshowersandthunder_night
                "heavyrainshowersandthunder_polartwilight" -> R.drawable.heavyrainshowersandthunder_polartwilight
                "heavysleet" -> R.drawable.heavysleet
                "heavysleetandthunder" -> R.drawable.heavysleetandthunder
                "heavysleetshowers_day" -> R.drawable.heavysleetshowers_day
                "heavysleetshowers_night" -> R.drawable.heavysleetshowers_night
                "heavysleetshowers_polartwilight" -> R.drawable.heavysleetshowers_polartwilight
                "heavysleetshowersandthunder_day" -> R.drawable.heavysleetshowersandthunder_day
                "heavysleetshowersandthunder_night" -> R.drawable.heavysleetshowersandthunder_night
                "heavysleetshowersandthunder_polartwilight" -> R.drawable.heavysleetshowersandthunder_polartwilight
                "heavysnow" -> R.drawable.heavysnow
                "heavysnowandthunder" -> R.drawable.heavysnowandthunder
                "heavysnowshowers_day" -> R.drawable.heavysnowshowers_day
                "heavysnowshowers_night" -> R.drawable.heavysnowshowers_night
                "heavysnowshowers_polartwilight" -> R.drawable.heavysnowshowers_polartwilight
                "heavysnowshowersandthunder_day" -> R.drawable.heavysnowshowersandthunder_day
                "heavysnowshowersandthunder_night" -> R.drawable.heavysnowshowersandthunder_night
                "heavysnowshowersandthunder_polartwilight" -> R.drawable.heavysnowshowersandthunder_polartwilight
                "lightrain" -> R.drawable.lightrain
                "lightrainandthunder" -> R.drawable.lightrainandthunder
                "lightrainshowers_day" -> R.drawable.lightrainshowers_day
                "lightrainshowers_night" -> R.drawable.lightrainshowers_night
                "lightrainshowers_polartwilight" -> R.drawable.lightrainshowers_polartwilight
                "lightrainshowersandthunder_day" -> R.drawable.lightrainshowersandthunder_day
                "lightrainshowersandthunder_night" -> R.drawable.lightrainshowersandthunder_night
                "lightrainshowersandthunder_polartwilight" -> R.drawable.lightrainshowersandthunder_polartwilight
                "lightsleet" -> R.drawable.lightsleet
                "lightsleetandthunder" -> R.drawable.lightsleetandthunder
                "lightsleetshowers_day" -> R.drawable.lightsleetshowers_day
                "lightsleetshowers_night" -> R.drawable.lightsleetshowers_night
                "lightsleetshowers_polartwilight" -> R.drawable.lightsleetshowers_polartwilight
                "lightsnow" -> R.drawable.lightsnow
                "lightsnowandthunder" -> R.drawable.lightsnowandthunder
                "lightsnowshowers_day" -> R.drawable.lightsnowshowers_day
                "lightsnowshowers_night" -> R.drawable.lightsnowshowers_night
                "lightsnowshowers_polartwilight" -> R.drawable.lightsnowshowers_polartwilight
                "lightssleetshowersandthunder_day" -> R.drawable.lightssleetshowersandthunder_day
                "lightssleetshowersandthunder_night" -> R.drawable.lightssleetshowersandthunder_night
                "lightssleetshowersandthunder_polartwilight" -> R.drawable.lightssleetshowersandthunder_polartwilight
                "lightssnowshowersandthunder_day" -> R.drawable.lightssnowshowersandthunder_day
                "lightssnowshowersandthunder_night" -> R.drawable.lightssnowshowersandthunder_night
                "lightssnowshowersandthunder_polartwilight" -> R.drawable.lightssnowshowersandthunder_polartwilight
                "partlycloudy_day" -> R.drawable.partlycloudy_day
                "partlycloudy_night" -> R.drawable.partlycloudy_night
                "partlycloudy_polartwilight" -> R.drawable.partlycloudy_polartwilight
                "rain" -> R.drawable.rain
                "rainandthunder" -> R.drawable.rainandthunder
                "rainshowers_day" -> R.drawable.rainshowers_day
                "rainshowers_night" -> R.drawable.rainshowers_night
                "rainshowers_polartwilight" -> R.drawable.rainshowers_polartwilight
                "rainshowersandthunder_day" -> R.drawable.rainshowersandthunder_day
                "rainshowersandthunder_night" -> R.drawable.rainshowersandthunder_night
                "rainshowersandthunder_polartwilight" -> R.drawable.rainshowersandthunder_polartwilight
                "sleet" -> R.drawable.sleet
                "sleetandthunder" -> R.drawable.sleetandthunder
                "sleetshowers_day" -> R.drawable.sleetshowers_day
                "sleetshowers_night" -> R.drawable.sleetshowers_night
                "sleetshowers_polartwilight" -> R.drawable.sleetshowers_polartwilight
                "sleetshowersandthunder_day" -> R.drawable.sleetshowersandthunder_day
                "sleetshowersandthunder_night" -> R.drawable.sleetshowersandthunder_night
                "sleetshowersandthunder_polartwilight" -> R.drawable.sleetshowersandthunder_polartwilight
                "snow" -> R.drawable.snow
                "snowandthunder" -> R.drawable.snowandthunder
                "snowshowers_day" -> R.drawable.snowshowers_day
                "snowshowers_night" -> R.drawable.snowshowers_night
                "snowshowers_polartwilight" -> R.drawable.snowshowers_polartwilight
                "snowshowersandthunder_day" -> R.drawable.snowshowersandthunder_day
                "snowshowersandthunder_night" -> R.drawable.snowshowersandthunder_night
                "snowshowersandthunder_polartwilight" -> R.drawable.snowshowersandthunder_polartwilight
                else -> null
            }
        }
    }

    class TextStrings {
        companion object {
            // Overksriften til utepilsresulatet, med en boolean som avgjør hvilken streng som returneres.
            fun utepilsTitle(isUtepils: Boolean): String { return if (isUtepils) "Det er utepilsvær!" else "Det er ikke utepilsvær..." }

            // Underoverskriften til utepilsresultatet, med boolean som avgjør hvilken liste en
            // tilfeldig streng plukkes fra.
            fun utepilsSubtitle(isUtepils: Boolean): String {
                if (isUtepils) {
                    return listOf(
                        "Knekk en kald en og sett deg i solsteken!",
                        "Jekk opp en bjørnunge! Det er sol!",
                        "Smil, på tide å knekke en god pil(s)",
                        "Vis litt skills og hell i deg en pils!",
                        "Ingen vits å sitte inne! Gå ut og knekk deg en kald pinne."
                    ).random()
                } else {
                    return listOf(
                        "Er nok bedre med en klassisk innepils i dag.",
                        "Du må nok smøre deg med tålmodighet.",
                        "I dag er nok ikke din dag.",
                        "Ingen utepils i dag. Oppsøk lege om ølsuget vedvarer.",
                        "Får håpe abstinensene ikke blir for ille.",
                        "Alvorlig innepils-fare!"
                    ).random()
                }
            }

            // Innlastnignen har også en label som velges tilfeldig.
            fun loadingLabel(): String {
                return listOf(
                    "Sjekker forholdene for utepils...",
                    "2 sec brb...",
                    "Det er en eim av utepils i luften...",
                    "Vi krysser fingrene!",
                    "Kan dette være din utepils-dag?",
                    "Kalkulerer pils-data...",
                    "Laster inn utepils-vær...",
                    "Ber værgudene om utepils-vær...",
                    "Det er..."
                ).random()
            }
        }
    }
}