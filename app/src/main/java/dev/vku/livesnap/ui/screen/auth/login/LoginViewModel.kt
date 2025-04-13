package dev.vku.livesnap.ui.screen.auth.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CheckEmailExistResult {
    data object Exist : CheckEmailExistResult()
    data object NotExist : CheckEmailExistResult()
    data class Error(val message: String) : CheckEmailExistResult()
    data object Idle : CheckEmailExistResult()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {
    var email by mutableStateOf("")

    var isEmailValid by mutableStateOf(true)
        private set

    var isEmailNonExist by mutableStateOf(false)
        private set

    fun setEmailField(newEmail: String) {
        email = newEmail
        isEmailValid = validateEmail(newEmail)
    }

    var password by mutableStateOf("")

    var passwordError by mutableStateOf<String?>(null)
        private set

    fun setPasswordField(newPassword: String) {
        password = newPassword
    }

    var isLoading by mutableStateOf(false)
        private set

    private val _checkEmailExistResult = MutableStateFlow<CheckEmailExistResult>(CheckEmailExistResult.Idle)
    var checkEmailExistResult: StateFlow<CheckEmailExistResult> = _checkEmailExistResult

    private fun validateEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return regex.matches(email)
    }

    fun checkEmailIsExists() {
        viewModelScope.launch {
            _checkEmailExistResult.value = CheckEmailExistResult.Idle
            isEmailNonExist = false
            isLoading = true
            try {
                val response = usersRepository.checkEmailExist(email)
                if (response.code == 200) {
                    isEmailNonExist = !response.exist
                    _checkEmailExistResult.value =
                        if (response.exist) CheckEmailExistResult.Exist else CheckEmailExistResult.NotExist
                } else {
                    _checkEmailExistResult.value = CheckEmailExistResult.Error(response.message)
                }
            } catch (e: Exception) {
                _checkEmailExistResult.value = CheckEmailExistResult.Error(e.message ?: "Unknown error")
                Log.e("LoginViewModel", "Email check failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetCheckEmailExistResult() {
        _checkEmailExistResult.value = CheckEmailExistResult.Idle
    }
}