package edu.ap.savebite.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

/**
 * A surprise bag in Firestore at `bags/{bagId}`, with the field names of the assignment.
 * storeName, category and location are copied from the store, so a customer
 * does not have to load the store separately.
 * Default values give the empty constructor that Firestore's toObject() needs.
 */
data class Bag(
    /** The document id; Firestore fills it in when reading and leaves it out when writing. */
    @DocumentId val id: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val category: String = "",
    val location: GeoPoint? = null,
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val originalValue: Double = 0.0,
    val quantityTotal: Int = 0,
    val quantityLeft: Int = 0,
    val pickupStart: Timestamp? = null,
    val pickupEnd: Timestamp? = null,
    /** False when the merchant took the bag offline (H7). */
    val active: Boolean = true,
    /** JPEG of at most 800 px as Base64 text. */
    val photoBase64: String = "",
    /** Filled in by the server on the first save. */
    @ServerTimestamp val createdAt: Timestamp? = null,
)
