package ie.griffith.digitalatlas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ie.griffith.digitalatlas.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

//one settings file for the whole app, the delegate makes sure only one datastore instance ever exists
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

//reads and writes the user's settings, every activity goes through this so they all agree on the theme
class SettingsRepository(private val context: Context) {

    private val themeModeKey = stringPreferencesKey("theme_mode")

    //flow so screens update straight away whenever the setting changes
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        //falling back to defaults if the file can't be read instead of crashing the app
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            //saved as the enum's name e.g. "DARK", runCatching in case it's ever something unexpected
            prefs[themeModeKey]
                ?.let { saved -> runCatching { ThemeMode.valueOf(saved) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        }

    //suspend because writing to storage happens off the main thread
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[themeModeKey] = mode.name }
    }
}