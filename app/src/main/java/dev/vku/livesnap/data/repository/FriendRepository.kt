package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.response.FriendListResponse
import retrofit2.Response

interface FriendRepository {
    suspend fun fetchFriendList(limit: Int = 20, offset: Int = 0): Response<FriendListResponse>
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
}