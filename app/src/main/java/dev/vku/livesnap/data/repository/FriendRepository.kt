package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.response.DefaultResponse
import dev.vku.livesnap.data.remote.dto.response.FriendListResponse
import dev.vku.livesnap.data.remote.dto.response.FriendRequestListResponse
import retrofit2.Response

interface FriendRepository {
    suspend fun fetchFriendList(limit: Int = 20, offset: Int = 0): Response<FriendListResponse>
    suspend fun sendFriendRequest(userId: String): Response<DefaultResponse>
    suspend fun fetchIncomingRequestList(): Response<FriendRequestListResponse>
    suspend fun fetchOutgoingRequestList(): Response<FriendRequestListResponse>
    suspend fun acceptFriendRequest(requestId: String): Response<DefaultResponse>
    suspend fun rejectFriendRequest(requestId: String): Response<DefaultResponse>
    suspend fun removeFriend(friendId: String): Response<DefaultResponse>
    suspend fun cancelFriendRequest(requestId: String): Response<DefaultResponse>
}

class DefaultFriendRepository(
    private val apiService: ApiService
) : FriendRepository {
    override suspend fun fetchFriendList(
        limit: Int,
        offset: Int
    ): Response<FriendListResponse> {
        return apiService.fetchFriendList(limit, offset)
    }

    override suspend fun sendFriendRequest(userId: String): Response<DefaultResponse> {
        return apiService.sendFriendRequest(userId)
    }

    override suspend fun fetchIncomingRequestList(): Response<FriendRequestListResponse> {
        return apiService.fetchIncomingRequestList()
    }

    override suspend fun fetchOutgoingRequestList(): Response<FriendRequestListResponse> {
        return apiService.fetchOutgoingRequestList()
    }

    override suspend fun acceptFriendRequest(requestId: String): Response<DefaultResponse> {
        return apiService.acceptFriendRequest(requestId)
    }

    override suspend fun rejectFriendRequest(requestId: String): Response<DefaultResponse> {
        return apiService.rejectFriendRequest(requestId)
    }

    override suspend fun removeFriend(friendId: String): Response<DefaultResponse> {
        return apiService.removeFriend(friendId)
    }

    override suspend fun cancelFriendRequest(requestId: String): Response<DefaultResponse> {
        return apiService.cancelFriendRequest(requestId)
    }
}