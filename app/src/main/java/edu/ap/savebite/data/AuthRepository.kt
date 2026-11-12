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
    /** The uid of the signed-in user, or null when nobody is signed in. */
    val currentUid: String?
        get() = auth.currentUser?.uid

    /** Creates the Auth account and the profile document `users/{uid}`. */
    suspend fun register(name: String, email: String, password: String, role: Role) {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user!!
        try {
            firestore.collection("users").document(user.uid)
                .set(mapOf("name" to name, "email" to email, "role" to role.value))
                .await()
        } catch (e: Exception) {
            // Without a profile the account has no role, so remove it and let the user retry
            try {
                user.delete().await()
            } catch (_: Exception) {
                // Delete failed too: at least do not stay signed in without a role
                auth.signOut()
            }
            throw e
        }
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() = auth.signOut()

    suspend fun sendPasswordReset(email: String) {
        // Firebase uses its Dutch template for the mail
        auth.setLanguageCode("nl")
        auth.sendPasswordResetEmail(email).await()
    }

    /** Reads the role from `users/{uid}`. */
    suspend fun getRole(uid: String): Role {
        val value = firestore.collection("users").document(uid).get().await().getString("role")
        return Role.entries.first { it.value == value }
    }
}
