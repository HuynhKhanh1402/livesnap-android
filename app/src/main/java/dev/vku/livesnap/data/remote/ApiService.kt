package dev.vku.livesnap.data.remote

import dev.vku.livesnap.data.remote.dto.request.CheckEmailExistRequest
import dev.vku.livesnap.data.remote.dto.request.CheckUsernameExistRequest
import dev.vku.livesnap.data.remote.dto.request.LoginRequest
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.response.CheckEmailExistResponse
import dev.vku.livesnap.data.remote.dto.response.CheckUsernameExistResponse
import dev.vku.livesnap.data.remote.dto.response.LoginResponse
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun registerUser(@Body user: UserRegistrationRequest): UserRegistrationResponse

    @POST("users/check-email-exist")
    suspend fun checkEmailExist(@Body request: CheckEmailExistRequest): CheckEmailExistResponse

    @POST("users/check-username-exist")
    suspend fun checkUsernameExist(@Body request: CheckUsernameExistRequest): CheckUsernameExistResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}