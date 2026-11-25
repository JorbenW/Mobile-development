package edu.ap.savebite.ui.store

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.GeoPoint
import edu.ap.savebite.R
import edu.ap.savebite.SaveBiteApplication
import edu.ap.savebite.data.AuthRepository
import edu.ap.savebite.data.GeocodingRepository
import edu.ap.savebite.data.StoreRepository
import edu.ap.savebite.data.model.Store
import edu.ap.savebite.data.model.StoreCategory
import edu.ap.savebite.ui.authErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

data class MyStoreUiState(
    val name: String = "",
    val category: StoreCategory = StoreCategory.BAKER,
    /** Position of the pin, null until the merchant taps the map. */
    val lat: Double? = null,
    val lon: Double? = null,
    /** Street + house number */
    val address: String = "",
    val postalCode: String = "",
    val city: String = "",
    /** Loading the existing store when the screen opens. */
    val isLoading: Boolean = true,
    /** Waiting for Nominatim after the pin moved (H2). */
    val isLoadingAddress: Boolean = false,
    val isSaving: Boolean = false,
    // One error per field, shown below that field
    @StringRes val nameError: Int? = null,
    @StringRes val pinError: Int? = null,
    @StringRes val addressError: Int? = null,
    @StringRes val cityError: Int? = null,
    /** Reverse geocoding failed: the merchant fills in the address himself. */
    @StringRes val addressMessage: Int? = null,
    /** Loading or saving the store failed. */
    @StringRes val errorMessage: Int? = null,
    /** True after a successful save; the screen then navigates. */
    val isSaved: Boolean = false,
)

/** Mijn winkel: name, category and location of the store (H1, H2). */
class MyStoreViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val geocodingRepository: GeocodingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyStoreUiState())
    val uiState: StateFlow<MyStoreUiState> = _uiState.asStateFlow()

    /** The running address lookup, cancelled when the pin moves again. */
    private var addressJob: Job? = null

    init {
        loadStore()
    }

    private fun loadStore() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            try {
                val store = storeRepository.getStore(uid)
                _uiState.update { state ->
                    if (store == null) {
                        state.copy(isLoading = false)
                    } else {
                        state.copy(
                            name = store.name,
                            category = StoreCategory.fromValue(store.category),
                            lat = store.location?.latitude,
                            lon = store.location?.longitude,
                            address = store.address,
                            postalCode = store.postalCode,
                            city = store.city,
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                // The merchant can still fill in the form and try to save
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = authErrorMessage(e, R.string.error_store_load_failed))
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, nameError = null) }

    fun onCategoryChange(category: StoreCategory) = _uiState.update { it.copy(category = category) }

    fun onAddressChange(address: String) = _uiState.update { it.copy(address = address, addressError = null) }

    fun onPostalCodeChange(postalCode: String) = _uiState.update { it.copy(postalCode = postalCode) }

    fun onCityChange(city: String) = _uiState.update { it.copy(city = city, cityError = null) }

    /** The pin was placed or released: look up the address (H2). */
    fun onPinMoved(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(
                lat = lat, lon = lon, pinError = null,
                isLoadingAddress = true, addressMessage = null
            )
        }
        // Only the last position counts
        addressJob?.cancel()
        addressJob = viewModelScope.launch {
            try {
                val found = geocodingRepository.reverse(lat, lon)
                _uiState.update { state ->
                    if (found == null) {
                        state.copy(isLoadingAddress = false, addressMessage = R.string.address_not_found)
                    } else {
                        state.copy(
                            address = listOfNotNull(found.road, found.houseNumber).joinToString(" "),
                            postalCode = found.postcode.orEmpty(),
                            city = found.city.orEmpty(),
                            addressError = null,
                            cityError = null,
                            isLoadingAddress = false,
                            // No street at this place (e.g. in the middle of a square)
                            addressMessage = if (found.road == null) R.string.address_no_street else null,
                        )
                    }
                }
            } catch (e: Exception) {
                // Cancelled because the pin moved again: the new lookup takes over
                if (e is CancellationException) throw e
                val message = when (e) {
                    is HttpException -> R.string.error_address_server
                    is IOException -> R.string.error_address_no_internet
                    else -> R.string.error_address_failed
                }
                _uiState.update { it.copy(isLoadingAddress = false, addressMessage = message) }
            }
        }
    }

    /** The merchant typed the address himself: put the pin there (the other way around than [onPinMoved]). */
    fun onSearchAddress() {
        val state = _uiState.value
        if (state.address.isBlank() || state.city.isBlank()) {
            _uiState.update {
                it.copy(
                    addressError = if (state.address.isBlank()) R.string.error_store_address_empty else null,
                    cityError = if (state.city.isBlank()) R.string.error_store_city_empty else null,
                )
            }
            return
        }
        val query = "${state.address.trim()}, ${state.postalCode.trim()} ${state.city.trim()}"

        _uiState.update { it.copy(isLoadingAddress = true, addressMessage = null) }
        addressJob?.cancel()
        addressJob = viewModelScope.launch {
            try {
                val found = geocodingRepository.search(query)
                _uiState.update {
                    if (found == null) {
                        it.copy(isLoadingAddress = false, addressMessage = R.string.address_search_not_found)
                    } else {
                        // Only the pin moves; the address stays as the merchant typed it
                        it.copy(lat = found.lat, lon = found.lon, pinError = null, isLoadingAddress = false)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val message = when (e) {
                    is HttpException -> R.string.error_address_server
                    is IOException -> R.string.error_address_search_no_internet
                    else -> R.string.error_address_search_failed
                }
                _uiState.update { it.copy(isLoadingAddress = false, addressMessage = message) }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val uid = authRepository.currentUid ?: return

        val nameError = if (state.name.isBlank()) R.string.error_store_name_empty else null
        val pinError = if (state.lat == null || state.lon == null) R.string.error_store_pin else null
        val addressError = if (state.address.isBlank()) R.string.error_store_address_empty else null
        val cityError = if (state.city.isBlank()) R.string.error_store_city_empty else null
        if (listOf(nameError, pinError, addressError, cityError).any { it != null }) {
            _uiState.update {
                it.copy(nameError = nameError, pinError = pinError, addressError = addressError, cityError = cityError)
            }
            return
        }

        val store = Store(
            ownerId = uid,
            name = state.name.trim(),
            category = state.category.value,
            location = GeoPoint(state.lat!!, state.lon!!),
            address = state.address.trim(),
            postalCode = state.postalCode.trim(),
            city = state.city.trim(),
        )
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Offline, Firestore keeps the write and never answers: do not wait forever
                withTimeout(SAVE_TIMEOUT_MS) { storeRepository.saveStore(store) }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: TimeoutCancellationException) {
                // Firestore sends the queued write as soon as there is a connection again
                _uiState.update { it.copy(isSaving = false, errorMessage = R.string.error_store_saved_offline) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Saving the store failed", e)
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = authErrorMessage(e, R.string.error_store_save_failed))
                }
            }
        }
    }

    companion object {
        private const val TAG = "MyStoreViewModel"
        private const val SAVE_TIMEOUT_MS = 10_000L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SaveBiteApplication).container
                MyStoreViewModel(container.authRepository, container.storeRepository, container.geocodingRepository)
            }
        }
    }
}
