package dev.vku.livesnap.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.remote.AuthInterceptor
import dev.vku.livesnap.data.repository.DefaultSnapRepository
import dev.vku.livesnap.data.repository.DefaultUsersRepository
import dev.vku.livesnap.data.repository.SnapRepository
import dev.vku.livesnap.data.repository.UsersRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:3000/api/")
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
    fun proveSnapRepository(apiService: ApiService): SnapRepository {
        return DefaultSnapRepository(apiService)
    }
}
