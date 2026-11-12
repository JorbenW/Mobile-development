package edu.ap.savebite.ui.register

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: Role = Role.CUSTOMER,
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    /** Set after a successful registration; the screen then navigates. */
    val registeredRole: Role? = null
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, errorMessage = null) }
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, errorMessage = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, errorMessage = null) }
    fun onRoleChange(role: Role) = _uiState.update { it.copy(role = role) }

    fun register() {
        val state = _uiState.value
        val error = when {
            state.name.isBlank() -> R.string.error_name_empty
            state.email.isBlank() -> R.string.error_email_empty
            // Firebase Auth refuses shorter passwords
            state.password.length < 6 -> R.string.error_password_short
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authRepository.register(state.name.trim(), state.email.trim(), state.password, state.role)
                _uiState.update { it.copy(isLoading = false, registeredRole = state.role) }
            } catch (e: Exception) {
                // shortcut: one generic message, specific messages per error come in step 7 (A3)
                _uiState.update { it.copy(isLoading = false, errorMessage = R.string.error_register_failed) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SaveBiteApplication
                RegisterViewModel(app.container.authRepository)
            }
        }
    }
}
