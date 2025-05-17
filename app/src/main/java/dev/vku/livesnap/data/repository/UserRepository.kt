package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.CheckEmailExistRequest
import dev.vku.livesnap.data.remote.dto.request.CheckUsernameExistRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateNameRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateFcmTokenRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateUsernameRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateVisibilityRequest
import dev.vku.livesnap.data.remote.dto.response.CheckEmailExistResponse
import dev.vku.livesnap.data.remote.dto.response.CheckUsernameExistResponse
import dev.vku.livesnap.data.remote.dto.response.UserDetailResponse
import dev.vku.livesnap.data.remote.dto.response.UserListResponse
import dev.vku.livesnap.data.remote.dto.response.DefaultResponse
import dev.vku.livesnap.data.remote.dto.response.PaymentQRResponse
import dev.vku.livesnap.data.remote.dto.response.FeedbackHistoryResponse
import retrofit2.Response

interface UsersRepository {
    suspend fun checkEmailExist(email: String): CheckEmailExistResponse
    suspend fun checkUsernameExist(username: String): CheckUsernameExistResponse
    suspend fun fetchUserDetail(): Response<UserDetailResponse>
    suspend fun getUserById(userId: String): Response<UserDetailResponse>
    suspend fun updateName(firstName: String, lastName: String): Response<Unit>
    suspend fun searchUsers(username: String): Response<UserListResponse>
    suspend fun checkPassword(password: String): Response<DefaultResponse>
    suspend fun updateEmail(email: String): Response<DefaultResponse>
    suspend fun updateFcmToken(fcmToken: String): Response<DefaultResponse>
    suspend fun updateUsername(username: String): Response<DefaultResponse>
    suspend fun getPaymentQR(): Response<PaymentQRResponse>
    suspend fun sendFeedback(message: String): Response<DefaultResponse>
    suspend fun getFeedbackHistory(): Response<FeedbackHistoryResponse>
    suspend fun updateVisibility(visible: Boolean): Response<DefaultResponse>
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

    override suspend fun checkPassword(password: String): Response<DefaultResponse> {
        return apiService.checkPassword(dev.vku.livesnap.data.remote.dto.request.CheckPasswordRequest(password))
    }

    override suspend fun updateEmail(email: String): Response<DefaultResponse> {
        return apiService.updateEmail(dev.vku.livesnap.data.remote.dto.request.UpdateEmailRequest(email))
    }

    override suspend fun updateFcmToken(fcmToken: String): Response<DefaultResponse> {
        return apiService.updateFcmToken(UpdateFcmTokenRequest(fcmToken))
    }

    override suspend fun updateUsername(username: String): Response<DefaultResponse> {
        return apiService.updateUsername(UpdateUsernameRequest(username))
    }

    override suspend fun getPaymentQR(): Response<PaymentQRResponse> {
        return apiService.getPaymentQR()
    }

    override suspend fun sendFeedback(message: String): Response<DefaultResponse> {
        return apiService.sendFeedback(dev.vku.livesnap.data.remote.dto.request.FeedbackRequest(message))
    }

    override suspend fun getFeedbackHistory(): Response<FeedbackHistoryResponse> {
        return apiService.getFeedbackHistory()
    }

    override suspend fun updateVisibility(visible: Boolean): Response<DefaultResponse> {
        return apiService.updateVisibility(UpdateVisibilityRequest(visible))
    }
}