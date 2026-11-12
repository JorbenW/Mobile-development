package edu.ap.savebite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.ap.savebite.SaveBiteApplication
import edu.ap.savebite.data.AuthRepository

/** Actions of the shared top bar. */
class AppViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun logout() = authRepository.logout()

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SaveBiteApplication
                AppViewModel(app.container.authRepository)
            }
        }
    }
}
