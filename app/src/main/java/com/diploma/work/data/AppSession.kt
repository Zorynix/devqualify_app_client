package com.diploma.work.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppSession(context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    fun storeToken(token: String) {
        sharedPrefs.edit() { putString("access_token", token) }
    }

    fun getToken(): String? {
        return sharedPrefs.getString("access_token", null)
    }

    fun clearToken() {
        sharedPrefs.edit() { remove("access_token") }
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
}