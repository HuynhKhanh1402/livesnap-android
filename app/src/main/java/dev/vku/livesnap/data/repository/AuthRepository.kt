package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.LoginRequest
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.response.LoginResponse
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(email: String, password: String): Response<LoginResponse>
    suspend fun registerUser(user: UserRegistrationRequest): Response<UserRegistrationResponse>
    suspend fun logout(): Response<Unit>
    suspend fun getCurrentUserId(): String?
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

    override suspend fun logout(): Response<Unit> {
        return apiService.logout()
    }

    override suspend fun getCurrentUserId(): String? {
        return tokenManager.getCurrentUserId()
    }
} 