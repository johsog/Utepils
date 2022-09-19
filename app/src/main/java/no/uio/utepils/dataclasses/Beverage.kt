package no.uio.utepils.dataclasses

import no.uio.utepils.data.round
import kotlin.random.Random

// Data-klasse som er modellert etter Vinmonopolets API.
// Vår egene butikk-api er også designet etter denne, så begge kan bruke de samme klassene.
data class Beverage (
    private val basic: Basic? = null,
    private val logistics: Logistics? = null,
    private val origins: Origins? = null,
    private val properties: Properties? = null,
    private val classification: Classification? = null,
    private val ingredients: Ingredients? = null,
    private val description: Description? = null,
    private val assortment: Assortment? = null,
    private val prices: List<Price>? = null,
    private val attributes: List<Any>? = null,
    private val lastChanged: LastChanged? = null)
{
    // Her har vi laget noen metoder og computing-variables som gjør det lettere å hente ut
    // data fra objektet.
    val name: String? get() = basic?.productLongName ?: basic?.productShortName
    val price: Double? get() = prices?.firstOrNull()?.salesPrice
    val volume: Double? get() = basic?.volume
    val category: String? get() = classification?.subProductTypeName
    val kind: String? get() = classification?.productTypeName ?: category
    val alcoholContent: Double? get() = basic?.alcoholContent
    private val isVinmonopolet: Boolean get () = logistics?.wholesalerId != null

    // For å hente bilder bruker vi nettsidene til Vinmonopolet og Meny.
    // Her er det bare å bytte ut en del av URL-en med ID-en til produktet,
    // så får man bildet og kan vise det med Coil.
    val imageURL: String get() {
        basic?.productId?.let { id ->
            if (isVinmonopolet) {
                return "https://bilder.vinmonopolet.no/cache/512x512-0/$id-1.jpg"
            } else {
                return "https://bilder.ngdata.no/$id/meny/large.jpg"
            }
        }
        return "https://bilder.vinmonopolet.no/bottle.png"
    }

    // For å bestemme hvor godt en vare passer med været sammenlikner vi den optimale
    // tempraturen med den faktiske tempraturen. Vi reduserer også matchRaten til
    // Pol-produkter med 5% slik at butikk-produktene skal komme litt mer fram. Dette gjøres
    // fordi butikk-øl er billigere og mer populære for vanlige folk, og de forsvinner fort
    // i den utrolige mengden med Pol-varer.
    fun matchRate(temp: Double?): Double {
        var rate = 0.0
        temp?.let {
            rate = 1 - kotlin.math.abs((it - optimalTemp) / 5)
            if (isVinmonopolet) rate -= 0.05
        }
        return rate
    }

    // For å finne den optimale temperaturen bruker vi ulike data fra Vinmonopolet, i tillegg
    // til produktnavnene og øltypen. Lette, lyse, friske øl passer oftest best om sommeren,
    // og mørke, fyldige øl passer bedre om vinteren. Denne er lazy slik at ikke optimal
    // tempratur endrer seg for hver gang man bytter dag. Pluss at det er mindre å prossessere.
    private val optimalTemp: Double by lazy {
        // Begynner på et sted mellom 17 og 18 grader og trekker ned litt for alkoholprosent.
        var temp = Random.nextDouble(17.0, 18.0) - ((alcoholContent ?: 4.6) / 3)

        // Øker optimal tempratur basert på friskhet, og senker (litt mindre) basert
        // på fyldighet og bitterhet
        temp += (description?.freshness?.toIntOrNull() ?: 0) / 3
        temp -= (description?.fullness?.toIntOrNull() ?: 0) / 5
        temp -= (description?.bitterness?.toIntOrNull() ?: 0) / 5

        // Setter Regex-matching til å ignorere case.
        val option = RegexOption.IGNORE_CASE

        // Vi øker og senker optimal temperatur basert på nøkkelord i tittelen på drikkevarene
        // Sommerlige stikkord øket optimal temperatur, og vinterlige stikkord senker den.
        // Her endrer vi også optimal temperatur basert på produkttypen.
        if (name?.contains(Regex("(lime|mango|juicy|sitron)", option)) == true) temp += 8
        if (name?.contains(Regex("(sommer|skjærgård|anker|båt|hav)", option)) == true) temp += 7
        if (name?.contains(Regex("(corona|desperados|miguel|peroni|blanc)", option)) == true) temp += 5
        if (name?.contains(Regex("(lite|light|lett)", option)) == true) temp += 4
        if (classification?.productTypeName?.contains(Regex("(hvete|weiss)", option)) == true) temp += 3
        if (classification?.productTypeName?.contains(Regex("(pale ale|pilsner)", option)) == true) temp += 3
        if (classification?.productTypeName?.contains(Regex("lys", option)) == true) temp += 2
        if (name?.contains(Regex("(bayer)", option)) == true) temp -= 8
        if (classification?.productTypeName?.contains(Regex("mørk", option)) == true) temp -= 9
        if (classification?.productTypeName?.contains(Regex("sur", option)) == true) temp -= 10
        if (name?.contains(Regex("(jul|snø|vinter|christ|santa|winter|snow|nisse)", option)) == true) temp -= 8
        if (name?.contains(Regex("(guinness)", option)) == true ||
            classification?.productTypeName?.contains(Regex("(stout|draught|brown|dark|porter)", option)) == true
        ) temp -= 20

        temp.round(3)
    }
    // Her er resten av dataklassene som holder på dataene fra API-ene.
    data class Assortment(
        val assortmentId: String? = null,
        val assortment: String? = null,
        val validFrom: String? = null,
        val listedFrom: String? = null,
        val assortmentGrades: List<Any>? = null)

    data class Barcodes(
        val gtin: String? = null,
        val isMainGtin: Boolean? = null,
        val unitOfMeasure: String? = null,
        val packageQuantity: Double? = null)
    
    data class Basic(
        val productId: String? = null,
        val productShortName: String? = null,
        val productLongName: String? = null,
        val volume: Double? = null,
        val alcoholContent: Double? = null,
        val vintage: Int? = null,
        val ageLimit: String? = null,
        val packagingMaterialId: String? = null,
        val packagingMaterial: String? = null,
        val volumTypeId: String? = null,
        val volumType: String? = null,
        val corkTypeId: String? = null,
        val corkType: String? = null,
        val bottlePerSalesUnit: Double? = null,
        val introductionDate: String? = null,
        val productStatusSaleId: String? = null,
        val productStatusSaleName: String? = null,
        val productStatusSaleValidFrom: String? = null)

    data class Characteristics(
        val colour: String? = null,
        val odour: String? = null,
        val taste: String? = null)

    data class Classification(
        val mainProductTypeId: String? = null,
        val mainProductTypeName: String? = null,
        val subProductTypeId: String? = null,
        val subProductTypeName: String? = null,
        val productTypeId: String? = null,
        val productTypeName: String? = null,
        val productGroupId: String? = null,
        val productGroupName: String? = null)

    data class Description(
        val characteristics: Characteristics? = null,
        val freshness: String? = null,
        val fullness: String? = null,
        val bitterness: String? = null,
        val sweetness: String? = null,
        val tannins: String? = null,
        val recommendedFood: List<RecommendedFood>? = null)

    data class Ingredients(
        val grapes: List<Any>? = null,
        val ingredients: String? = null,
        val sugar: String? = null,
        val acid: String? = null,
        val allergens: String? = null)

    data class LastChanged(val date: String? = null, val time: String? = null)

    data class Logistics(
        val wholesalerId: String? = null,
        val wholesalerName: String? = null,
        val vendorId: String? = null,
        val vendorName: String? = null,
        val vendorValidFrom: String? = null,
        val manufacturerId: String? = null,
        val manufacturerName: String? = null,
        val barcodes: List<Barcodes>? = null,
        val orderPack: String? = null,
        val minimumOrderQuantity: Double? = null,
        val packagingWeight: Double? = null)

    data class Origin(
        val countryId: String? = null,
        val country: String? = null,
        val regionId: String? = null,
        val region: String? = null,
        val subRegionId: String? = null,
        val subRegion: String? = null)

    data class Origins(
        val origin: Origin? = null,
        val production: Production? = null,
        val localQualityClassifId: String? = null,
        val localQualityClassif: String? = null)

    data class Price(
        val priceValidFrom: String? = null,
        val salesPrice: Double? = null,
        val salesPricePrLiter: Double? = null,
        val bottleReturnValue: Double? = null)

    data class Production(
        val countryId: String? = null,
        val country: String? = null,
        val regionId: String? = null,
        val region: String? = null)

    data class Properties(
        val ecoLabellingId: String? = null,
        val ecoLabelling: String? = null,
        val storagePotentialId: String? = null,
        val storagePotential: String? = null,
        val organic: Boolean? = null,
        val biodynamic: Boolean? = null,
        val ethicallyCertified: Boolean? = null,
        val vintageControlled: Boolean? = null,
        val sweetWine: Boolean? = null,
        val freeOrLowOnGluten: Boolean? = null,
        val kosher: Boolean? = null,
        val locallyProduced: Boolean? = null,
        val noAddedSulphur: Boolean? = null,
        val environmentallySmart: Boolean? = null,
        val productionMethodStorage: String? = null)

    data class RecommendedFood(val foodId: String? = null, val foodDesc: String? = null)
}