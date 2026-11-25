package edu.ap.savebite.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/** Geocoding with Nominatim (based on lab OSM). */
interface NominatimService {
    /** Coordinates → address, when the merchant places the pin (H2). */
    // Nominatim requires a User-Agent that identifies the app
    @Headers("User-Agent: edu.ap.savebite/1.0 (Android student project)")
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        // 18 = building (street + number, H2), 10 = municipality (K2)
        @Query("zoom") zoom: Int = 18,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") language: String = "nl",
    ): ReverseResult

    /** Address → coordinates, when the merchant types the address himself (as `getAddress` in lab OSM). */
    @Headers("User-Agent: edu.ap.savebite/1.0 (Android student project)")
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1,
        // Only addresses in Belgium, so "Meir 1" does not end up abroad
        @Query("countrycodes") countryCodes: String = "be",
    ): List<SearchResult>
}
