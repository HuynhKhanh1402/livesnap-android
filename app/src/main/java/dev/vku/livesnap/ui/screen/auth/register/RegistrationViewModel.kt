package dev.vku.livesnap.ui.screen.auth.register

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.repository.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegistrationResult {
    data object Success : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
    data object Idle : RegistrationResult()
}

class RegistrationViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {
    var email by mutableStateOf("")

    var firstName by mutableStateOf("")
        private set

    var lastName by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var userId by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val _registrationResult = MutableStateFlow<RegistrationResult>(RegistrationResult.Idle)
    var registrationResult: StateFlow<RegistrationResult> = _registrationResult

    fun setEmailField(newEmail: String) {
        email = newEmail
    }

    fun setFirstNameField(first: String) {
        firstName = first
    }

    fun setLastNameField(last: String) {
        lastName = last
    }

    fun setPasswordField(newPassword: String) {
        password = newPassword
    }

    fun setUserIdField(newUserId: String) {
        userId = newUserId
    }

    fun register() {
        viewModelScope.launch {
            _registrationResult.value = RegistrationResult.Idle
            isLoading = true
            try {
                val userRegistration = UserRegistrationRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    username = userId
                )
                val response = usersRepository.registerUser(userRegistration)
                if (response.code == 200) {
                    _registrationResult.value = RegistrationResult.Success
                } else {
                    _registrationResult.value = RegistrationResult.Error(response.message)
                }
            } catch (e: Exception) {
                _registrationResult.value = RegistrationResult.Error(e.message ?: "Unknown error")
                Log.e("RegistrationViewModel", "Registration failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetRegistrationResult() {
        _registrationResult.value = RegistrationResult.Idle
    }
}