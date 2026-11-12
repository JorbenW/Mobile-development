package edu.ap.savebite.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Creates the repositories once for the whole app (ADR 0002). */
class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
}
