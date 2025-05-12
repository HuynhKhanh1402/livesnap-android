package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.CheckEmailExistRequest
import dev.vku.livesnap.data.remote.dto.request.CheckUsernameExistRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateNameRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateFcmTokenRequest
import dev.vku.livesnap.data.remote.dto.response.CheckEmailExistResponse
import dev.vku.livesnap.data.remote.dto.response.CheckUsernameExistResponse
import dev.vku.livesnap.data.remote.dto.response.UserDetailResponse
import dev.vku.livesnap.data.remote.dto.response.UserListResponse
import retrofit2.Response

interface UsersRepository {
    suspend fun checkEmailExist(email: String): CheckEmailExistResponse
    suspend fun checkUsernameExist(username: String): CheckUsernameExistResponse
    suspend fun fetchUserDetail(): Response<UserDetailResponse>
    suspend fun getUserById(userId: String): Response<UserDetailResponse>
    suspend fun updateName(firstName: String, lastName: String): Response<Unit>
    suspend fun searchUsers(username: String): Response<UserListResponse>
    suspend fun checkPassword(password: String): Response<dev.vku.livesnap.data.remote.dto.response.DefaultResponse>
    suspend fun updateEmail(email: String): Response<dev.vku.livesnap.data.remote.dto.response.DefaultResponse>
    suspend fun updateFcmToken(fcmToken: String): Response<dev.vku.livesnap.data.remote.dto.response.DefaultResponse>
}

class DefaultUsersRepository(
    internal val apiService: ApiService
) : UsersRepository {
    override suspend fun checkEmailExist(email: String): CheckEmailExistResponse {
        return apiService.checkEmailExist(CheckEmailExistRequest(email))
    }

    override suspend fun checkUsernameExist(username: String): CheckUsernameExistResponse {
        return apiService.checkUsernameExist(CheckUsernameExistRequest(username))
    }

    override suspend fun fetchUserDetail(): Response<UserDetailResponse> {
        return apiService.fetchUserDetail()
    }

    override suspend fun getUserById(userId: String): Response<UserDetailResponse> {
        return apiService.getUserById(userId)
    }

    override suspend fun updateName(
        firstName: String,
        lastName: String
    ): Response<Unit> {
        return apiService.updateName(UpdateNameRequest(firstName, lastName))
    }

    override suspend fun searchUsers(username: String): Response<UserListResponse> {
        return apiService.searchUsers(username)
    }

    override suspend fun checkPassword(password: String): Response<dev.vku.livesnap.data.remote.dto.response.DefaultResponse> {
        return apiService.checkPassword(dev.vku.livesnap.data.remote.dto.request.CheckPasswordRequest(password))
    }

    override suspend fun updateEmail(email: String): Response<dev.vku.livesnap.data.remote.dto.response.DefaultResponse> {
        return apiService.updateEmail(dev.vku.livesnap.data.remote.dto.request.UpdateEmailRequest(email))
    }

    override suspend fun updateFcmToken(fcmToken: String): Response<dev.vku.livesnap.data.remote.dto.response.DefaultResponse> {
        return apiService.updateFcmToken(UpdateFcmTokenRequest(fcmToken))
    }
}