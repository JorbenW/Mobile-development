package edu.ap.savebite.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import edu.ap.savebite.R

/**
 * Opens the settings of this app, where a permission that was refused for good can still be allowed
 * (link "Intents" in the course material).
 */
fun Context.openAppSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
}

/** An explanation with one button below it, e.g. "Locatie toestaan". */
@Composable
fun PermissionMessage(text: String, buttonText: String, onClick: () -> Unit) {
    InfoMessage(text)
    SaveBiteSecondaryButton(text = buttonText, onClick = onClick)
}

/**
 * What to show after a permission was refused (lab Permissions, plus the case it was missing):
 * - refused once: explain why and offer to ask again;
 * - refused for good: Android no longer asks, so only the app settings can still allow it.
 *
 * @param shouldShowRationale from Accompanist; true after one refusal, false after a refusal for good
 */
@Composable
fun PermissionRefused(
    shouldShowRationale: Boolean,
    rationale: String,
    askAgainText: String,
    onAskAgain: () -> Unit,
    deniedForever: String,
) {
    if (shouldShowRationale) {
        PermissionMessage(rationale, askAgainText, onAskAgain)
    } else {
        val context = LocalContext.current
        PermissionMessage(deniedForever, stringResource(R.string.open_settings)) { context.openAppSettings() }
    }
}
