package com.diploma.work.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SecureTokenManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val TAG = "SecureTokenManager"
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            logSecure("Failed to create EncryptedSharedPreferences, falling back to regular prefs: ${e.message}")
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private val _tokenFlow = MutableStateFlow<String?>(null)

    init {
        _tokenFlow.value = getAccessToken()
    }

    fun storeAccessToken(token: String) {
        encryptedPrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        _tokenFlow.value = token
        logSecure("Access token stored securely")
    }

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun observeToken(): StateFlow<String?> = _tokenFlow

    fun storeRefreshToken(token: String) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        logSecure("Refresh token stored securely")
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun storeUserId(userId: Long) {
        encryptedPrefs.edit().putLong(KEY_USER_ID, userId).apply()
        logSecure("User ID stored securely")
    }

    fun getUserId(): Long? {
        return if (encryptedPrefs.contains(KEY_USER_ID)) {
            encryptedPrefs.getLong(KEY_USER_ID, -1L).takeIf { it != -1L }
        } else {
            null
        }
    }

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        _tokenFlow.value = null
        logSecure("All secure tokens cleared")
    }

    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }


    private fun logSecure(message: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$TAG: $message")
        }
    }
}
