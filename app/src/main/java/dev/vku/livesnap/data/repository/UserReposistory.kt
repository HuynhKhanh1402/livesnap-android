package dev.vku.livesnap.data.repository

import dev.vku.livesnap.data.remote.ApiService

interface UsersRepository {

}

class DefaultUsersRepository(
    val apiService: ApiService
) : UsersRepository {
}