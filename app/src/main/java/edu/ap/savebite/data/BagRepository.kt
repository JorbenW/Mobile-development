package edu.ap.savebite.data

import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.savebite.data.model.Bag
import kotlinx.coroutines.tasks.await

class BagRepository(firestore: FirebaseFirestore) {
    private val bags = firestore.collection("bags")

    /** Creates a new bag with an id chosen by Firestore. */
    suspend fun createBag(bag: Bag) {
        bags.document().set(bag).await()
    }
}
