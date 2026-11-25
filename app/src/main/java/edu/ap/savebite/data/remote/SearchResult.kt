package edu.ap.savebite.data.remote

import com.google.gson.annotations.SerializedName

/** One result of Nominatim `/search` (the same as `Entry` in lab OSM, plus the address parts). */
data class SearchResult(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("display_name") val displayName: String? = null,
    /** Filled in thanks to `addressdetails=1`; the same object as in [ReverseResult]. */
    @SerializedName("address") val address: Address? = null,
)
