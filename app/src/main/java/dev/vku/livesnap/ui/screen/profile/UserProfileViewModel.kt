package dev.vku.livesnap.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

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
}