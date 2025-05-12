package dev.vku.livesnap.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Base64
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore("auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext internal val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }.first()
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
        }
    }

    suspend fun getCurrentUserId(): String? {
        val token = getToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedPayload = Base64.decode(payload, Base64.URL_SAFE)
            val jsonString = String(decodedPayload)
            val jsonObject = JSONObject(jsonString)
            
            jsonObject.getString("userId")
        } catch (e: Exception) {
            null
        }
    }
}
