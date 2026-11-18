package edu.ap.savebite.data.model

import androidx.annotation.StringRes
import com.google.firebase.firestore.GeoPoint
import edu.ap.savebite.R

/**
 * A store in Firestore at `stores/{ownerId}` (ADR 0004).
 * Default values give the empty constructor that Firestore's toObject() needs.
 */
data class Store(
    val ownerId: String = "",
    val name: String = "",
    /** [StoreCategory.value] */
    val category: String = StoreCategory.BAKER.value,
    // Firestore's GeoPoint, not the one from osmdroid
    val location: GeoPoint? = null,
    /** Street + house number */
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
)

/** Categories of a store. [value] is stored in Firestore, [label] is shown. */
enum class StoreCategory(val value: String, @StringRes val label: Int) {
    BAKER("Bakker", R.string.category_baker),
    SUPERMARKET("Supermarkt", R.string.category_supermarket),
    RESTAURANT("Restaurant", R.string.category_restaurant),
    CATERER("Traiteur", R.string.category_caterer),
    OTHER("Andere", R.string.category_other);

    companion object {
        fun fromValue(value: String) = entries.firstOrNull { it.value == value } ?: OTHER
    }
}
