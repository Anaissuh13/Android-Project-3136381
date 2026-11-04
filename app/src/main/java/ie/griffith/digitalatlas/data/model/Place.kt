package ie.griffith.digitalatlas.data.model

//a place the user tapped on, built from nominatim's reverse geocoding result
data class Place(
    val name: String,
    //e.g. "County Galway, Ireland", shown under the name on the preview card
    val subtitle: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    //null when osm doesn't have it, which is common for villages
    val population: Long?,
    //english wikipedia article title if osm links one, used for the facts tab tomorrow
    val wikipediaTitle: String?
)