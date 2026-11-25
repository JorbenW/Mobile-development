package edu.ap.savebite.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Result of reverse geocoding, kept so Nominatim is asked only once per place.
 * [key] is the latitude and longitude rounded to 4 decimals (about 10 m).
 */
@Entity
data class CachedAddress(
    @PrimaryKey val key: String,
    val displayName: String,
    val road: String?,
    val houseNumber: String?,
    val postcode: String?,
    val city: String?,
    val fetchedAt: Long,
)
