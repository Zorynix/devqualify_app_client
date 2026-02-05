package com.diploma.work.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.data.models.DeliveryFrequency
import com.diploma.work.data.models.UserPreferences
import com.diploma.work.utils.Constants
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_TECHNOLOGY_IDS = "technology_ids"
        private const val KEY_DIRECTIONS = "directions"
        private const val KEY_DELIVERY_FREQUENCY = "delivery_frequency"
        private const val KEY_EMAIL_NOTIFICATIONS = "email_notifications"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
        private const val KEY_ARTICLES_PER_DAY = "articles_per_day"
        private const val TAG = "UserPreferencesManager"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _usernameFlow = MutableStateFlow<String?>(null)
    val usernameFlow: StateFlow<String?> = _usernameFlow

    init {
        _usernameFlow.value = getUsername()
    }

    fun storeUsername(username: String) {
        prefs.edit { putString(KEY_USERNAME, username) }
        _usernameFlow.value = username
        logDebug("Username stored")
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun refreshUsername() {
        _usernameFlow.value = getUsername()
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit { putBoolean(KEY_IS_DARK_THEME, isDark) }
    }

    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_IS_DARK_THEME, false)

    fun storeUserPreferences(preferences: UserPreferences) {
        prefs.edit {
            putStringSet(
                KEY_TECHNOLOGY_IDS,
                preferences.technologyIds.map { it.toString() }.toSet()
            )
            putStringSet(
                KEY_DIRECTIONS,
                preferences.directions.map { it.name }.toSet()
            )
            putString(KEY_DELIVERY_FREQUENCY, preferences.deliveryFrequency.name)
            putBoolean(KEY_EMAIL_NOTIFICATIONS, preferences.emailNotifications)
            putBoolean(KEY_PUSH_NOTIFICATIONS, preferences.pushNotifications)
            putInt(KEY_ARTICLES_PER_DAY, preferences.articlesPerDay)
        }
        logDebug("User preferences stored")
    }

    fun getUserPreferences(userId: Long): UserPreferences? {
        if (!prefs.contains(KEY_TECHNOLOGY_IDS)) {
            return null
        }

        return try {
            val technologyIds = prefs.getStringSet(KEY_TECHNOLOGY_IDS, emptySet())
                ?.mapNotNull { it.toLongOrNull() } ?: emptyList()

            val directions = prefs.getStringSet(KEY_DIRECTIONS, emptySet())
                ?.mapNotNull { directionName ->
                    try {
                        ArticleDirection.valueOf(directionName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: emptyList()

            val deliveryFrequencyString = prefs.getString(KEY_DELIVERY_FREQUENCY, "WEEKLY") ?: "WEEKLY"
            val deliveryFrequency = try {
                DeliveryFrequency.valueOf(deliveryFrequencyString)
            } catch (e: IllegalArgumentException) {
                DeliveryFrequency.WEEKLY
            }

            UserPreferences(
                userId = userId,
                technologyIds = technologyIds,
                directions = directions,
                deliveryFrequency = deliveryFrequency,
                emailNotifications = prefs.getBoolean(KEY_EMAIL_NOTIFICATIONS, true),
                pushNotifications = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true),
                excludedSources = emptyList(),
                articlesPerDay = prefs.getInt(KEY_ARTICLES_PER_DAY, 5),
                updatedAt = Instant.now()
            )
        } catch (e: Exception) {
            logDebug("Error parsing user preferences: ${e.message}")
            null
        }
    }

    fun clearUserPreferences() {
        prefs.edit {
            remove(KEY_TECHNOLOGY_IDS)
            remove(KEY_DIRECTIONS)
            remove(KEY_DELIVERY_FREQUENCY)
            remove(KEY_EMAIL_NOTIFICATIONS)
            remove(KEY_PUSH_NOTIFICATIONS)
            remove(KEY_ARTICLES_PER_DAY)
        }
        logDebug("User preferences cleared")
    }

    fun clearAll() {
        prefs.edit { clear() }
        _usernameFlow.value = null
        logDebug("All preferences cleared")
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$TAG: $message")
        }
    }
}
