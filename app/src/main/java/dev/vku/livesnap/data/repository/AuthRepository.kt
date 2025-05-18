package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.ForgotPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.LoginRequest
import dev.vku.livesnap.data.remote.dto.request.ResetPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.SendVerificationOtpRequest
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.request.VerifyOtpRequest
import dev.vku.livesnap.data.remote.dto.response.DefaultResponse
import dev.vku.livesnap.data.remote.dto.response.LoginResponse
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(email: String, password: String): Response<LoginResponse>
    suspend fun registerUser(user: UserRegistrationRequest): Response<UserRegistrationResponse>
    suspend fun sendVerificationOtp(request: SendVerificationOtpRequest): Response<DefaultResponse>
    suspend fun logout(): Response<Unit>
    suspend fun getCurrentUserId(): String?
    suspend fun forgotPassword(request: ForgotPasswordRequest): Response<DefaultResponse>
    suspend fun verifyOtp(request: VerifyOtpRequest): Response<DefaultResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): Response<DefaultResponse>
}

class DefaultAuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Response<LoginResponse> {
        val response = apiService.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            response.body()?.data?.token?.let { token ->
                tokenManager.saveToken(token)
            }
        }
        return response
    }

    override suspend fun registerUser(user: UserRegistrationRequest): Response<UserRegistrationResponse> {
        return apiService.registerUser(user)
    }

    override suspend fun sendVerificationOtp(request: SendVerificationOtpRequest): Response<DefaultResponse> {
        return apiService.sendVerificationOtp(request)
    }

    override suspend fun logout(): Response<Unit> {
        val response = apiService.logout()
        if (response.isSuccessful) {
            tokenManager.clearToken()
        }
        return response
    }

    override suspend fun getCurrentUserId(): String? {
        return tokenManager.getCurrentUserId()
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Response<DefaultResponse> {
        return apiService.forgotPassword(request)
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Response<DefaultResponse> {
        return apiService.verifyOtp(request)
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Response<DefaultResponse> {
        return apiService.resetPassword(request)
    }
} 