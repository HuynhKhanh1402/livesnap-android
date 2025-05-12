package dev.vku.livesnap.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

interface FCMRepository {
    suspend fun getFCMToken(): String?
    suspend fun refreshFCMToken(): String
}

class DefaultFCMRepository(
) : FCMRepository {
    override suspend fun getFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e("FCMRepository", "Error getting FCM token: ${e.message}", e)
            null
        }
    }

    override suspend fun refreshFCMToken(): String {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
            val token = FirebaseMessaging.getInstance().token.await()
            return token
        } catch (e: Exception) {
            throw RuntimeException("Error refreshing FCM token", e)
        }
    }


} 