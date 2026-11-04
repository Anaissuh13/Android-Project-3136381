package ie.griffith.digitalatlas.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasTheme
import ie.griffith.digitalatlas.ui.theme.ThemeMode

//third activity, opened from the settings icon in the main screen's top bar
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            //only kept on this screen for now (rememberSaveable survives rotation),
            //saving it with DataStore so the whole app follows it comes next
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }

            //passing the choice into the theme so the screen changes straight away as a preview
            DigitalAtlasTheme(themeMode = themeMode) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it },
                    onBack = { finish() }
                )
            }
        }
    }
}