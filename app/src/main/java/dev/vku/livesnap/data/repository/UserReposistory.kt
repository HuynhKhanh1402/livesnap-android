package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.request.CheckEmailExistRequest
import dev.vku.livesnap.data.remote.dto.request.CheckUsernameExistRequest
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.response.CheckEmailExistResponse
import dev.vku.livesnap.data.remote.dto.response.CheckUsernameExistResponse
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse

interface UsersRepository {
    suspend fun registerUser(user: UserRegistrationRequest): UserRegistrationResponse
    suspend fun checkEmailExist(email: String): CheckEmailExistResponse
    suspend fun checkUsernameExist(username: String): CheckUsernameExistResponse
}

class DefaultUsersRepository(
    private val apiService: ApiService
) : UsersRepository {
    override suspend fun registerUser(user: UserRegistrationRequest): UserRegistrationResponse {
        return apiService.registerUser(user)
    }

    override suspend fun checkEmailExist(email: String): CheckEmailExistResponse {
        return apiService.checkEmailExist(CheckEmailExistRequest(email))
    }

    override suspend fun checkUsernameExist(username: String): CheckUsernameExistResponse {
        return apiService.checkUsernameExist(CheckUsernameExistRequest(username))
    }
}