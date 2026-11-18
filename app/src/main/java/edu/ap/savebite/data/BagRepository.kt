package edu.ap.savebite.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import edu.ap.savebite.data.model.Bag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BagRepository(firestore: FirebaseFirestore) {
    private val bags = firestore.collection("bags")

    /** Creates a new bag with an id chosen by Firestore. */
    suspend fun createBag(bag: Bag) {
        bags.document().set(bag).await()
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
