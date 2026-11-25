package edu.ap.savebite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.ap.savebite.ui.SaveBiteApp
import edu.ap.savebite.ui.theme.SaveBiteTheme
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid configuration (lab OSM), before the first MapView is created
        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            // The OSM tile servers require a User-Agent that identifies this app.
            userAgentValue = packageName
            // App-specific folders: no storage permissions needed.
            osmdroidBasePath = File(filesDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }

        enableEdgeToEdge()
        setContent {
            SaveBiteTheme {
                SaveBiteApp()
            }
        }
    }
}
