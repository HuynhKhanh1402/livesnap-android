package dev.vku.livesnap.ui.screen.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthSelectViewModel @Inject constructor(
    val tokenManager: TokenManager
) : ViewModel() {
    fun isAuthenticated(): Boolean {
        return runBlocking {
            return@runBlocking tokenManager.getToken() != null
        }
    }
}