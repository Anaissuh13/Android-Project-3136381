package ie.griffith.digitalatlas.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ie.griffith.digitalatlas.data.model.Place
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasAppTheme
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.core.net.toUri
import ie.griffith.digitalatlas.R

//second activity, opened from the "see details" button on the map's preview card
class PlaceDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //reading the place that MainActivity sent over in the intent
        val name = intent.getStringExtra(EXTRA_NAME)
        //closing straight away if it was opened without a place, there'd be nothing to show
        if (name == null) {
            finish()
            return
        }
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
        val latitude = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        val longitude = intent.getDoubleExtra(EXTRA_LON, 0.0)
        //null when openstreetmap didn't link a wikipedia article
        val wikiTitle = intent.getStringExtra(EXTRA_WIKI_TITLE)

        setContent {
            DigitalAtlasAppTheme {
                PlaceDetailScreen(
                    name = name,
                    subtitle = subtitle,
                    latitude = latitude,
                    longitude = longitude,
                    wikiTitle = wikiTitle,
                    onShare = { sharePlace(name, subtitle, latitude, longitude) },
                    onOpenInMaps = { openInMaps(name, latitude, longitude) },
                    onOpenWikipedia = { url -> openUrl(url) },
                    //finish() closes this activity and goes back to the map
                    onBack = { finish() }
                )
            }
        }
    }

    //implicit intent: asking android for any app that can share plain text (messages, email, whatsapp...)
    private fun sharePlace(name: String, subtitle: String, lat: Double, lon: Double) {
        val heading = if (subtitle.isBlank()) name else "$name, $subtitle"
        val text = getString(R.string.share_text, heading, osmLink(lat, lon))
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        //createChooser always shows the share sheet, instead of jumping straight into a remembered default app
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_place)))
    }

    //implicit intent with a geo: link, opens whichever maps app the user has, e.g. for directions
    private fun openInMaps(name: String, lat: Double, lon: Double) {
        //q= drops a labelled pin, Uri.encode deals with spaces and accents in the name
        val geoUri = "geo:$lat,$lon?q=$lat,$lon(${android.net.Uri.encode(name)})".toUri()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        } catch (e: ActivityNotFoundException) {
            //no maps app on the phone, so showing the place on openstreetmap.org in the browser instead
            openUrl(osmLink(lat, lon))
        }
    }

    //implicit intent to open a web link, android picks the browser (or the wikipedia app if installed)
    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    //link to the place on openstreetmap's website, used for sharing and as the maps fallback
    private fun osmLink(lat: Double, lon: Double) =
        "https://www.openstreetmap.org/?mlat=$lat&mlon=$lon#map=13/$lat/$lon"

    companion object {
        //keys for the intent extras, kept as constants so the sender and receiver can't spell them differently
        private const val EXTRA_NAME = "ie.griffith.digitalatlas.extra.NAME"
        private const val EXTRA_SUBTITLE = "ie.griffith.digitalatlas.extra.SUBTITLE"
        private const val EXTRA_LAT = "ie.griffith.digitalatlas.extra.LAT"
        private const val EXTRA_LON = "ie.griffith.digitalatlas.extra.LON"
        //used by the facts tab to load the right wikipedia article
        private const val EXTRA_WIKI_TITLE = "ie.griffith.digitalatlas.extra.WIKI_TITLE"

        //explicit intent, naming PlaceDetailActivity directly as the screen to open,
        //with the place's details packed in as extras
        fun newIntent(context: Context, place: Place): Intent =
            Intent(context, PlaceDetailActivity::class.java).apply {
                putExtra(EXTRA_NAME, place.name)
                putExtra(EXTRA_SUBTITLE, place.subtitle)
                putExtra(EXTRA_LAT, place.latitude)
                putExtra(EXTRA_LON, place.longitude)
                putExtra(EXTRA_WIKI_TITLE, place.wikipediaTitle)
            }
    }
}