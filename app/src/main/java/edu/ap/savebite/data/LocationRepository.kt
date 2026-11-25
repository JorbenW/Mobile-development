package edu.ap.savebite.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * The current location of the device, for the ViewModels (analysis §4: FusedLocationProviderClient).
 * Used by Mijn winkel now, and by the customer screens (K1) later.
 */
class LocationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    /** True when the user allowed at least the approximate location. */
    fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Asks the device for a fresh location; falls back on the last known one.
     * Returns null without permission, or when the location of the device is turned off.
     */
    @SuppressLint("MissingPermission") // checked by hasPermission()
    suspend fun currentLocation(): Location? {
        if (!hasPermission()) return null
        val cancellation = CancellationTokenSource()
        return try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token).await()
                ?: client.lastLocation.await()
        } finally {
            cancellation.cancel()
        }
    }
}
