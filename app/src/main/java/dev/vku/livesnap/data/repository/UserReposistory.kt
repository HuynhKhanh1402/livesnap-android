package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.dto.UserRegistrationDTO
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.domain.model.User

interface UsersRepository {
    suspend fun registerUser(user: UserRegistrationDTO): User
}

class DefaultUsersRepository(
    private val apiService: ApiService
) : UsersRepository {
    override suspend fun registerUser(user: UserRegistrationDTO): User {
        val userDTO = apiService.registerUser(user)
        return userDTO.toDomain()
    }
}