package edu.ap.savebite.data.remote

import com.google.gson.annotations.SerializedName

/** One result of Nominatim `/search` (the same as `Entry` in lab OSM). */
data class SearchResult(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("display_name") val displayName: String? = null,
)
