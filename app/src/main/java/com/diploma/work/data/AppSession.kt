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
import com.diploma.work.data.models.UserPreferences
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.data.models.DeliveryFrequency

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
    }    fun clearToken() {
        sharedPrefs.edit() { 
            remove("access_token")
            remove("user_id")
            remove("username") 
            remove("avatar_url")
            remove("avatar_data")
        }
        clearUserPreferences()
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
    
    fun storeUserPreferences(preferences: UserPreferences) {
        sharedPrefs.edit {
            putString("user_preferences_technology_ids", preferences.technologyIds.joinToString(","))
            putString("user_preferences_directions", preferences.directions.map { it.name }.joinToString(","))
            putString("user_preferences_delivery_frequency", preferences.deliveryFrequency.name)
            putBoolean("user_preferences_email_notifications", preferences.emailNotifications)
            putBoolean("user_preferences_push_notifications", preferences.pushNotifications)
            putInt("user_preferences_articles_per_day", preferences.articlesPerDay)
            putString("user_preferences_excluded_sources", preferences.excludedSources.joinToString(","))
        }
    }
    
    fun getUserPreferences(): UserPreferences? {
        if (!sharedPrefs.contains("user_preferences_technology_ids")) {
            return null
        }
        
        return try {
            val technologyIdsString = sharedPrefs.getString("user_preferences_technology_ids", "") ?: ""
            val directionsString = sharedPrefs.getString("user_preferences_directions", "") ?: ""
            val deliveryFrequencyString = sharedPrefs.getString("user_preferences_delivery_frequency", "WEEKLY") ?: "WEEKLY"
            val emailNotifications = sharedPrefs.getBoolean("user_preferences_email_notifications", true)
            val pushNotifications = sharedPrefs.getBoolean("user_preferences_push_notifications", true)
            val articlesPerDay = sharedPrefs.getInt("user_preferences_articles_per_day", 5)
            val excludedSourcesString = sharedPrefs.getString("user_preferences_excluded_sources", "") ?: ""
            
            val technologyIds = if (technologyIdsString.isNotEmpty()) {
                technologyIdsString.split(",").mapNotNull { it.toLongOrNull() }
            } else {
                emptyList()
            }
              val directions = if (directionsString.isNotEmpty()) {
                directionsString.split(",").mapNotNull { directionName ->
                    try {
                        ArticleDirection.valueOf(directionName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            } else {
                emptyList()
            }
            
            val deliveryFrequency = try {
                DeliveryFrequency.valueOf(deliveryFrequencyString)
            } catch (e: IllegalArgumentException) {
                DeliveryFrequency.WEEKLY
            }
            
            val excludedSources = if (excludedSourcesString.isNotEmpty()) {
                excludedSourcesString.split(",")
            } else {
                emptyList()
            }
              UserPreferences(
                userId = getUserId() ?: -1L,
                technologyIds = technologyIds,
                directions = directions,
                deliveryFrequency = deliveryFrequency,
                emailNotifications = emailNotifications,
                pushNotifications = pushNotifications,
                excludedSources = excludedSources,
                articlesPerDay = articlesPerDay,
                updatedAt = java.time.Instant.now()
            )
        } catch (e: Exception) {
            Logger.e("Error parsing user preferences: ${e.message}")
            null
        }
    }
    
    fun clearUserPreferences() {
        sharedPrefs.edit {
            remove("user_preferences_technology_ids")
            remove("user_preferences_directions")
            remove("user_preferences_delivery_frequency")
            remove("user_preferences_email_notifications")
            remove("user_preferences_push_notifications")
            remove("user_preferences_articles_per_day")
            remove("user_preferences_excluded_sources")
        }
    }
}