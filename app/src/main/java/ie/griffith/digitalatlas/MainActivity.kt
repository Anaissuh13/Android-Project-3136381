package ie.griffith.digitalatlas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ie.griffith.digitalatlas.ui.main.MainScreen
import ie.griffith.digitalatlas.ui.theme.DigitalAtlasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //drawing behind the status and navigation bars, scaffold handles the padding
        enableEdgeToEdge()
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