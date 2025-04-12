package dev.vku.livesnap.data.remote

import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("users/register")
    suspend fun registerUser(@Body user: UserRegistrationRequest): UserRegistrationResponse
}