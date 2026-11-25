package edu.ap.savebite.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** One Retrofit instance for the whole app (lab OSM). */
object RetrofitClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val nominatim: NominatimService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimService::class.java)
    }
}
