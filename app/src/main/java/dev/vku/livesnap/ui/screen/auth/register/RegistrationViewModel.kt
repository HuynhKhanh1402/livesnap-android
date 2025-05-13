package dev.vku.livesnap.ui.screen.auth.register

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.repository.AuthRepository
import dev.vku.livesnap.data.repository.FCMRepository
import dev.vku.livesnap.data.repository.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EmailExistResult {
    data object Exist : EmailExistResult()
    data object NotExist : EmailExistResult()
    data class Error(val message: String) : EmailExistResult()
    data object Idle : EmailExistResult()
}

sealed class RegistrationResult {
    data object Success : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
    data object Idle : RegistrationResult()
}

sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
    data object Idle : LoginResult()
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val authRepository: AuthRepository,
    private val fcmRepository: FCMRepository
): ViewModel() {
    var email by mutableStateOf("")

    var isEmailValid by mutableStateOf(true)
        private set

    fun setEmailField(newEmail: String) {
        email = newEmail
        isEmailValid = validateEmail(newEmail)
    }

    var firstName by mutableStateOf("")
        private set

    var lastName by mutableStateOf("")
        private set

    var firstNameError by mutableStateOf<String?>(null)
        private set

    var lastNameError by mutableStateOf<String?>(null)
        private set

    var password by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val _emailExistResult = MutableStateFlow<EmailExistResult>(EmailExistResult.Idle)
    var emailExistResult: StateFlow<EmailExistResult> = _emailExistResult

    private val _registrationResult = MutableStateFlow<RegistrationResult>(RegistrationResult.Idle)
    var registrationResult: StateFlow<RegistrationResult> = _registrationResult

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    var loginResult: StateFlow<LoginResult> = _loginResult

    fun resetState() {
        // Reset all mutable state variables
        email = ""
        isEmailValid = true
        firstName = ""
        lastName = ""
        firstNameError = null
        lastNameError = null
        password = ""
        username = ""
        isLoading = false

        // Reset all state flows
        _emailExistResult.value = EmailExistResult.Idle
        _registrationResult.value = RegistrationResult.Idle
        _loginResult.value = LoginResult.Idle
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

    fun setUsernameField(newUsername: String) {
        username = newUsername
    }

    private fun validateEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return regex.matches(email)
    }

    fun checkEmailIsExists() {
        viewModelScope.launch {
            _emailExistResult.value = EmailExistResult.Idle
            isLoading = true
            try {
                val response = usersRepository.checkEmailExist(email)
                if (response.code == 200) {
                    _emailExistResult.value =
                        if (response.exist) EmailExistResult.Exist else EmailExistResult.NotExist
                } else {
                    _emailExistResult.value = EmailExistResult.Error(response.message)
                }
            } catch (e: Exception) {
                _emailExistResult.value = EmailExistResult.Error(e.message ?: "Unknown error")
                Log.e("RegistrationViewModel", "Email check failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun isNameValid(name: String): Boolean {
        val regex = "^[\\p{L} .'-]{2,50}$".toRegex()
        return name.matches(regex)
    }

    fun resetEmailExistResult() {
        _emailExistResult.value = EmailExistResult.Idle
    }

    fun validateNames(): Boolean {
        var isValid = true

        if (!isNameValid(firstName)) {
            firstNameError = "Tên không hợp lệ"
            isValid = false
        } else {
            firstNameError = null
        }

        if (!isNameValid(lastName)) {
            lastNameError = "Họ không hợp lệ"
            isValid = false
        } else {
            lastNameError = null
        }

        return isValid
    }

    fun isValidUsername(): Boolean {
        val regex = Regex("^[a-zA-Z0-9]{4,}$")
        return username.matches(regex)
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
                    username = username
                )
                val response = authRepository.registerUser(userRegistration)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _registrationResult.value = RegistrationResult.Success
                } else {
                    _registrationResult.value = RegistrationResult.Error(response.body()?.message ?: "Registration failed")
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

    fun login() {
        viewModelScope.launch {
            _loginResult.value = LoginResult.Idle
            isLoading = true

            try {
                val response = authRepository.login(email, password)
                if (response.isSuccessful && response.body()?.code == 200) {
                    val fcmToken = fcmRepository.refreshFCMToken()
                    usersRepository.updateFcmToken(fcmToken)
                    _loginResult.value = LoginResult.Success
                } else {
                    _loginResult.value = LoginResult.Error(response.body()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error(e.message ?: "Unknown error")
                Log.e("RegistrationViewModel", "Login failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetLoginResult() {
        _loginResult.value = LoginResult.Idle
    }
}