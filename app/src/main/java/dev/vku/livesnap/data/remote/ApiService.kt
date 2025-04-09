package dev.vku.livesnap.data.remote

import dev.vku.livesnap.data.remote.dto.UserDTO
import dev.vku.livesnap.data.remote.dto.UserRegistrationDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body user: UserRegistrationDTO): UserDTO
}