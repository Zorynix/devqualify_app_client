package com.diploma.work.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.edit
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AppSession(private val context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
        
    private val _avatarUrlFlow = MutableStateFlow<String?>(null)
    private val _usernameFlow = MutableStateFlow<String?>(null)
    
    init {
        _avatarUrlFlow.value = getAvatarUrl()
        _usernameFlow.value = getUsername()
    }

    fun storeToken(token: String) {
        sharedPrefs.edit() { putString("access_token", token) }
    }

    fun getToken(): String? {
        return sharedPrefs.getString("access_token", null)
    }

    fun clearToken() {
        sharedPrefs.edit() { 
            remove("access_token")
            remove("user_id")
            remove("username") 
            remove("avatar_url")
            remove("avatar_data")
        }
        _avatarUrlFlow.value = null
        _usernameFlow.value = null
    }

    fun setTheme(isDark: Boolean) {
        sharedPrefs.edit() { putBoolean("is_dark_theme", isDark) }
    }

    fun getTheme(): Boolean {
        return sharedPrefs.getBoolean("is_dark_theme", false)
    }
    
    fun storeUserId(userId: Long) {
        sharedPrefs.edit() { putLong("user_id", userId) }
    }
    
    fun getUserId(): Long? {
        if (!sharedPrefs.contains("user_id")) {
            return null
        }
        return sharedPrefs.getLong("user_id", -1)
    }
    
    fun storeAvatarUrl(avatarUrl: String) {
        sharedPrefs.edit() { putString("avatar_url", avatarUrl) }
        _avatarUrlFlow.value = avatarUrl
    }
    
    fun getAvatarUrl(): String? {
        val avatarData = getAvatarData()
        if (avatarData != null) {
            return "data:avatar"
        }
        
        return sharedPrefs.getString("avatar_url", null)
    }
    
    suspend fun storeAvatarImage(uri: Uri) {
        try {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val byteArray = outputStream.toByteArray()
                        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                        
                        sharedPrefs.edit { putString("avatar_data", base64String) }
                        Logger.d("Avatar image stored as base64 string")
                        
                        _avatarUrlFlow.value = "data:avatar"
                    }
                } catch (e: Exception) {
                    Logger.e("Error processing avatar image: ${e.message}")
                    throw e
                }
            }
        } catch (e: Exception) {
            Logger.e("Error storing avatar image: ${e.message}")
        }
    }
    
    fun getAvatarData(): String? {
        return sharedPrefs.getString("avatar_data", null)
    }
    
    fun getAvatarBitmap(): Bitmap? {
        val base64String = getAvatarData() ?: return null
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Logger.e("Error decoding avatar image: ${e.message}")
            return null
        }
    }
    
    fun observeAvatarUrl(): StateFlow<String?> {
        return _avatarUrlFlow
    }
    
    fun storeUsername(username: String) {
        sharedPrefs.edit() { putString("username", username) }
        _usernameFlow.value = username
    }
    
    fun getUsername(): String? {
        return sharedPrefs.getString("username", null)
    }
    
    fun observeUsername(): StateFlow<String?> {
        return _usernameFlow
    }
}