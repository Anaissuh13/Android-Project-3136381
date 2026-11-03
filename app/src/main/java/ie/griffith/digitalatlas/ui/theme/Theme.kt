package ie.griffith.digitalatlas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

//the three options the user will pick from in settings
enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightColorScheme = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = OnTealPrimaryLight,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = OnTealContainerLight,
    secondary = SandSecondaryLight,
    onSecondary = OnSandSecondaryLight,
    secondaryContainer = SandContainerLight,
    onSecondaryContainer = OnSandContainerLight,
    tertiary = CoralTertiaryLight,
    onTertiary = OnCoralTertiaryLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = BackgroundLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = OnTealPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = OnTealContainerDark,
    secondary = SandSecondaryDark,
    onSecondary = OnSandSecondaryDark,
    secondaryContainer = SandContainerDark,
    onSecondaryContainer = OnSandContainerDark,
    tertiary = CoralTertiaryDark,
    onTertiary = OnCoralTertiaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = BackgroundDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun DigitalAtlasTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    //working out if dark mode should be on, system just follows the phone setting
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    //removed dynamic colour from the template so the app keeps its own palette
    //instead of taking colours from the user's wallpaper
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}