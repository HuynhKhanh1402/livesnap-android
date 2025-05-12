package dev.vku.livesnap.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.FCMRepository
import dev.vku.livesnap.data.repository.FriendRepository
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.model.Friend
import dev.vku.livesnap.domain.model.FriendRequest
import dev.vku.livesnap.domain.model.User
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendModalViewModel @Inject constructor(
    val userRepository: UsersRepository,
    val friendRepository: FriendRepository,
    private val fcmRepository: FCMRepository
) : ViewModel() {
    var isFirstLoad = true
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchUsersResult = MutableStateFlow<LoadingResult<List<User>>>(LoadingResult.Idle)
    val searchUsersResult: StateFlow<LoadingResult<List<User>>> = _searchUsersResult

    private val _sendFriendRequestResult = MutableStateFlow<LoadingResult<Unit>>(LoadingResult.Idle)
    val sendFriendRequestResult: StateFlow<LoadingResult<Unit>> = _sendFriendRequestResult
    var requestedUserId: String? = null
        private set

    private val _fetchIncomingRequestListResult = MutableStateFlow<LoadingResult<List<FriendRequest>>>(LoadingResult.Idle)
    val fetchIncomingRequestListResult: StateFlow<LoadingResult<List<FriendRequest>>> = _fetchIncomingRequestListResult
    var acceptingRequestId: String? = null
        private set
    var rejectingRequestId: String? = null

    private val _acceptFriendRequestResult = MutableStateFlow<LoadingResult<Unit>>(LoadingResult.Idle)
    val acceptFriendRequestResult: StateFlow<LoadingResult<Unit>> = _acceptFriendRequestResult

    private val _rejectFriendRequestResult = MutableStateFlow<LoadingResult<Unit>>(LoadingResult.Idle)
    val rejectFriendRequestResult: StateFlow<LoadingResult<Unit>> = _rejectFriendRequestResult

    private val _fetchFriendListResult = MutableStateFlow<LoadingResult<List<Friend>>>(LoadingResult.Idle)
    val fetchFriendListResult: StateFlow<LoadingResult<List<Friend>>> = _fetchFriendListResult
    var removedFriendId: String? = null
        private set

    private val _removeFriendResult = MutableStateFlow<LoadingResult<Unit>>(LoadingResult.Idle)
    val removeFriendResult: StateFlow<LoadingResult<Unit>> = _removeFriendResult

    private val _sentFriendRequestListResult = MutableStateFlow<LoadingResult<List<FriendRequest>>>(LoadingResult.Idle)
    val sentFriendRequestListResult: StateFlow<LoadingResult<List<FriendRequest>>> = _sentFriendRequestListResult
    var cancellingRequestId: String? = null
        private set

    private val _cancelFriendRequestResult = MutableStateFlow<LoadingResult<Unit>>(LoadingResult.Idle)
    val cancelFriendRequestResult: StateFlow<LoadingResult<Unit>> = _cancelFriendRequestResult

    init {
        observeSearchQuery()
    }


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchUsersResult.value = LoadingResult.Idle
        }
    }

    fun resetViewModel() {
        isFirstLoad = true
        _fetchFriendListResult.value = LoadingResult.Idle
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    _searchUsersResult.value = LoadingResult.Loading
                    try {
                        val response = userRepository.searchUsers(searchQuery.value)
                        if (response.isSuccessful && response.body()?.code == 200) {
                            val users = response.body()?.data?.toDomain() ?: emptyList()
                            _searchUsersResult.value = LoadingResult.Success(users)
                        } else {
                            _searchUsersResult.value = LoadingResult.Error("Error: ${response.body()?.message ?: "Unknown error"}")
                        }
                    } catch (e: Exception) {
                        Log.e("FriendModalViewModel", "An error occurred while searching users: ${e.message}", e)
                        _searchUsersResult.value = LoadingResult.Error("An error occurred while searching users: ${e.message}")
                    }
                }

        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            requestedUserId = userId
            _sendFriendRequestResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.sendFriendRequest(userId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _sendFriendRequestResult.value = LoadingResult.Success(Unit)

                    // Get user info to send notification
                    val userResponse = userRepository.getUserById(userId)
                    if (userResponse.isSuccessful && userResponse.body()?.data != null) {
                        val user = userResponse.body()!!.data.user.toDomain()
                        fcmRepository.sendFriendRequestNotification(
                            receiverId = userId,
                            senderName = "${user.lastName} ${user.firstName}"
                        )
                    }
                } else {
                    _sendFriendRequestResult.value =
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "Error sending friend request: ${e.message}", e)
                _sendFriendRequestResult.value = LoadingResult.Error("Error sending friend request: ${e.message}")
            }
        }
    }

    fun resetSendFriendRequestResult() {
        _sendFriendRequestResult.value = LoadingResult.Idle
        fetchSentFriendRequestList()
    }

    fun fetchIncomingRequestList() {
        viewModelScope.launch {
            _isLoading.value = true
            _fetchIncomingRequestListResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.fetchIncomingRequestList()
                if (response.isSuccessful && response.body()?.code == 200) {
                    val incomingRequestList =
                        response.body()?.data?.requests?.toDomain() ?: emptyList()
                    _fetchIncomingRequestListResult.value =
                        LoadingResult.Success(incomingRequestList)
                } else {
                    _fetchIncomingRequestListResult.value =
                        LoadingResult.Error("Error: ${response.body()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "An error occurred while fetching incoming requests: ${e.message}", e)
                _fetchIncomingRequestListResult.value = LoadingResult.Error("An error occurred while fetching incoming requests: ${e.message}")
            } finally {
                isFirstLoad = false
                _isLoading.value = false
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            acceptingRequestId = requestId
            _acceptFriendRequestResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.acceptFriendRequest(requestId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _acceptFriendRequestResult.value = LoadingResult.Success(Unit)
                    
                    // Get the request details to send notification
                    val incomingRequests = friendRepository.fetchIncomingRequestList()
                    if (incomingRequests.isSuccessful && incomingRequests.body()?.data != null) {
                        val request = incomingRequests.body()!!.data.requests.find { it.id == requestId }
                        if (request != null) {
                            fcmRepository.sendFriendRequestAcceptedNotification(
                                receiverId = request.user.id,
                                accepterName = "${request.user.lastName} ${request.user.firstName}"
                            )
                        }
                    }
                } else {
                    _acceptFriendRequestResult.value =
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "Error accepting friend request: ${e.message}", e)
                _acceptFriendRequestResult.value = LoadingResult.Error("Error accepting friend request: ${e.message}")
            }
        }
    }

    fun resetAcceptFriendRequestResult() {
        _acceptFriendRequestResult.value = LoadingResult.Idle
        fetchIncomingRequestList()
        fetchFriendList()
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            rejectingRequestId = requestId
            _rejectFriendRequestResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.rejectFriendRequest(requestId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _rejectFriendRequestResult.value = LoadingResult.Success(Unit)
                } else {
                    _rejectFriendRequestResult.value =
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                    Log.e(
                        "FriendModalViewModel",
                        "Error rejecting friend request: ${response.body()?.message ?: "Unknown error"}"
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "FriendModalViewModel",
                    "An error occurred while rejecting friend request: ${e.message}",
                    e
                )
                _rejectFriendRequestResult.value =
                    LoadingResult.Error("An error occurred while rejecting friend request: ${e.message}")
            }
        }
    }

    fun resetRejectFriendRequestResult() {
        _rejectFriendRequestResult.value = LoadingResult.Idle
        fetchIncomingRequestList()
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
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "An error occurred while fetching friend count: ${e.message}", e)
                _fetchFriendListResult.value = LoadingResult.Error("An error occurred while fetching: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            removedFriendId = friendId
            _removeFriendResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.removeFriend(friendId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _removeFriendResult.value = LoadingResult.Success(Unit)
                } else {
                    _removeFriendResult.value =
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "An error occurred while removing friend: ${e.message}", e)
                _removeFriendResult.value = LoadingResult.Error("An error occurred while removing friend: ${e.message}")
            }
        }
    }

    fun resetRemoveFriendResult() {
        _removeFriendResult.value = LoadingResult.Idle
        fetchFriendList()
    }

    fun fetchSentFriendRequestList() {
        viewModelScope.launch {
            _isLoading.value = true
            _sentFriendRequestListResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.fetchOutgoingRequestList()
                if (response.isSuccessful && response.body()?.code == 200) {
                    val sentRequestList =
                        response.body()?.data?.requests?.toDomain() ?: emptyList()
                    _sentFriendRequestListResult.value =
                        LoadingResult.Success(sentRequestList)
                } else {
                    Log.e("FriendModalViewModel", "Error fetching sent friend requests: ${response.body()?.message ?: "Unknown error"}")
                    _sentFriendRequestListResult.value =
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "An error occurred while fetching sent friend requests: ${e.message}", e)
                _sentFriendRequestListResult.value = LoadingResult.Error("An error occurred while fetching sent friend requests: ${e.message}")
            }
        }
    }

    fun cancelFriendRequest(userId: String) {
        viewModelScope.launch {
            cancellingRequestId = userId
            _cancelFriendRequestResult.value = LoadingResult.Loading
            try {
                val response = friendRepository.cancelFriendRequest(userId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    _cancelFriendRequestResult.value = LoadingResult.Success(Unit)
                } else {
                    _cancelFriendRequestResult.value =
                        LoadingResult.Error(response.body()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FriendModalViewModel", "An error occurred while cancelling friend request: ${e.message}", e)
                _cancelFriendRequestResult.value = LoadingResult.Error("An error occurred while cancelling friend request: ${e.message}")}
        }
    }

    fun resetCancelFriendRequestResult() {
        _cancelFriendRequestResult.value = LoadingResult.Idle
        fetchSentFriendRequestList()
    }
}