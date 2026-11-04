package ie.griffith.digitalatlas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ie.griffith.digitalatlas.ui.main.MainScreen
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasTheme
import org.osmdroid.config.Configuration
import java.io.File
import ie.griffith.digitalatlas.ui.detail.PlaceDetailActivity
import android.content.Intent
import ie.griffith.digitalatlas.ui.settings.SettingsActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //drawing behind the status and navigation bars, scaffold handles the padding
        enableEdgeToEdge()

        //setting up osmdroid once before any map is created
        val osmConfig = Configuration.getInstance()
        osmConfig.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        //openstreetmap blocks apps that don't identify themselves, using the package name as the user agent
        osmConfig.userAgentValue = packageName
        //keeping downloaded tiles in the app's own cache folder so no storage permission is needed
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(osmConfig.osmdroidBasePath, "tiles")

        setContent {
            DigitalAtlasTheme {
                MainScreen(
                    //showing a toast for now, this becomes an explicit intent to SettingsActivity in the next step
                    //explicit intent to SettingsActivity, no extras needed
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    //explicit intent to the details screen, carrying the tapped place as extras
                    onSeeDetails = { place ->
                        startActivity(PlaceDetailActivity.newIntent(this, place))
                    }
                )
            }
        }
    }
}