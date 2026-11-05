package ie.griffith.digitalatlas.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.griffith.digitalatlas.data.SettingsRepository
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasTheme
import ie.griffith.digitalatlas.ui.theme.ThemeMode
import kotlinx.coroutines.launch

//third activity, opened from the settings icon in the main screen's top bar
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repository = remember { SettingsRepository(applicationContext) }
            //reading the saved choice so the selector shows what's actually saved
            val themeMode by repository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            //scope for saving, since writing to datastore is a suspend function
            val scope = rememberCoroutineScope()

            DigitalAtlasTheme(themeMode = themeMode) {
                SettingsScreen(
                    themeMode = themeMode,
                    //saving straight away, every open screen is listening and updates itself
                    onThemeModeChange = { mode ->
                        scope.launch { repository.setThemeMode(mode) }
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}