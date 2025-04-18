package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.response.SnapsResponse
import retrofit2.Response

interface SnapRepository {
    suspend fun getSnaps(currentPage: Int, pageSize: Int): Response<SnapsResponse>
}

class DefaultSnapRepository(
    private val apiService: ApiService
) : SnapRepository {
    override suspend fun getSnaps(
        currentPage: Int,
        pageSize: Int
    ): Response<SnapsResponse> {
        return apiService.getSnaps(currentPage, pageSize)
    }
}