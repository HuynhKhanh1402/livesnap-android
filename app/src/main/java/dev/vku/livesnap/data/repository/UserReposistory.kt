package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse

interface UsersRepository {
    suspend fun registerUser(user: UserRegistrationRequest): UserRegistrationResponse
}

class DefaultUsersRepository(
    private val apiService: ApiService
) : UsersRepository {
    override suspend fun registerUser(user: UserRegistrationRequest): UserRegistrationResponse {
        return apiService.registerUser(user)
    }
}