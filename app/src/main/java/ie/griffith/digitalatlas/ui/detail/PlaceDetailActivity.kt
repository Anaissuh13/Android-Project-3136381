package ie.griffith.digitalatlas.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ie.griffith.digitalatlas.data.model.Place
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasTheme

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

        setContent {
            DigitalAtlasTheme {
                PlaceDetailScreen(
                    name = name,
                    subtitle = subtitle,
                    latitude = latitude,
                    longitude = longitude,
                    //finish() closes this activity and goes back to the map
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        //keys for the intent extras, kept as constants so the sender and receiver can't spell them differently
        private const val EXTRA_NAME = "ie.griffith.digitalatlas.extra.NAME"
        private const val EXTRA_SUBTITLE = "ie.griffith.digitalatlas.extra.SUBTITLE"
        private const val EXTRA_LAT = "ie.griffith.digitalatlas.extra.LAT"
        private const val EXTRA_LON = "ie.griffith.digitalatlas.extra.LON"
        //sent now but only read tomorrow when the facts tab loads from wikipedia
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