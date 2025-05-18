package dev.vku.livesnap.data.remote

import dev.vku.livesnap.data.remote.dto.request.CheckEmailExistRequest
import dev.vku.livesnap.data.remote.dto.request.CheckUsernameExistRequest
import dev.vku.livesnap.data.remote.dto.request.ForgotPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.LoginRequest
import dev.vku.livesnap.data.remote.dto.request.ReactSnapRequest
import dev.vku.livesnap.data.remote.dto.request.ResetPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateNameRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateEmailRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateFcmTokenRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateUsernameRequest
import dev.vku.livesnap.data.remote.dto.request.UpdateVisibilityRequest
import dev.vku.livesnap.data.remote.dto.request.FeedbackRequest
import dev.vku.livesnap.data.remote.dto.request.SendVerificationOtpRequest
import dev.vku.livesnap.data.remote.dto.request.UserRegistrationRequest
import dev.vku.livesnap.data.remote.dto.request.CheckPasswordRequest
import dev.vku.livesnap.data.remote.dto.request.VerifyOtpRequest
import dev.vku.livesnap.data.remote.dto.response.CheckEmailExistResponse
import dev.vku.livesnap.data.remote.dto.response.CheckUsernameExistResponse
import dev.vku.livesnap.data.remote.dto.response.DefaultResponse
import dev.vku.livesnap.data.remote.dto.response.FriendListResponse
import dev.vku.livesnap.data.remote.dto.response.FriendRequestListResponse
import dev.vku.livesnap.data.remote.dto.response.FriendSuggestionListDTO
import dev.vku.livesnap.data.remote.dto.response.LoginResponse
import dev.vku.livesnap.data.remote.dto.response.SnapResponse
import dev.vku.livesnap.data.remote.dto.response.SnapsResponse
import dev.vku.livesnap.data.remote.dto.response.UploadSnapResponse
import dev.vku.livesnap.data.remote.dto.response.UserDetailResponse
import dev.vku.livesnap.data.remote.dto.response.UserListResponse
import dev.vku.livesnap.data.remote.dto.response.UserRegistrationResponse
import dev.vku.livesnap.data.remote.dto.response.PaymentQRResponse
import dev.vku.livesnap.data.remote.dto.response.FeedbackHistoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun registerUser(@Body user: UserRegistrationRequest): Response<UserRegistrationResponse>

    @POST("users/check-email-exist")
    suspend fun checkEmailExist(@Body request: CheckEmailExistRequest): CheckEmailExistResponse

    @POST("users/check-username-exist")
    suspend fun checkUsernameExist(@Body request: CheckUsernameExistRequest): CheckUsernameExistResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/send-verification-otp")
    suspend fun sendVerificationOtp(@Body request: SendVerificationOtpRequest): Response<DefaultResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<DefaultResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<DefaultResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<DefaultResponse>

    @GET("users/detail")
    suspend fun fetchUserDetail(): Response<UserDetailResponse>

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserDetailResponse>

    @Multipart
    @POST("users/set-avatar")
    suspend fun setAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<Unit>

    @PATCH("users/update-name")
    suspend fun updateName(
        @Body request: UpdateNameRequest
    ): Response<Unit>

    @GET("users/search")
    suspend fun searchUsers(@Query("username") query: String): Response<UserListResponse>

    @GET("snaps/load")
    suspend fun fetchSnaps(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("userId") userId: String? = null
    ): Response<SnapsResponse>

    @Multipart
    @POST("snaps/upload")
    suspend fun uploadSnap(
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody
    ): Response<UploadSnapResponse>

    @DELETE("snaps/delete/{snapId}")
    suspend fun deleteSnap(@Path("snapId") snapId: String): Response<Unit>

    @POST("snaps/react")
    suspend fun reactSnap(@Body request: ReactSnapRequest) : Response<DefaultResponse>

    @GET("snaps/{snapId}")
    suspend fun fetchSnap(@Path("snapId") snapId: String): Response<SnapResponse>

    @GET("friends/list")
    suspend fun fetchFriendList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<FriendListResponse>

    @POST("friends/request/{userId}")
    suspend fun sendFriendRequest(@Path("userId") userId: String): Response<DefaultResponse>

    @GET("friends/request/incoming")
    suspend fun fetchIncomingRequestList(): Response<FriendRequestListResponse>

    @GET("friends/request/outgoing")
    suspend fun fetchOutgoingRequestList(): Response<FriendRequestListResponse>

    @POST("friends/accept/{requestId}")
    suspend fun acceptFriendRequest(@Path("requestId") requestId: String): Response<DefaultResponse>

    @POST("fiends/reject/{userId}")
    suspend fun rejectFriendRequest(@Path("userId") userId: String): Response<DefaultResponse>

    @DELETE("friends/remove/{userId}")
    suspend fun removeFriend(@Path("userId") userId: String): Response<DefaultResponse>

    @DELETE("friends/cancel-request/{userId}")
    suspend fun cancelFriendRequest(@Path("userId") userId: String): Response<DefaultResponse>

    @GET("friends/suggestions")
    suspend fun fetchFriendSuggestions(): Response<FriendSuggestionListDTO>

    @POST("users/check-password")
    suspend fun checkPassword(@Body request: CheckPasswordRequest): Response<DefaultResponse>

    @PATCH("users/update-email")
    suspend fun updateEmail(@Body request: UpdateEmailRequest): Response<DefaultResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("users/fcm-token")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest): Response<DefaultResponse>

    @PATCH("users/update-username")
    suspend fun updateUsername(@Body request: UpdateUsernameRequest): Response<DefaultResponse>

    @GET("users/payment-qr")
    suspend fun getPaymentQR(): Response<PaymentQRResponse>

    @POST("users/send-feedback")
    suspend fun sendFeedback(@Body request: FeedbackRequest): Response<DefaultResponse>

    @GET("users/feedback-history")
    suspend fun getFeedbackHistory(): Response<FeedbackHistoryResponse>

    @PATCH("users/update-visibility")
    suspend fun updateVisibility(@Body request: UpdateVisibilityRequest): Response<DefaultResponse>
}