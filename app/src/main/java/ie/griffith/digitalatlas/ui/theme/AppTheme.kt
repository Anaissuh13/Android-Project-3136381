package ie.griffith.digitalatlas.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ie.griffith.digitalatlas.data.SettingsRepository

//wraps DigitalAtlasTheme with the user's saved theme choice,
//every activity uses this so they all switch together when the setting changes
@Composable
fun DigitalAtlasAppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context.applicationContext) }
    //collectAsStateWithLifecycle stops listening while the screen is in the background, saving battery.
    //SYSTEM is only used for the split second before the saved value loads
    val themeMode by repository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    DigitalAtlasTheme(themeMode = themeMode, content = content)
}