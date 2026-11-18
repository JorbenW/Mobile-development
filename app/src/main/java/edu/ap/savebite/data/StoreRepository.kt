package edu.ap.savebite.data

import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.savebite.data.model.Store
import kotlinx.coroutines.tasks.await

class StoreRepository(private val firestore: FirebaseFirestore) {
    private val stores = firestore.collection("stores")

    /** A store lives at `stores/{uid}` of its merchant (ADR 0004). */
    suspend fun hasStore(uid: String): Boolean =
        stores.document(uid).get().await().exists()

    /** The store of this merchant, or null when there is none yet. */
    suspend fun getStore(uid: String): Store? =
        stores.document(uid).get().await().toObject(Store::class.java)

    /**
     * Saves the store and updates the copy of its name, category and location
     * in all bags of this store, in one batch so they never differ.
     */
    suspend fun saveStore(store: Store) {
        val bags = firestore.collection("bags")
            .whereEqualTo("storeId", store.ownerId)
            .get().await()

        val batch = firestore.batch()
        batch.set(stores.document(store.ownerId), store)
        bags.documents.forEach { bag ->
            batch.update(
                bag.reference,
                mapOf(
                    "storeName" to store.name,
                    "category" to store.category,
                    "location" to store.location
                )
            )
        }
        batch.commit().await()
    }
}
