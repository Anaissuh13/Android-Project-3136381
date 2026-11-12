package ie.griffith.digitalatlas.network

import ie.griffith.digitalatlas.data.model.WikiSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

//gets the short summary of a wikipedia article using wikipedia's free REST api
//Also helps separate "no article" (normal) from "no internet" (an error)
object WikipediaClient {

    private const val SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/"

    //returns null when there's no usable article, throws when the request itself fails
    suspend fun getSummary(title: String): WikiSummary? = withContext(Dispatchers.IO) {
        //wikipedia titles use underscores instead of spaces, then encoding anything else like accents
        val encodedTitle = URLEncoder.encode(title.replace(' ', '_'), "UTF-8")

        val body = try {
            HttpHelper.get(SUMMARY_URL + encodedTitle)
        } catch (e: HttpStatusException) {
            //404 just means there's no article with that name, which is normal for small places
            if (e.code == 404) return@withContext null else throw e
        }

        val json = JSONObject(body)
        //disambiguation pages are only lists of other articles (e.g. "Springfield"), not useful as facts
        if (json.optString("type") == "disambiguation") return@withContext null
        val extract = json.optString("extract")
        if (extract.isBlank()) return@withContext null

        WikiSummary(
            title = json.optString("title", title),
            description = json.optString("description").ifBlank { null },
            extract = extract,
            thumbnailUrl = json.optJSONObject("thumbnail")?.optString("source")?.ifBlank { null },
            //mobile link since this is a phone app, building one ourselves if it's missing
            pageUrl = json.optJSONObject("content_urls")
                ?.optJSONObject("mobile")
                ?.optString("page")
                ?.ifBlank { null }
                ?: "https://en.m.wikipedia.org/wiki/$encodedTitle"
        )
    }
}