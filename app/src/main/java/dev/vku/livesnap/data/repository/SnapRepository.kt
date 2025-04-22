package dev.vku.livesnap.data.repository

import android.content.Context
import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.UploadSnapRequest
import dev.vku.livesnap.data.remote.dto.response.SnapsResponse
import dev.vku.livesnap.data.remote.dto.response.UploadSnapResponse
import retrofit2.Response

interface SnapRepository {
    suspend fun getSnaps(currentPage: Int, pageSize: Int): Response<SnapsResponse>
    suspend fun uploadSnap(context: Context, request: UploadSnapRequest): Response<UploadSnapResponse>
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

    override suspend fun uploadSnap(context: Context, request: UploadSnapRequest): Response<UploadSnapResponse> {
        val (imagePart, captionPart) = request.toMultipartParts(context)
        return apiService.uploadSnap(imagePart, captionPart)
    }
}