package edu.ap.savebite.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** The role of a user, stored as `value` in Firestore. */
enum class Role(val value: String) {
    CUSTOMER("customer"),
    MERCHANT("merchant")
}

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    /** Creates the Auth account and the profile document `users/{uid}`. */
    suspend fun register(name: String, email: String, password: String, role: Role) {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user!!
        try {
            firestore.collection("users").document(user.uid)
                .set(mapOf("name" to name, "email" to email, "role" to role.value))
                .await()
        } catch (e: Exception) {
            // Without a profile the account has no role, so remove it and let the user retry
            user.delete().await()
            throw e
        }
    }
}
