package ie.griffith.digitalatlas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ie.griffith.digitalatlas.ui.main.MainScreen
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasTheme
import org.osmdroid.config.Configuration
import java.io.File

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
                    onSettingsClick = {
                        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}