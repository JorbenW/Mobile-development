package edu.ap.savebite.ui.bags

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.ap.savebite.R
import edu.ap.savebite.SaveBiteApplication
import edu.ap.savebite.data.AuthRepository
import edu.ap.savebite.data.BagRepository
import edu.ap.savebite.data.StoreRepository
import edu.ap.savebite.data.model.Bag
import edu.ap.savebite.ui.authErrorMessage
import edu.ap.savebite.util.PhotoConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Status badge of a bag in the list. */
enum class BagStatus(@StringRes val label: Int) {
    ACTIVE(R.string.status_active),
    SOLD_OUT(R.string.status_sold_out),
    OFFLINE(R.string.status_offline),
    /** The pickup window is over. */
    ENDED(R.string.status_ended)
}

/** One row in Mijn pakketten, ready to show. */
data class BagItem(
    val id: String,
    val title: String,
    val photo: Bitmap?,
    /** e.g. "17:00–18:00" */
    val pickupWindow: String,
    val reserved: Int,
    val left: Int,
    val status: BagStatus,
)

data class MyBagsUiState(
    val bags: List<BagItem> = emptyList(),
    val isLoading: Boolean = true,
    /** Null while unknown. */
    val hasStore: Boolean? = null,
    /** "Nieuw pakket" was tapped without a store: show where to make one. */
    val showNoStoreMessage: Boolean = false,
    @StringRes val errorMessage: Int? = null,
)

/** Mijn pakketten: the bags of this merchant with the number of reservations, realtime (H5). */
class MyBagsViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val bagRepository: BagRepository,
    private val photoConverter: PhotoConverter,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyBagsUiState())
    val uiState: StateFlow<MyBagsUiState> = _uiState.asStateFlow()

    /**
     * Decoded photos per bag, so a new snapshot (e.g. one more reservation)
     * does not decode every photo again. The Base64 text tells if the photo changed.
     */
    private val photoCache = mutableMapOf<String, Pair<String, Bitmap?>>()

    init {
        val uid = authRepository.currentUid
        if (uid != null) {
            checkStore(uid)
            observeBags(uid)
        }
    }

    private fun checkStore(uid: String) {
        viewModelScope.launch {
            try {
                val hasStore = storeRepository.hasStore(uid)
                _uiState.update { it.copy(hasStore = hasStore) }
            } catch (e: Exception) {
                // Unknown: the bag form checks it again when saving
                Log.w(TAG, "Checking the store failed", e)
            }
        }
    }

    private fun observeBags(uid: String) {
        viewModelScope.launch {
            bagRepository.observeMyBags(uid)
                .catch { e ->
                    Log.w(TAG, "Loading the bags failed", e)
                    val message = if (e is Exception) authErrorMessage(e, R.string.error_bags_load_failed)
                    else R.string.error_bags_load_failed
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
                .collect { bags ->
                    val items = bags
                        // Today's bags first
                        .sortedByDescending { it.pickupStart }
                        .map { it.toItem() }
                    _uiState.update { it.copy(bags = items, isLoading = false, errorMessage = null) }
                }
        }
    }

    fun onNewBagWithoutStore() = _uiState.update { it.copy(showNoStoreMessage = true) }

    private suspend fun Bag.toItem(): BagItem {
        val now = Date()
        val status = when {
            !active -> BagStatus.OFFLINE
            pickupEnd != null && pickupEnd.toDate().before(now) -> BagStatus.ENDED
            quantityLeft <= 0 -> BagStatus.SOLD_OUT
            else -> BagStatus.ACTIVE
        }
        return BagItem(
            id = id,
            title = title,
            photo = photoFor(this),
            pickupWindow = listOfNotNull(pickupStart, pickupEnd)
                .joinToString("–") { timeFormat.format(it.toDate()) },
            reserved = quantityTotal - quantityLeft,
            left = quantityLeft,
            status = status,
        )
    }

    /** Decodes on Dispatchers.Default (in PhotoConverter), not in the composable. */
    private suspend fun photoFor(bag: Bag): Bitmap? {
        photoCache[bag.id]?.let { (base64, bitmap) -> if (base64 == bag.photoBase64) return bitmap }
        val bitmap = photoConverter.fromBase64(bag.photoBase64)
        photoCache[bag.id] = bag.photoBase64 to bitmap
        return bitmap
    }

    companion object {
        private const val TAG = "MyBagsViewModel"
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SaveBiteApplication).container
                MyBagsViewModel(
                    container.authRepository,
                    container.storeRepository,
                    container.bagRepository,
                    container.photoConverter
                )
            }
        }
    }
}
