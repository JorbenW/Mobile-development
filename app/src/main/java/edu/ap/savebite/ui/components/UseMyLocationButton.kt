package edu.ap.savebite.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import edu.ap.savebite.R

/** Where the location permission flow is. Only screen state, so it lives here. */
private enum class LocationStep {
    /** Nothing shown yet. */
    IDLE,
    /** The explanation is shown; the system dialog comes only after a tap on "Locatie toestaan". */
    EXPLAIN,
    /** The system dialog is open. */
    WAITING,
    /** The merchant answered; show what fits the answer. */
    ANSWERED,
}

/**
 * Button "Gebruik mijn huidige locatie" with the whole permission flow (lab Permissions):
 * FINE and COARSE together, asked only after an explanation and a tap (assignment, Permissies),
 * and every case handled: precise allowed, only approximate, refused, refused for good.
 *
 * @param onLocationAllowed called when at least the approximate location may be used;
 *   the ViewModel then asks LocationRepository for the position
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UseMyLocationButton(onLocationAllowed: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    var step by rememberSaveable { mutableStateOf(LocationStep.IDLE) }

    // Since Android 12, FINE must be asked together with COARSE (lab Permissions)
    val permissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    ) { results ->
        step = LocationStep.ANSWERED
        if (results.values.any { it }) onLocationAllowed()
    }
    val fineGranted = permissions.permissions.first { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
        .status.isGranted
    val coarseGranted = permissions.permissions.first { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }
        .status.isGranted

    fun askSystem() {
        step = LocationStep.WAITING
        permissions.launchMultiplePermissionRequest()
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SaveBiteSecondaryButton(
            text = stringResource(R.string.use_my_location),
            onClick = {
                if (fineGranted || coarseGranted) {
                    step = LocationStep.ANSWERED
                    onLocationAllowed()
                } else {
                    // First explain why, the system dialog only comes after the next tap
                    step = LocationStep.EXPLAIN
                }
            },
            enabled = enabled
        )

        when {
            step == LocationStep.EXPLAIN -> PermissionMessage(
                text = stringResource(R.string.location_permission_explain),
                buttonText = stringResource(R.string.location_permission_allow),
                onClick = ::askSystem
            )
            step != LocationStep.ANSWERED || fineGranted -> Unit
            // Only approximate: the pin can be off; offer to allow the precise location
            coarseGranted -> PermissionMessage(
                text = stringResource(R.string.location_approximate_only),
                buttonText = stringResource(R.string.location_allow_precise),
                onClick = ::askSystem
            )
            else -> PermissionRefused(
                shouldShowRationale = permissions.shouldShowRationale,
                rationale = stringResource(R.string.location_permission_rationale),
                askAgainText = stringResource(R.string.location_permission_ask_again),
                onAskAgain = ::askSystem,
                deniedForever = stringResource(R.string.location_permission_denied_forever),
            )
        }
    }
}
