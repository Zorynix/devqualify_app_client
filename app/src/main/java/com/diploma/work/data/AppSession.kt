package com.diploma.work.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.diploma.work.data.cache.ArticlesCacheManager
import com.diploma.work.data.models.Article
import com.diploma.work.data.models.UserPreferences
import com.diploma.work.data.preferences.UserPreferencesManager
import com.diploma.work.data.security.AvatarManager
import com.diploma.work.data.security.SecureTokenManager
import com.diploma.work.utils.SecureLogger
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppSession @Inject constructor(
    private val context: Context,
    private val secureTokenManager: SecureTokenManager,
    private val avatarManager: AvatarManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val articlesCacheManager: ArticlesCacheManager
) {
    companion object {
        private const val TAG = "AppSession"
    }

    fun storeToken(token: String) {
        secureTokenManager.storeAccessToken(token)
        SecureLogger.sensitive(TAG, "Token stored: ${SecureLogger.maskToken(token)}")
    }

    fun getToken(): String? = secureTokenManager.getAccessToken()

    fun observeToken(): StateFlow<String?> = secureTokenManager.observeToken()

    fun clearToken() {
        secureTokenManager.clearAll()
        userPreferencesManager.clearAll()
        articlesCacheManager.clearAll()
        SecureLogger.d(TAG, "Session cleared")
    }

    fun storeUserId(userId: Long) {
        secureTokenManager.storeUserId(userId)
        SecureLogger.sensitive(TAG, "User ID stored: ${SecureLogger.maskUserId(userId)}")
    }

    fun getUserId(): Long? = secureTokenManager.getUserId()

    fun setTheme(isDark: Boolean) {
        userPreferencesManager.setDarkTheme(isDark)
    }

    fun getTheme(): Boolean = userPreferencesManager.isDarkTheme()

    fun storeAvatarUrl(avatarUrl: String) {
        avatarManager.refreshAvatarUrl()
    }

    fun getAvatarUrl(): String? = avatarManager.getAvatarPath()

    suspend fun storeAvatarImage(uri: Uri): Boolean {
        return avatarManager.storeAvatarFromUri(uri)
    }

    fun getAvatarData(): String? = null

    suspend fun getAvatarBitmap(): Bitmap? = avatarManager.getAvatarBitmap()

    fun observeAvatarUrl(): StateFlow<String?> = avatarManager.avatarUrlFlow

    fun refreshAvatarUrl() {
        avatarManager.refreshAvatarUrl()
    }

    fun storeUsername(username: String) {
        userPreferencesManager.storeUsername(username)
    }

    fun getUsername(): String? = userPreferencesManager.getUsername()

    fun observeUsername(): StateFlow<String?> = userPreferencesManager.usernameFlow

    fun refreshUsername() {
        userPreferencesManager.refreshUsername()
    }

    fun storeUserPreferences(preferences: UserPreferences) {
        userPreferencesManager.storeUserPreferences(preferences)
    }

    fun getUserPreferences(): UserPreferences? {
        val userId = getUserId() ?: return null
        return userPreferencesManager.getUserPreferences(userId)
    }

    fun clearUserPreferences() {
        userPreferencesManager.clearUserPreferences()
    }


    fun markArticleAsViewed(articleId: Long) {
        articlesCacheManager.markArticleAsViewed(articleId)
    }

    fun getViewedArticles(): Set<Long> = articlesCacheManager.getViewedArticles()

    fun isArticleViewed(articleId: Long): Boolean = articlesCacheManager.isArticleViewed(articleId)

    fun likeArticle(articleId: Long) {
        articlesCacheManager.likeArticle(articleId)
    }

    fun dislikeArticle(articleId: Long) {
        articlesCacheManager.dislikeArticle(articleId)
    }

    fun removeLikeDislike(articleId: Long) {
        articlesCacheManager.removeLikeDislike(articleId)
    }

    fun getLikedArticles(): Set<Long> = articlesCacheManager.getLikedArticles()

    fun getDislikedArticles(): Set<Long> = articlesCacheManager.getDislikedArticles()

    fun getArticleLikeStatus(articleId: Long): Boolean? = articlesCacheManager.getArticleLikeStatus(articleId)

    fun clearViewsAndRatings() {
        articlesCacheManager.clearViewsAndRatings()
    }

    fun cacheArticles(articles: List<Article>) {
        articlesCacheManager.cacheArticles(articles)
    }

    fun getCachedArticle(articleId: Long): Article? = articlesCacheManager.getCachedArticle(articleId)

    fun getCachedArticles(articleIds: Set<Long>): List<Article> = articlesCacheManager.getCachedArticles(articleIds)

    fun getAllCachedArticles(): List<Article> = articlesCacheManager.getAllCachedArticles()

    fun clearArticlesCache() {
        articlesCacheManager.clearArticlesCache()
    }
}