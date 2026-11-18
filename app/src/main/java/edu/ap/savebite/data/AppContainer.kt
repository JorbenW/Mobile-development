package edu.ap.savebite.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.savebite.data.local.AppDatabase
import edu.ap.savebite.data.remote.RetrofitClient

/** Creates the repositories once for the whole app (ADR 0002). */
class AppContainer(context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(context)

    val authRepository = AuthRepository(auth, firestore)
    val storeRepository = StoreRepository(firestore)
    val geocodingRepository = GeocodingRepository(RetrofitClient.nominatim, database.cachedAddressDao())
}
