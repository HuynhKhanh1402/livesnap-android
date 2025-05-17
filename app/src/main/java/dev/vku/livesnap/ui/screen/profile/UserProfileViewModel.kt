package dev.vku.livesnap.ui.screen.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dev.vku.livesnap.ui.ViewModelResetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.repository.AuthRepository
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

sealed class FetchUserResult {
    data class Success(val user: User) : FetchUserResult()
    data class Error(val message: String) : FetchUserResult()
    data object Idle : FetchUserResult()
}

sealed class LogoutResult {
    data object Success : LogoutResult()
    data class Error(val message: String) : LogoutResult()
    data object Idle : LogoutResult()
}

sealed class LogoutUiState {
    data object Initial : LogoutUiState()
    data object Loading : LogoutUiState()
    data object Success : LogoutUiState()
    data class Error(val message: String) : LogoutUiState()
}

sealed class UploadAvatarResult {
    data object Success : UploadAvatarResult()
    data class Error(val message: String) : UploadAvatarResult()
    data object Idle : UploadAvatarResult()
}

sealed class ProfileUiEvent {
    object PickImageFromGallery : ProfileUiEvent()
    object CaptureImageFromCamera : ProfileUiEvent()
    data class ShowSnackbar(val message: String) : ProfileUiEvent()
}

sealed class ChangeEmailUiState {
    object Initial : ChangeEmailUiState()
    object Loading : ChangeEmailUiState()
    object PasswordVerified : ChangeEmailUiState()
    object Success : ChangeEmailUiState()
    data class Error(val message: String) : ChangeEmailUiState()
}

sealed class UpdateUsernameUiState {
    data object Initial : UpdateUsernameUiState()
    data object Loading : UpdateUsernameUiState()
    data object Success : UpdateUsernameUiState()
    data class Error(val message: String) : UpdateUsernameUiState()
}

sealed class SendFeedbackUiState {
    object Idle : SendFeedbackUiState()
    object Loading : SendFeedbackUiState()
    data class Success(val message: String) : SendFeedbackUiState()
    data class Error(val error: String) : SendFeedbackUiState()
}

sealed class FeedbackHistoryUiState {
    object Idle : FeedbackHistoryUiState()
    object Loading : FeedbackHistoryUiState()
    data class Success(val feedbacks: List<dev.vku.livesnap.data.remote.dto.response.FeedbackHistoryResponse.FeedbackItem>) : FeedbackHistoryUiState()
    data class Error(val error: String) : FeedbackHistoryUiState()
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    val tokenManager: TokenManager,
    val userRepository: UsersRepository,
    val authRepository: AuthRepository,
    val viewModelResetManager: ViewModelResetManager
) : ViewModel() {
    private val _fetchUserResult = MutableStateFlow<FetchUserResult>(FetchUserResult.Idle)
    val fetchUserResult: StateFlow<FetchUserResult> = _fetchUserResult

    private val _logoutResult = MutableStateFlow<LogoutResult>(LogoutResult.Idle)
    val logoutResult: StateFlow<LogoutResult> = _logoutResult

    private val _logoutUiState = MutableStateFlow<LogoutUiState>(LogoutUiState.Initial)
    val logoutUiState: StateFlow<LogoutUiState> = _logoutUiState

    private val _uploadAvatarResult = MutableStateFlow<UploadAvatarResult>(UploadAvatarResult.Idle)
    val uploadAvatarResult: StateFlow<UploadAvatarResult> = _uploadAvatarResult

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _changeEmailUiState = MutableStateFlow<ChangeEmailUiState>(ChangeEmailUiState.Initial)
    val changeEmailUiState: StateFlow<ChangeEmailUiState> = _changeEmailUiState

    private val _updateUsernameUiState = MutableStateFlow<UpdateUsernameUiState>(UpdateUsernameUiState.Initial)
    val updateUsernameUiState: StateFlow<UpdateUsernameUiState> = _updateUsernameUiState

    private val _sendFeedbackUiState = MutableStateFlow<SendFeedbackUiState>(SendFeedbackUiState.Idle)
    val sendFeedbackUiState: StateFlow<SendFeedbackUiState> = _sendFeedbackUiState

    private val _feedbackHistoryUiState = MutableStateFlow<FeedbackHistoryUiState>(FeedbackHistoryUiState.Idle)
    val feedbackHistoryUiState: StateFlow<FeedbackHistoryUiState> = _feedbackHistoryUiState

    fun resetState() {
        _fetchUserResult.value = FetchUserResult.Idle
        _logoutResult.value = LogoutResult.Idle
        _logoutUiState.value = LogoutUiState.Initial
        _uploadAvatarResult.value = UploadAvatarResult.Idle
        _loadingState.value = false
        _changeEmailUiState.value = ChangeEmailUiState.Initial
    }

    fun fetchUser() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                Log.d("UserProfileViewModel", "Fetching user detail")
                val response = userRepository.fetchUserDetail()
                Log.d("UserProfileViewModel", "Response: $response")
                Log.d("UserProfileViewModel", "Response body: ${response.body()}")
                if (response.isSuccessful) {
                    val user = response.body()?.data?.user?.toDomain()
                    if (user != null) {
                        _fetchUserResult.value = FetchUserResult.Success(user)
                    } else {
                        _fetchUserResult.value = FetchUserResult.Error("Can not parse user detail")
                    }
                } else {
                    _fetchUserResult.value =
                        FetchUserResult.Error("ERROR: ${response.message() ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                _fetchUserResult.value = FetchUserResult.Error("Error fetching user detail: ${e.message}")
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutUiState.value = LogoutUiState.Loading
            try {
                val response = authRepository.logout()
                if (response.isSuccessful) {
                    tokenManager.clearToken()
                    _logoutUiState.value = LogoutUiState.Success
                    _logoutResult.value = LogoutResult.Success
                } else {
                    _logoutUiState.value = LogoutUiState.Error("Error logging out: ${response.message()}")
                    _logoutResult.value = LogoutResult.Error("Error logging out: ${response.message()}")
                }
            } catch (e: Exception) {
                _logoutUiState.value = LogoutUiState.Error("Error logging out: ${e.message}")
                _logoutResult.value = LogoutResult.Error("Error logging out: ${e.message}")
                tokenManager.clearToken()
            }
        }
    }

    fun resetLogoutState() {
        _logoutUiState.value = LogoutUiState.Initial
        _logoutResult.value = LogoutResult.Idle
    }

    fun pickImageFromGallery() {
        viewModelScope.launch {
            _uiEvent.emit(ProfileUiEvent.PickImageFromGallery)
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val context = tokenManager.context // Giả sử TokenManager có context, nếu không hãy truyền context vào ViewModel
                val contentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Nén ảnh xuống 80% chất lượng
                val byteArrayOutputStream = ByteArrayOutputStream()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val compressedBytes = byteArrayOutputStream.toByteArray()

                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                val requestFile = compressedBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("avatar", fileName, requestFile)

                val response = (userRepository as? dev.vku.livesnap.data.repository.DefaultUsersRepository)?.apiService?.setAvatar(imagePart)
                if (response != null && response.isSuccessful) {
                    fetchUser()
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Cập nhật avatar thành công!"))
                } else {
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Cập nhật avatar thất bại!"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Lỗi cập nhật avatar: ${e.message}"))
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun updateName(firstName: String, lastName: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val response = userRepository.updateName(firstName, lastName)
                if (response.isSuccessful) {
                    fetchUser() // Làm mới dữ liệu người dùng
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Cập nhật tên thành công"))
                } else {
                    _fetchUserResult.value = FetchUserResult.Error("Lỗi API: ${response.code()} - ${response.message()}")
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Cập nhật tên thất bại: ${response.message()}"))
                }
            } catch (e: Exception) {
                _fetchUserResult.value = FetchUserResult.Error("Lỗi: ${e.message}")
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Lỗi: ${e.message}"))
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun checkPassword(password: String) {
        viewModelScope.launch {
            _changeEmailUiState.value = ChangeEmailUiState.Loading
            try {
                val response = userRepository.checkPassword(password)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isValid == true) {
                        _changeEmailUiState.value = ChangeEmailUiState.PasswordVerified
                    } else {
                        _changeEmailUiState.value = ChangeEmailUiState.Error(body?.message ?: "Invalid password")
                    }
                } else {
                    _changeEmailUiState.value = ChangeEmailUiState.Error("Invalid password")
                }
            } catch (e: Exception) {
                _changeEmailUiState.value = ChangeEmailUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun updateEmail(email: String) {
        viewModelScope.launch {
            _changeEmailUiState.value = ChangeEmailUiState.Loading
            try {
                val response = userRepository.updateEmail(email)
                if (response.isSuccessful) {
                    _changeEmailUiState.value = ChangeEmailUiState.Success
                    fetchUser() // Refresh user info
                } else {
                    _changeEmailUiState.value = ChangeEmailUiState.Error("Failed to update email")
                }
            } catch (e: Exception) {
                _changeEmailUiState.value = ChangeEmailUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun resetChangeEmailUiState() {
        _changeEmailUiState.value = ChangeEmailUiState.Initial
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            _updateUsernameUiState.value = UpdateUsernameUiState.Loading
            try {
                val response = userRepository.updateUsername(username)
                if (response.isSuccessful) {
                    _updateUsernameUiState.value = UpdateUsernameUiState.Success
                    fetchUser() // Refresh user data
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar("Username updated successfully"))
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to update username"
                    _updateUsernameUiState.value = UpdateUsernameUiState.Error(errorMessage)
                    _uiEvent.emit(ProfileUiEvent.ShowSnackbar(errorMessage))
                }
            } catch (e: Exception) {
                _updateUsernameUiState.value = UpdateUsernameUiState.Error(e.message ?: "An error occurred")
                _uiEvent.emit(ProfileUiEvent.ShowSnackbar(e.message ?: "An error occurred"))
            }
        }
    }

    fun resetUpdateUsernameState() {
        _updateUsernameUiState.value = UpdateUsernameUiState.Initial
    }

    fun sendFeedback(message: String) {
        viewModelScope.launch {
            _sendFeedbackUiState.value = SendFeedbackUiState.Loading
            try {
                val response = userRepository.sendFeedback(message)
                if (response.isSuccessful && response.body() != null) {
                    _sendFeedbackUiState.value = SendFeedbackUiState.Success(response.body()!!.message ?: "Feedback sent successfully.")
                } else {
                    _sendFeedbackUiState.value = SendFeedbackUiState.Error(response.message())
                }
            } catch (e: Exception) {
                _sendFeedbackUiState.value = SendFeedbackUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetSendFeedbackUiState() {
        _sendFeedbackUiState.value = SendFeedbackUiState.Idle
    }

    fun getFeedbackHistory() {
        viewModelScope.launch {
            _feedbackHistoryUiState.value = FeedbackHistoryUiState.Loading
            try {
                val response = userRepository.getFeedbackHistory()
                if (response.isSuccessful && response.body() != null) {
                    _feedbackHistoryUiState.value = FeedbackHistoryUiState.Success(response.body()!!.data)
                } else {
                    _feedbackHistoryUiState.value = FeedbackHistoryUiState.Error(response.message())
                }
            } catch (e: Exception) {
                _feedbackHistoryUiState.value = FeedbackHistoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetFeedbackHistoryUiState() {
        _feedbackHistoryUiState.value = FeedbackHistoryUiState.Idle
    }
}