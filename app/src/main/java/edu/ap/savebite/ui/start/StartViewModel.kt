package edu.ap.savebite.ui.start

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.ap.savebite.R
import edu.ap.savebite.SaveBiteApplication
import edu.ap.savebite.data.AuthRepository
import edu.ap.savebite.data.Role
import edu.ap.savebite.data.StoreRepository
import edu.ap.savebite.ui.SaveBiteScreen
import edu.ap.savebite.ui.authErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StartUiState(
    /** Where the app goes next; null while loading or after an error. */
    val destination: SaveBiteScreen? = null,
    @StringRes val errorMessage: Int? = null
)

/** Decides the first screen: Login, or the start screen of the user's role (A2). */
class StartViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = StartUiState()
        val uid = authRepository.currentUid
        if (uid == null) {
            _uiState.value = StartUiState(destination = SaveBiteScreen.Login)
            return
        }
        viewModelScope.launch {
            try {
                val destination = when (authRepository.getRole(uid)) {
                    Role.CUSTOMER -> SaveBiteScreen.Discover
                    Role.MERCHANT ->
                        if (storeRepository.hasStore(uid)) SaveBiteScreen.MyBags else SaveBiteScreen.MyStore
                }
                _uiState.value = StartUiState(destination = destination)
            } catch (e: Exception) {
                _uiState.value = StartUiState(errorMessage = authErrorMessage(e, fallback = R.string.error_start_failed))
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = StartUiState(destination = SaveBiteScreen.Login)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SaveBiteApplication).container
                StartViewModel(container.authRepository, container.storeRepository)
            }
        }
    }
}
