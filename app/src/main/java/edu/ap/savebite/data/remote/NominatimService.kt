package edu.ap.savebite.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/** Reverse geocoding with Nominatim (based on lab OSM, `search` changed to `reverse`). */
interface NominatimService {
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
}
