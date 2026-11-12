package ie.griffith.digitalatlas.network

import ie.griffith.digitalatlas.data.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

//talks to nominatim (openstreetmap's geocoder) to find out what place is at a point on the map using its latitude and longitude. Reverse geocoding
object NominatimClient {

    private const val BASE_URL = "https://nominatim.openstreetmap.org"

    //returns null when there's nothing at that point (like tapping the ocean),
    //and throws if the request itself fails so the screen can show a connection error instead
    suspend fun reverseGeocode(lat: Double, lon: Double): Place? = withContext(Dispatchers.IO) {
        //zoom=10 asks for city/town level instead of the exact street address,
        //accept-language=en so names come back in english even though the map labels are local
        val url = "$BASE_URL/reverse?format=jsonv2&lat=$lat&lon=$lon" +
                "&zoom=10&addressdetails=1&extratags=1&accept-language=en"

        val json = JSONObject(HttpHelper.get(url))
        if (json.has("error")) null else parsePlace(json, lat, lon)
    }

    private fun parsePlace(json: JSONObject, tappedLat: Double, tappedLon: Double): Place {
        val address = json.optJSONObject("address") ?: JSONObject()
        val extraTags = json.optJSONObject("extratags") ?: JSONObject()

        //name is sometimes empty in rural areas, so falling back through the address levels
        val name = json.optString("name").ifBlank {
            listOf("city", "town", "village", "hamlet", "county", "state", "country")
                .firstNotNullOfOrNull { key -> address.optString(key).ifBlank { null } }
                ?: "Unknown place"
        }
        val country = address.optString("country")

        //building "County Galway, Ireland", skipping anything that just repeats the name
        val region = address.optString("county").ifBlank { address.optString("state") }
        val subtitle = listOf(region, country)
            .filter { it.isNotBlank() && it != name }
            .distinct()
            .joinToString(", ")

        //osm stores the article as "en:Galway", only keeping english ones for now
        val wikiTag = extraTags.optString("wikipedia")
        val wikipediaTitle = if (wikiTag.startsWith("en:")) wikiTag.removePrefix("en:") else null

        return Place(
            name = name,
            subtitle = subtitle,
            country = country,
            //nominatim sends coordinates as strings, falling back to the tapped point if they're missing
            latitude = json.optString("lat").toDoubleOrNull() ?: tappedLat,
            longitude = json.optString("lon").toDoubleOrNull() ?: tappedLon,
            population = extraTags.optString("population").toLongOrNull(),
            wikipediaTitle = wikipediaTitle
        )
    }
}