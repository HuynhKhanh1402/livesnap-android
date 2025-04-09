package dev.vku.livesnap.data

import dev.vku.livesnap.data.remote.ApiService
import dev.vku.livesnap.data.repository.DefaultUsersRepository
import dev.vku.livesnap.data.repository.UsersRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val usersRepository: UsersRepository
}

class DefaultAppContainer : AppContainer {
    private val baseUrl = "https://apilivesnap.vercel.app/api/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    override val usersRepository: UsersRepository by lazy {
        DefaultUsersRepository(apiService)
    }
}