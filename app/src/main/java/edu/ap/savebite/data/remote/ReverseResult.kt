package edu.ap.savebite.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Answer of Nominatim `/reverse`. When nothing is found, only [error] is filled in.
 * All fields are nullable: Nominatim leaves out what it does not know.
 */
data class ReverseResult(
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("error") val error: String? = null,
)

data class Address(
    @SerializedName("road") val road: String? = null,
    @SerializedName("house_number") val houseNumber: String? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("town") val town: String? = null,
    @SerializedName("village") val village: String? = null,
) {
    /** The municipality: depending on the place, Nominatim calls it city, town or village. */
    val municipality: String? get() = city ?: town ?: village
}
