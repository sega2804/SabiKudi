package com.crypticsamsara.sabikudi.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sabikudi.shared.dto.AccessLevel
import com.sabikudi.shared.dto.AuthResponse
import com.sabikudi.shared.dto.MeResponse
import com.sabikudi.shared.dto.OtpChannel
import com.sabikudi.shared.dto.OtpRequest
import com.sabikudi.shared.dto.OtpRequestResponse
import com.sabikudi.shared.dto.OtpVerifyRequest
import com.sabikudi.shared.dto.RefreshRequest
import com.sabikudi.shared.dto.TokenPair
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = try {
        EncryptedSharedPreferences.create(
            context,
            "sabikudi_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        context.getSharedPreferences("sabikudi_auth_prefs_fallback", Context.MODE_PRIVATE)
    }

    private val refreshMutex = Mutex()

    private var inMemoryAccessToken: String? = null

    private val _accessLevel = MutableStateFlow<AccessLevel?>(null)
    val accessLevel: StateFlow<AccessLevel?> = _accessLevel.asStateFlow()

    val installationId: String
        get() {
            var id = prefs.getString("KEY_INSTALLATION_ID", null)
            if (id == null) {
                id = UUID.randomUUID().toString()
                prefs.edit().putString("KEY_INSTALLATION_ID", id).apply()
            }
            return id
        }

    fun getAccessToken(): String? = inMemoryAccessToken

    fun getRefreshToken(): String? = prefs.getString("KEY_REFRESH_TOKEN", null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        inMemoryAccessToken = accessToken
        prefs.edit().putString("KEY_REFRESH_TOKEN", refreshToken).apply()
    }

    fun clearTokens() {
        inMemoryAccessToken = null
        prefs.edit().remove("KEY_REFRESH_TOKEN").apply()
        _accessLevel.value = null
    }

    /** Single-flight token refresh guarded by Mutex to prevent reuse detection race conditions. */
    suspend fun refreshTokenSingleFlight(refreshCall: suspend (RefreshRequest) -> TokenPair): Result<TokenPair> {
        return refreshMutex.withLock {
            val currentRefreshToken = getRefreshToken()
                ?: return Result.failure(IllegalStateException("No refresh token available"))

            try {
                val newTokens = refreshCall(RefreshRequest(currentRefreshToken, installationId))
                saveTokens(newTokens.accessToken, newTokens.refreshToken)
                Result.success(newTokens)
            } catch (e: Exception) {
                clearTokens()
                Result.failure(e)
            }
        }
    }

    fun updateAccessLevel(level: AccessLevel?) {
        _accessLevel.value = level
    }
}
