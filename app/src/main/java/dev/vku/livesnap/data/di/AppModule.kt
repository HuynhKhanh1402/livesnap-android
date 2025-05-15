package dev.vku.livesnap.data.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.AuthInterceptor
import dev.vku.livesnap.data.repository.AuthRepository
import dev.vku.livesnap.data.repository.DefaultAuthRepository
import dev.vku.livesnap.data.repository.DefaultFCMRepository
import dev.vku.livesnap.data.repository.DefaultFriendRepository
import dev.vku.livesnap.data.repository.DefaultSnapRepository
import dev.vku.livesnap.data.repository.DefaultUsersRepository
import dev.vku.livesnap.data.repository.FirebaseMessageRepository
import dev.vku.livesnap.data.repository.FriendRepository
import dev.vku.livesnap.data.repository.FCMRepository
import dev.vku.livesnap.data.repository.SnapRepository
import dev.vku.livesnap.data.repository.UsersRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor { tokenManager.getToken() }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:3000/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UsersRepository {
        return DefaultUsersRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideSnapRepository(apiService: ApiService): SnapRepository {
        return DefaultSnapRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideFriendRepository(apiService: ApiService): FriendRepository {
        return DefaultFriendRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideFirebaseMessageRepository(tokenManager: TokenManager): FirebaseMessageRepository {
        return FirebaseMessageRepository(
            firestore =  FirebaseFirestore.getInstance(),
            tokenManager = tokenManager
        )
    }

    @Provides
    @Singleton
    fun provideFCMRepository(): FCMRepository {
        return DefaultFCMRepository()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, tokenManager: TokenManager): AuthRepository {
        return DefaultAuthRepository(apiService, tokenManager)
    }
}
