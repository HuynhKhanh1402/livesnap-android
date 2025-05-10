package dev.vku.livesnap.ui.screen.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
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

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    val tokenManager: TokenManager,
    val userRepository: UsersRepository
) : ViewModel() {
    var isFirstLoad = true

    private val _fetchUserResult = MutableStateFlow<FetchUserResult>(FetchUserResult.Idle)
    val fetchUserResult: StateFlow<FetchUserResult> = _fetchUserResult

    private val _logoutResult = MutableStateFlow<LogoutResult>(LogoutResult.Idle)
    val logoutResult: StateFlow<LogoutResult> = _logoutResult

    private val _uploadAvatarResult = MutableStateFlow<UploadAvatarResult>(UploadAvatarResult.Idle)
    val uploadAvatarResult: StateFlow<UploadAvatarResult> = _uploadAvatarResult

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _uiEvent = MutableSharedFlow<ProfileUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun fetchUser() {
        viewModelScope.launch {
            _loadingState.value = true
            isFirstLoad = false
            try {
                val response = userRepository.fetchUserDetail()
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

    fun resetFetchUserResult() {
        _fetchUserResult.value = FetchUserResult.Idle
        isFirstLoad = true
    }

    fun logout() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                tokenManager.clearToken()
                _logoutResult.value = LogoutResult.Success
            } catch (e: Exception) {
                _logoutResult.value = LogoutResult.Error("Error logging out: ${e.message}")
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun pickImageFromGallery() {
        viewModelScope.launch {
            _uiEvent.emit(ProfileUiEvent.PickImageFromGallery)
        }
    }

    fun updateAvatar(uri: Uri) {
        // Implement avatar update logic here
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
}