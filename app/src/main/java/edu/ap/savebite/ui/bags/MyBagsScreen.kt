package edu.ap.savebite.ui.bags

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.ap.savebite.R
import edu.ap.savebite.ui.components.SaveBiteButton

/**
 * Mijn pakketten (H5).
 * shortcut: only the "+ Nieuw pakket" button for now; the realtime list of bags follows in the next step.
 */
@Composable
fun MyBagsScreen(onNewBag: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 4.dp)
    ) {
        SaveBiteButton(text = stringResource(R.string.new_bag), onClick = onNewBag)
    }
}
