package ie.griffith.digitalatlas.network

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

//one shared GET helper for nominatim, photon and wikipedia,
//so the headers and timeouts are only set up in one place
object HttpHelper {

    //nominatim and wikipedia both block requests that don't identify the app
    private const val USER_AGENT = "DigitalAtlas/1.0 (ie.griffith.digitalatlas; student project)"

    //this is a blocking call, so it must only be called from a background thread (Dispatchers.IO)
    fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            //giving up after 10 seconds instead of hanging forever on a bad connection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode} from $url")
            }
            //use{} closes the stream automatically once it's read
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}