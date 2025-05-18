package dev.vku.livesnap.ui.screen.auth.forgotpassword

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.AuthRepository
import dev.vku.livesnap.data.remote.dto.request.ForgotPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.ResetPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.VerifyOtpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ForgotPasswordResult {
    data object Success : ForgotPasswordResult()
    data class Error(val message: String) : ForgotPasswordResult()
    data object Idle : ForgotPasswordResult()
}

sealed class VerifyOtpResult {
    data object Success : VerifyOtpResult()
    data class Error(val message: String) : VerifyOtpResult()
    data object Idle : VerifyOtpResult()
}

sealed class ResetPasswordResult {
    data object Success : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
    data object Idle : ResetPasswordResult()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var otp by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isEmailValid by mutableStateOf(true)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val _forgotPasswordResult = MutableStateFlow<ForgotPasswordResult>(ForgotPasswordResult.Idle)
    val forgotPasswordResult: StateFlow<ForgotPasswordResult> = _forgotPasswordResult

    private val _verifyOtpResult = MutableStateFlow<VerifyOtpResult>(VerifyOtpResult.Idle)
    val verifyOtpResult: StateFlow<VerifyOtpResult> = _verifyOtpResult

    private val _resetPasswordResult = MutableStateFlow<ResetPasswordResult>(ResetPasswordResult.Idle)
    val resetPasswordResult: StateFlow<ResetPasswordResult> = _resetPasswordResult

    fun setEmailField(newEmail: String) {
        email = newEmail
        isEmailValid = validateEmail(newEmail)
    }

    fun setOtpField(newOtp: String) {
        otp = newOtp
    }

    fun setNewPasswordField(newPassword: String) {
        this.newPassword = newPassword
    }

    fun setConfirmPasswordField(confirmPassword: String) {
        this.confirmPassword = confirmPassword
    }

    private fun validateEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return regex.matches(email)
    }

    fun forgotPassword() {
        viewModelScope.launch {
            _forgotPasswordResult.value = ForgotPasswordResult.Idle
            isLoading = true
            try {
                val response = authRepository.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body()?.code == 200) {
                    _forgotPasswordResult.value = ForgotPasswordResult.Success
                } else {
                    _forgotPasswordResult.value = ForgotPasswordResult.Error(response.body()?.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                _forgotPasswordResult.value = ForgotPasswordResult.Error(e.message ?: "Unknown error")
                Log.e("ForgotPasswordViewModel", "Forgot password failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            _verifyOtpResult.value = VerifyOtpResult.Idle
            isLoading = true
            try {
                val response = authRepository.verifyOtp(VerifyOtpRequest(email, otp))
                if (response.isSuccessful && response.body()?.code == 200) {
                    _verifyOtpResult.value = VerifyOtpResult.Success
                } else {
                    _verifyOtpResult.value = VerifyOtpResult.Error(response.body()?.message ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _verifyOtpResult.value = VerifyOtpResult.Error(e.message ?: "Unknown error")
                Log.e("ForgotPasswordViewModel", "Verify OTP failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword() {
        if (newPassword != confirmPassword) {
            _resetPasswordResult.value = ResetPasswordResult.Error("Passwords do not match")
            return
        }

        if (newPassword.length < 8) {
            _resetPasswordResult.value = ResetPasswordResult.Error("Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            _resetPasswordResult.value = ResetPasswordResult.Idle
            isLoading = true
            try {
                val response = authRepository.resetPassword(
                    ResetPasswordRequest(
                        email = email,
                        otp = otp,
                        newPassword = newPassword
                    )
                )
                if (response.isSuccessful && response.body()?.code == 200) {
                    _resetPasswordResult.value = ResetPasswordResult.Success
                } else {
                    _resetPasswordResult.value = ResetPasswordResult.Error(response.body()?.message ?: "Failed to reset password")
                }
            } catch (e: Exception) {
                _resetPasswordResult.value = ResetPasswordResult.Error(e.message ?: "Unknown error")
                Log.e("ForgotPasswordViewModel", "Reset password failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetForgotPasswordResult() {
        _forgotPasswordResult.value = ForgotPasswordResult.Idle
    }

    fun resetVerifyOtpResult() {
        _verifyOtpResult.value = VerifyOtpResult.Idle
    }

    fun resetResetPasswordResult() {
        _resetPasswordResult.value = ResetPasswordResult.Idle
    }

    fun resetState() {
        email = ""
        otp = ""
        newPassword = ""
        confirmPassword = ""
        isEmailValid = true
        isLoading = false
        resetForgotPasswordResult()
        resetVerifyOtpResult()
        resetResetPasswordResult()
    }
} 