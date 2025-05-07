package dev.vku.livesnap.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.FriendRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.model.Friend
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendModalViewModel @Inject constructor(
    val friendRepository: FriendRepository
) : ViewModel() {
    var isFirstLoad = true
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchIncomingRequestListResult = MutableStateFlow<LoadingResult<List<Friend>>>(LoadingResult.Idle)
    val fetchIncomingRequestListResult: StateFlow<LoadingResult<List<Friend>>> = _fetchIncomingRequestListResult

    private val _fetchFriendListResult = MutableStateFlow<LoadingResult<List<Friend>>>(LoadingResult.Idle)
    val fetchFriendListResult: StateFlow<LoadingResult<List<Friend>>> = _fetchFriendListResult

    fun resetViewModel() {
        isFirstLoad = true
        _fetchFriendListResult.value = LoadingResult.Idle
    }

    fun fetchIncomingRequestList() {

    }

    fun fetchFriendList() {
        viewModelScope.launch {
            _isLoading.value = false
            _fetchFriendListResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.fetchFriendList()
                if (response.isSuccessful && response.body()?.code == 200) {
                    val friendList = response.body()?.data?.toDomain() ?: emptyList()
                    _fetchFriendListResult.value = LoadingResult.Success(friendList)
                } else {
                    _fetchFriendListResult.value =
                        LoadingResult.Error("Error: ${response.message() ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "An error occurred while fetching friend count: ${e.message}", e)
                _fetchFriendListResult.value = LoadingResult.Error("An error occurred while fetching: ${e.message}")
            } finally {
                isFirstLoad = false
                _isLoading.value = false
            }
        }
    }
}