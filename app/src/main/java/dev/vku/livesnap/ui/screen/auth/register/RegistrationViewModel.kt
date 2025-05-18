package dev.vku.livesnap.ui.screen.auth.register

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.remote.dto.request.SendVerificationOtpRequest
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

sealed class UsernameExistResult {
    data object Exist : UsernameExistResult()
    data object NotExist : UsernameExistResult()
    data class Error(val message: String) : UsernameExistResult()
    data object Idle : UsernameExistResult()
}

sealed class SendOtpResult {
    data object Success : SendOtpResult()
    data class Error(val message: String) : SendOtpResult()
    data object Idle : SendOtpResult()
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
    var username by mutableStateOf("")
    var otp by mutableStateOf("")
    var isEmailValid by mutableStateOf(true)
        private set
    var isUsernameValid by mutableStateOf(true)
        private set

    private val _usernameExistResult = MutableStateFlow<UsernameExistResult>(UsernameExistResult.Idle)
    val usernameExistResult: StateFlow<UsernameExistResult> = _usernameExistResult

    private val _sendOtpResult = MutableStateFlow<SendOtpResult>(SendOtpResult.Idle)
    val sendOtpResult: StateFlow<SendOtpResult> = _sendOtpResult

    var isLoading by mutableStateOf(false)
        private set

    fun setEmailField(newEmail: String) {
        email = newEmail
        isEmailValid = validateEmail(newEmail)
    }

    fun setUsernameField(newUsername: String) {
        username = newUsername
        isUsernameValid = isValidUsername()
    }

    fun setOtpField(newOtp: String) {
        otp = newOtp
    }

    var firstName by mutableStateOf("")
        private set

    var lastName by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var firstNameError by mutableStateOf<String?>(null)
        private set

    var lastNameError by mutableStateOf<String?>(null)
        private set

    private val _emailExistResult = MutableStateFlow<EmailExistResult>(EmailExistResult.Idle)
    val emailExistResult: StateFlow<EmailExistResult> = _emailExistResult

    private val _registrationResult = MutableStateFlow<RegistrationResult>(RegistrationResult.Idle)
    val registrationResult: StateFlow<RegistrationResult> = _registrationResult

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult

    fun setFirstNameField(newFirstName: String) {
        firstName = newFirstName
        if (firstNameError != null) {
            validateNames()
        }
    }

    fun setLastNameField(newLastName: String) {
        lastName = newLastName
        if (lastNameError != null) {
            validateNames()
        }
    }

    fun setPasswordField(newPassword: String) {
        password = newPassword
    }

    fun validateEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()
        return email.matches(regex)
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

    fun checkUsernameExists() {
        viewModelScope.launch {
            _usernameExistResult.value = UsernameExistResult.Idle
            isLoading = true
            try {
                val response = usersRepository.checkUsernameExist(username)
                if (response.code == 200) {
                    _usernameExistResult.value =
                        if (response.exist) UsernameExistResult.Exist else UsernameExistResult.NotExist
                } else {
                    _usernameExistResult.value = UsernameExistResult.Error(response.message)
                }
            } catch (e: Exception) {
                _usernameExistResult.value = UsernameExistResult.Error(e.message ?: "Unknown error")
                Log.e("RegistrationViewModel", "Username check failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun sendVerificationOtp() {
        viewModelScope.launch {
            _sendOtpResult.value = SendOtpResult.Idle
            isLoading = true
            try {
                val request = SendVerificationOtpRequest(email, username)
                val response = authRepository.sendVerificationOtp(request)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _sendOtpResult.value = SendOtpResult.Success
                } else {
                    _sendOtpResult.value = SendOtpResult.Error(response.body()?.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                _sendOtpResult.value = SendOtpResult.Error(e.message ?: "Unknown error")
                Log.e("RegistrationViewModel", "Send OTP failed: ${e.message}", e)
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

    fun resetUsernameExistResult() {
        _usernameExistResult.value = UsernameExistResult.Idle
    }

    fun resetSendOtpResult() {
        _sendOtpResult.value = SendOtpResult.Idle
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
                    username = username,
                    otp = otp
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

    fun resetState() {
        isLoading = false
        email = ""
        username = ""
        otp = ""
        isEmailValid = true
        isUsernameValid = true
        _usernameExistResult.value = UsernameExistResult.Idle
        _sendOtpResult.value = SendOtpResult.Idle
        firstName = ""
        lastName = ""
        password = ""
        firstNameError = null
        lastNameError = null
        _emailExistResult.value = EmailExistResult.Idle
        _registrationResult.value = RegistrationResult.Idle
        _loginResult.value = LoginResult.Idle
    }
}