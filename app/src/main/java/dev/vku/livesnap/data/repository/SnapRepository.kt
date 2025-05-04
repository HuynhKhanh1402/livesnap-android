package dev.vku.livesnap.data.repository

import android.content.Context
import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.ReactSnapRequest
import dev.vku.livesnap.data.remote.dto.request.UploadSnapRequest
import dev.vku.livesnap.data.remote.dto.response.DefaultResponse
import dev.vku.livesnap.data.remote.dto.response.SnapsResponse
import dev.vku.livesnap.data.remote.dto.response.UploadSnapResponse
import retrofit2.Response

interface SnapRepository {
    suspend fun getSnaps(currentPage: Int, pageSize: Int): Response<SnapsResponse>
    suspend fun uploadSnap(context: Context, request: UploadSnapRequest): Response<UploadSnapResponse>
    suspend fun deleteSnap(snapId: String): Response<Unit>
    suspend fun reactSnap(snapId: String, emoji: String): Response<DefaultResponse>
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

    override suspend fun deleteSnap(snapId: String): Response<Unit> {
        return apiService.deleteSnap(snapId)
    }

    override suspend fun reactSnap(
        snapId: String,
        emoji: String
    ): Response<DefaultResponse> {
        return apiService.reactSnap(ReactSnapRequest(snapId, emoji))
    }
}