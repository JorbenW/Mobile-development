package edu.ap.savebite.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StoreRepository(private val firestore: FirebaseFirestore) {
    /** A store lives at `stores/{uid}` of its merchant (ADR 0004). */
    suspend fun hasStore(uid: String): Boolean =
        firestore.collection("stores").document(uid).get().await().exists()
}
