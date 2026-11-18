package edu.ap.savebite.data

import edu.ap.savebite.data.local.CachedAddress
import edu.ap.savebite.data.local.CachedAddressDao
import edu.ap.savebite.data.remote.NominatimService
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/**
 * Turns coordinates into an address: first from the Room cache, otherwise from Nominatim.
 * Network errors (IOException, HttpException) go up to the ViewModel, like in lab OSM.
 */
class GeocodingRepository(
    private val nominatim: NominatimService,
    private val cache: CachedAddressDao,
) {
    // Nominatim allows at most 1 request per second
    private val mutex = Mutex()
    private var lastRequestAt = 0L

    /** Returns null when Nominatim finds no address at this place. */
    suspend fun reverse(lat: Double, lon: Double): CachedAddress? {
        val key = cacheKey(lat, lon)
        cache.get(key)?.let { return it }

        val result = mutex.withLock {
            val wait = lastRequestAt + MIN_INTERVAL_MS - System.currentTimeMillis()
            if (wait > 0) delay(wait)
            try {
                nominatim.reverse(lat, lon)
            } finally {
                lastRequestAt = System.currentTimeMillis()
            }
        }
        if (result.error != null || result.displayName == null) return null

        val address = CachedAddress(
            key = key,
            displayName = result.displayName,
            road = result.address?.street,
            houseNumber = result.address?.houseNumber,
            postcode = result.address?.postcode,
            city = result.address?.municipality,
            fetchedAt = System.currentTimeMillis(),
        )
        cache.insert(address)
        return address
    }

    private companion object {
        const val MIN_INTERVAL_MS = 1_000L

        // Locale.US: a Belgian device would otherwise write a comma as decimal separator
        fun cacheKey(lat: Double, lon: Double) = String.format(Locale.US, "%.4f,%.4f", lat, lon)
    }
}
