package edu.ap.savebite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.ap.savebite.ui.SaveBiteApp
import edu.ap.savebite.ui.theme.SaveBiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaveBiteTheme {
                SaveBiteApp()
            }
        }
    }
}
