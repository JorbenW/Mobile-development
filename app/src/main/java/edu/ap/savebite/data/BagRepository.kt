package edu.ap.savebite.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import edu.ap.savebite.data.model.Bag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** Thrown when the merchant lowers the quantity below what customers already reserved. */
class QuantityBelowReservedException(val reserved: Int) : Exception()

class BagRepository(private val firestore: FirebaseFirestore) {
    private val bags = firestore.collection("bags")

    /** A new, unused document id for a bag. */
    fun newBagId(): String = bags.document().id

    /**
     * Saves a new bag under [bagId]. Saving again with the same id overwrites it,
     * so tapping the button twice (e.g. without internet) never makes two bags.
     */
    suspend fun createBag(bagId: String, bag: Bag) {
        bags.document(bagId).set(bag).await()
    }

    suspend fun getBag(bagId: String): Bag? =
        bags.document(bagId).get().await().toObject(Bag::class.java)

    /**
     * Saves the changes of the merchant (H7). In a transaction, because a customer can
     * reserve at the same moment: the number already reserved must stay reserved.
     */
    suspend fun updateBag(
        bagId: String,
        title: String,
        description: String,
        price: Double,
        originalValue: Double,
        quantityTotal: Int,
        pickupStart: Timestamp,
        pickupEnd: Timestamp,
        photoBase64: String,
    ) {
        val ref = bags.document(bagId)
        firestore.runTransaction { transaction ->
            val current = transaction.get(ref)
            val oldTotal = current.getLong("quantityTotal")?.toInt() ?: 0
            val oldLeft = current.getLong("quantityLeft")?.toInt() ?: 0
            val reserved = oldTotal - oldLeft
            if (quantityTotal < reserved) throw QuantityBelowReservedException(reserved)
            transaction.update(
                ref,
                mapOf(
                    "title" to title,
                    "description" to description,
                    "price" to price,
                    "originalValue" to originalValue,
                    "quantityTotal" to quantityTotal,
                    // What is reserved stays reserved, the rest is available again
                    "quantityLeft" to quantityTotal - reserved,
                    "pickupStart" to pickupStart,
                    "pickupEnd" to pickupEnd,
                    "photoBase64" to photoBase64,
                )
            )
        }.await()
    }

    /** Takes a bag offline or puts it back online (H7). */
    suspend fun setActive(bagId: String, active: Boolean) {
        bags.document(bagId).update("active", active).await()
    }

    /**
     * All bags of one store, realtime (H5): a new list every time a bag changes,
     * e.g. when a customer reserves one.
     */
    fun observeMyBags(storeId: String): Flow<List<Bag>> =
        bags.whereEqualTo("storeId", storeId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Bag::class.java) }
}
