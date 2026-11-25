package edu.ap.savebite.ui.bag

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Timestamp
import edu.ap.savebite.R
import edu.ap.savebite.SaveBiteApplication
import edu.ap.savebite.data.AuthRepository
import edu.ap.savebite.data.BagRepository
import edu.ap.savebite.data.QuantityBelowReservedException
import edu.ap.savebite.data.StoreRepository
import edu.ap.savebite.data.model.Bag
import edu.ap.savebite.ui.authErrorMessage
import edu.ap.savebite.util.PhotoConverter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Calendar
import java.util.Locale

data class BagFormUiState(
    /** True when an existing bag is edited (H7), false for a new bag. */
    val isEditing: Boolean = false,
    /** Loading the existing bag. */
    val isLoading: Boolean = false,
    /** Online or offline; only used when editing. */
    val active: Boolean = true,
    // Text fields keep what is typed; they are checked when saving
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val originalValue: String = "",
    val quantity: String = "",
    /** Pickup times in minutes after midnight, today. */
    val pickupStart: Int? = null,
    val pickupEnd: Int? = null,
    /** Scaled-down photo for the preview, and the same photo as Base64 for Firestore (H4). */
    val photo: Bitmap? = null,
    val photoBase64: String? = null,
    val isProcessingPhoto: Boolean = false,
    val isSaving: Boolean = false,
    // One error per field, shown below that field
    @StringRes val titleError: Int? = null,
    @StringRes val priceError: Int? = null,
    @StringRes val originalValueError: Int? = null,
    @StringRes val quantityError: Int? = null,
    @StringRes val pickupStartError: Int? = null,
    @StringRes val pickupEndError: Int? = null,
    @StringRes val photoError: Int? = null,
    /** Saving failed. */
    @StringRes val errorMessage: Int? = null,
    /** True after a successful save; the screen then goes back. */
    val isSaved: Boolean = false,
)

/** Nieuw pakket or Pakket aanpassen: the form to offer or edit a surprise bag (H3, H4, H7). */
class BagFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val bagRepository: BagRepository,
    private val photoConverter: PhotoConverter,
) : ViewModel() {
    /** The bag to edit, from the route "BagForm?bagId=…"; null for a new bag. */
    private val bagId: String? = savedStateHandle[BAG_ID]

    /**
     * Id for a new bag, chosen once. Without internet Firestore keeps the write in a queue;
     * when the merchant taps "Pakket aanbieden" again, the same document is written, not a second one.
     */
    private val newBagId: String by lazy { bagRepository.newBagId() }

    private val _uiState = MutableStateFlow(BagFormUiState(isEditing = bagId != null, isLoading = bagId != null))
    val uiState: StateFlow<BagFormUiState> = _uiState.asStateFlow()

    init {
        bagId?.let(::loadBag)
    }

    /** Fills the form with the existing bag (H7). */
    private fun loadBag(id: String) {
        viewModelScope.launch {
            try {
                val bag = bagRepository.getBag(id)
                if (bag == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = R.string.error_bag_load_failed) }
                    return@launch
                }
                val photo = photoConverter.fromBase64(bag.photoBase64)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        active = bag.active,
                        title = bag.title,
                        description = bag.description,
                        price = formatAmount(bag.price),
                        originalValue = formatAmount(bag.originalValue),
                        quantity = bag.quantityTotal.toString(),
                        pickupStart = bag.pickupStart?.let(::minutesOfDay),
                        pickupEnd = bag.pickupEnd?.let(::minutesOfDay),
                        photo = photo,
                        photoBase64 = bag.photoBase64.takeIf { photo != null },
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Loading the bag failed", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = authErrorMessage(e, R.string.error_bag_load_failed))
                }
            }
        }
    }

    /** Takes the bag offline or puts it back online (H7). Goes back to the list when done. */
    fun toggleActive() {
        val id = bagId ?: return
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                withTimeout(SAVE_TIMEOUT_MS) { bagRepository.setActive(id, !state.active) }
                _uiState.update { it.copy(isSaving = false, active = !state.active, isSaved = true) }
            } catch (e: TimeoutCancellationException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = R.string.error_bag_saved_offline) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Changing online/offline failed", e)
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = authErrorMessage(e, R.string.error_bag_save_failed))
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = null) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onPriceChange(value: String) =
        _uiState.update { it.copy(price = value, priceError = null, originalValueError = null) }

    fun onOriginalValueChange(value: String) =
        _uiState.update { it.copy(originalValue = value, originalValueError = null) }

    fun onQuantityChange(value: String) = _uiState.update { it.copy(quantity = value, quantityError = null) }

    fun onPickupStartChange(minutes: Int) =
        _uiState.update { it.copy(pickupStart = minutes, pickupStartError = null, pickupEndError = null) }

    fun onPickupEndChange(minutes: Int) = _uiState.update { it.copy(pickupEnd = minutes, pickupEndError = null) }

    /** A photo from the gallery (PickVisualMedia). */
    fun onPhotoPicked(uri: Uri) = processPhoto { photoConverter.load(uri) }

    /** A photo from the camera (TakePicturePreview). */
    fun onPhotoTaken(bitmap: Bitmap) = processPhoto { photoConverter.prepare(bitmap) }

    private fun processPhoto(load: suspend () -> Bitmap) {
        _uiState.update { it.copy(isProcessingPhoto = true, photoError = null) }
        viewModelScope.launch {
            try {
                val photo = load()
                val base64 = photoConverter.toBase64(photo)
                _uiState.update { it.copy(photo = photo, photoBase64 = base64, isProcessingPhoto = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Reading the photo failed", e)
                _uiState.update { it.copy(isProcessingPhoto = false, photoError = R.string.error_bag_photo_failed) }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isProcessingPhoto || state.isLoading) return
        val uid = authRepository.currentUid ?: return

        // Accept both 3,50 and 3.50
        val price = state.price.replace(',', '.').trim().toDoubleOrNull()
        val originalValue = state.originalValue.replace(',', '.').trim().toDoubleOrNull()
        val quantity = state.quantity.trim().toIntOrNull()
        val start = state.pickupStart
        val end = state.pickupEnd

        val titleError = if (state.title.isBlank()) R.string.error_bag_title_empty else null
        val priceError = if (price == null || price <= 0) R.string.error_bag_price_invalid else null
        val originalValueError = when {
            originalValue == null -> R.string.error_bag_original_invalid
            price != null && originalValue <= price -> R.string.error_bag_original_too_low
            else -> null
        }
        val quantityError = if (quantity == null || quantity < 1) R.string.error_bag_quantity_invalid else null
        val pickupStartError = if (start == null) R.string.error_bag_pickup_start_empty else null
        val pickupEndError = when {
            end == null -> R.string.error_bag_pickup_end_empty
            start != null && end <= start -> R.string.error_bag_pickup_end_before_start
            // A bag that can no longer be picked up today makes no sense
            todayAt(end).toDate().time <= System.currentTimeMillis() -> R.string.error_bag_pickup_past
            else -> null
        }
        val photoError = if (state.photoBase64 == null) R.string.error_bag_photo_empty else null

        val errors = listOf(
            titleError, priceError, originalValueError, quantityError, pickupStartError, pickupEndError, photoError
        )
        if (errors.any { it != null }) {
            _uiState.update {
                it.copy(
                    titleError = titleError, priceError = priceError, originalValueError = originalValueError,
                    quantityError = quantityError, pickupStartError = pickupStartError,
                    pickupEndError = pickupEndError, photoError = photoError, errorMessage = null
                )
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        if (bagId != null) {
            update(bagId, state, price!!, originalValue!!, quantity!!, start!!, end!!)
            return
        }

        viewModelScope.launch {
            try {
                val store = storeRepository.getStore(uid)
                if (store == null) {
                    _uiState.update { it.copy(isSaving = false, errorMessage = R.string.error_bag_no_store) }
                    return@launch
                }
                val bag = Bag(
                    storeId = uid,
                    storeName = store.name,
                    category = store.category,
                    location = store.location,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    price = price!!,
                    originalValue = originalValue!!,
                    quantityTotal = quantity!!,
                    quantityLeft = quantity,
                    pickupStart = todayAt(start!!),
                    pickupEnd = todayAt(end!!),
                    active = true,
                    photoBase64 = state.photoBase64!!,
                )
                // Offline, Firestore keeps the write and never answers: do not wait forever
                withTimeout(SAVE_TIMEOUT_MS) { bagRepository.createBag(newBagId, bag) }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: TimeoutCancellationException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = R.string.error_bag_saved_offline) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Saving the bag failed", e)
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = authErrorMessage(e, R.string.error_bag_save_failed))
                }
            }
        }
    }

    /** Saves the changes to an existing bag (H7). */
    private fun update(
        id: String, state: BagFormUiState, price: Double, originalValue: Double, quantity: Int, start: Int, end: Int
    ) {
        viewModelScope.launch {
            try {
                bagRepository.updateBag(
                    bagId = id,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    price = price,
                    originalValue = originalValue,
                    quantityTotal = quantity,
                    pickupStart = todayAt(start),
                    pickupEnd = todayAt(end),
                    photoBase64 = state.photoBase64!!,
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Firestore may wrap the exception thrown inside the transaction
                val belowReserved = e as? QuantityBelowReservedException
                    ?: e.cause as? QuantityBelowReservedException
                if (belowReserved != null) {
                    _uiState.update {
                        it.copy(isSaving = false, quantityError = R.string.error_bag_quantity_below_reserved)
                    }
                } else {
                    Log.w(TAG, "Updating the bag failed", e)
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = authErrorMessage(e, R.string.error_bag_save_failed))
                    }
                }
            }
        }
    }

    private fun minutesOfDay(timestamp: Timestamp): Int {
        val calendar = Calendar.getInstance().apply { time = timestamp.toDate() }
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    /** 3.5 → "3,50", as a Belgian merchant types it. */
    private fun formatAmount(amount: Double) = String.format(Locale.forLanguageTag("nl-BE"), "%.2f", amount)

    /** Today at the given time (H3: the pickup window is always today). */
    private fun todayAt(minutes: Int): Timestamp {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }

    companion object {
        private const val TAG = "BagFormViewModel"
        private const val SAVE_TIMEOUT_MS = 10_000L

        /** Name of the route argument, see SaveBiteApp. */
        const val BAG_ID = "bagId"

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SaveBiteApplication).container
                BagFormViewModel(
                    createSavedStateHandle(),
                    container.authRepository,
                    container.storeRepository,
                    container.bagRepository,
                    container.photoConverter
                )
            }
        }
    }
}
