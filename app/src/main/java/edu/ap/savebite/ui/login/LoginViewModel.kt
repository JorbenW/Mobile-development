package edu.ap.savebite.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.ap.savebite.R
import edu.ap.savebite.SaveBiteApplication
import edu.ap.savebite.data.AuthRepository
import edu.ap.savebite.ui.authErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    /** E-mail address the reset mail was sent to, shown as confirmation (A4). */
    val resetSentTo: String? = null,
    /** True after a successful login; the screen then navigates. */
    val isLoggedIn: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) =
        _uiState.update { it.copy(email = email, errorMessage = null, resetSentTo = null) }

    fun onPasswordChange(password: String) =
        _uiState.update { it.copy(password = password, errorMessage = null) }

    fun login() {
        val state = _uiState.value
        val error = when {
            state.email.isBlank() -> R.string.error_email_empty
            state.password.isEmpty() -> R.string.error_password_empty
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error, resetSentTo = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, resetSentTo = null) }
        viewModelScope.launch {
            try {
                authRepository.login(state.email.trim(), state.password)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                val message = authErrorMessage(e, fallback = R.string.error_login_failed)
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }

    /** Sends a reset mail to the e-mail in the field (A4). */
    fun forgotPassword() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = R.string.error_email_empty, resetSentTo = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, resetSentTo = null) }
        viewModelScope.launch {
            try {
                authRepository.sendPasswordReset(email)
                _uiState.update { it.copy(isLoading = false, resetSentTo = email) }
            } catch (e: Exception) {
                val message = authErrorMessage(e, fallback = R.string.error_reset_failed)
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SaveBiteApplication
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}
