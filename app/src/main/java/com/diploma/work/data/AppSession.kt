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
import com.diploma.work.data.models.Article
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.data.models.DeliveryFrequency
import com.diploma.work.utils.Constants

class AppSession(private val context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(Constants.PrefsKeys.APP_SESSION, Context.MODE_PRIVATE)
        
    private val _avatarUrlFlow = MutableStateFlow<String?>(null)
    private val _usernameFlow = MutableStateFlow<String?>(null)
    private val _tokenFlow = MutableStateFlow<String?>(null)
    
    private val articlesCache = mutableMapOf<Long, Article>()
    
    init {
        _avatarUrlFlow.value = getAvatarUrl()
        _usernameFlow.value = getUsername()
        _tokenFlow.value = getToken()
    }    
    fun storeToken(token: String) {
        sharedPrefs.edit {
            putString(Constants.PrefsKeys.ACCESS_TOKEN, token)
        }
        _tokenFlow.value = token
    }

    fun getToken(): String? {
        return sharedPrefs.getString(Constants.PrefsKeys.ACCESS_TOKEN, null)
    }
    
    fun observeToken(): StateFlow<String?> {
        return _tokenFlow

    }

fun clearToken() {
    sharedPrefs.edit {
        remove(Constants.PrefsKeys.ACCESS_TOKEN)
        remove(Constants.PrefsKeys.USER_ID)
        remove(Constants.PrefsKeys.USERNAME)
        remove(Constants.PrefsKeys.AVATAR_URL)
        remove(Constants.PrefsKeys.AVATAR_DATA)
    }
    clearUserPreferences()
    clearViewsAndRatings()
    _avatarUrlFlow.value = null
    _usernameFlow.value = null
    _tokenFlow.value = null
}    
fun setTheme(isDark: Boolean) {
        sharedPrefs.edit {
            putBoolean("is_dark_theme", isDark)
        }
    }

    fun getTheme(): Boolean {
        return sharedPrefs.getBoolean("is_dark_theme", false)
    }
    
    fun storeUserId(userId: Long) {
        sharedPrefs.edit {
            putLong(Constants.PrefsKeys.USER_ID, userId)
        }
    }
    
    fun getUserId(): Long? {
        if (!sharedPrefs.contains(Constants.PrefsKeys.USER_ID)) {
            return null
        }
        return sharedPrefs.getLong(Constants.PrefsKeys.USER_ID, -1)
    }
    
    fun storeAvatarUrl(avatarUrl: String) {
        sharedPrefs.edit {
            putString(Constants.PrefsKeys.AVATAR_URL, avatarUrl)
        }
        _avatarUrlFlow.value = avatarUrl
    }
    
    fun getAvatarUrl(): String? {
        val avatarData = getAvatarData()
        if (avatarData != null) {
            return "data:avatar"
        }
        
        return sharedPrefs.getString(Constants.PrefsKeys.AVATAR_URL, null)
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

                        sharedPrefs.edit {
                            putString(Constants.PrefsKeys.AVATAR_DATA, base64String)
                        }
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
        return sharedPrefs.getString(Constants.PrefsKeys.AVATAR_DATA, null)
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
    
    fun refreshAvatarUrl() {
        _avatarUrlFlow.value = getAvatarUrl()
    }
    
    fun refreshUsername() {
        _usernameFlow.value = getUsername()
    }
    
    fun storeUsername(username: String) {
        sharedPrefs.edit {
            putString(Constants.PrefsKeys.USERNAME, username)
        }
        _usernameFlow.value = username
    }
    
    fun getUsername(): String? {
        return sharedPrefs.getString(Constants.PrefsKeys.USERNAME, null)
    }
    
    fun observeUsername(): StateFlow<String?> {
        return _usernameFlow
    }
    
    fun storeUserPreferences(preferences: UserPreferences) {
        sharedPrefs.edit {
            putLong(Constants.PrefsKeys.USER_ID, preferences.userId)
            putStringSet(
                Constants.PrefsKeys.TECHNOLOGY_IDS,
                preferences.technologyIds.map { it.toString() }.toSet()
            )
            putStringSet(
                Constants.PrefsKeys.DIRECTIONS,
                preferences.directions.map { it.name }.toSet()
            )
            putString(Constants.PrefsKeys.DELIVERY_FREQUENCY, preferences.deliveryFrequency.name)
            putBoolean(Constants.PrefsKeys.EMAIL_NOTIFICATIONS, preferences.emailNotifications)
            putBoolean(Constants.PrefsKeys.PUSH_NOTIFICATIONS, preferences.pushNotifications)
            putInt(Constants.PrefsKeys.ARTICLES_PER_DAY, preferences.articlesPerDay)
        }
    }
    
    fun getUserPreferences(): UserPreferences? {
        if (!sharedPrefs.contains(Constants.PrefsKeys.TECHNOLOGY_IDS)) {
            return null
        }
        
        return try {
            val technologyIds = sharedPrefs.getStringSet(Constants.PrefsKeys.TECHNOLOGY_IDS, emptySet())
                ?.mapNotNull { it.toLongOrNull() } ?: emptyList()
            
            val directions = sharedPrefs.getStringSet(Constants.PrefsKeys.DIRECTIONS, emptySet())
                ?.mapNotNull { directionName ->
                    try {
                        ArticleDirection.valueOf(directionName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } ?: emptyList()
            
            val deliveryFrequencyString = sharedPrefs.getString(Constants.PrefsKeys.DELIVERY_FREQUENCY, "WEEKLY") ?: "WEEKLY"
            val deliveryFrequency = try {
                DeliveryFrequency.valueOf(deliveryFrequencyString)
            } catch (e: IllegalArgumentException) {
                DeliveryFrequency.WEEKLY
            }
            
            val emailNotifications = sharedPrefs.getBoolean(Constants.PrefsKeys.EMAIL_NOTIFICATIONS, true)
            val pushNotifications = sharedPrefs.getBoolean(Constants.PrefsKeys.PUSH_NOTIFICATIONS, true)
            val articlesPerDay = sharedPrefs.getInt(Constants.PrefsKeys.ARTICLES_PER_DAY, 5)
            
            UserPreferences(
                userId = getUserId() ?: -1L,
                technologyIds = technologyIds,
                directions = directions,
                deliveryFrequency = deliveryFrequency,
                emailNotifications = emailNotifications,
                pushNotifications = pushNotifications,
                excludedSources = emptyList(),
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
            remove(Constants.PrefsKeys.TECHNOLOGY_IDS)
            remove(Constants.PrefsKeys.DIRECTIONS)
            remove(Constants.PrefsKeys.DELIVERY_FREQUENCY)
            remove(Constants.PrefsKeys.EMAIL_NOTIFICATIONS)
            remove(Constants.PrefsKeys.PUSH_NOTIFICATIONS)
            remove(Constants.PrefsKeys.ARTICLES_PER_DAY)
        }
    }
    
    fun markArticleAsViewed(articleId: Long) {
        val viewedArticles = getViewedArticles().toMutableSet()
        viewedArticles.add(articleId)
        sharedPrefs.edit {
            putStringSet(Constants.PrefsKeys.VIEWED_ARTICLES, viewedArticles.map { it.toString() }.toSet())
        }
        Logger.d("AppSession: Marked article $articleId as viewed. Total viewed: ${viewedArticles.size}")
    }
    
    fun getViewedArticles(): Set<Long> {
        val articles = sharedPrefs.getStringSet(Constants.PrefsKeys.VIEWED_ARTICLES, emptySet())
            ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        Logger.d("AppSession: getViewedArticles returning ${articles.size} articles: $articles")
        return articles
    }
    
    fun isArticleViewed(articleId: Long): Boolean {
        val isViewed = getViewedArticles().contains(articleId)
        Logger.d("AppSession: Article $articleId viewed status: $isViewed")
        return isViewed
    }
    
    fun likeArticle(articleId: Long) {
        val likedArticles = getLikedArticles().toMutableSet()
        val dislikedArticles = getDislikedArticles().toMutableSet()
        
        likedArticles.add(articleId)
        dislikedArticles.remove(articleId)
        
        sharedPrefs.edit {
            putStringSet(Constants.PrefsKeys.LIKED_ARTICLES, likedArticles.map { it.toString() }.toSet())
            putStringSet(Constants.PrefsKeys.DISLIKED_ARTICLES, dislikedArticles.map { it.toString() }.toSet())
        }
    }
    
    fun dislikeArticle(articleId: Long) {
        val likedArticles = getLikedArticles().toMutableSet()
        val dislikedArticles = getDislikedArticles().toMutableSet()
        
        likedArticles.remove(articleId)
        dislikedArticles.add(articleId)
        
        sharedPrefs.edit {
            putStringSet(Constants.PrefsKeys.LIKED_ARTICLES, likedArticles.map { it.toString() }.toSet())
            putStringSet(Constants.PrefsKeys.DISLIKED_ARTICLES, dislikedArticles.map { it.toString() }.toSet())
        }
    }
    
    fun removeLikeDislike(articleId: Long) {
        val likedArticles = getLikedArticles().toMutableSet()
        val dislikedArticles = getDislikedArticles().toMutableSet()
        
        likedArticles.remove(articleId)
        dislikedArticles.remove(articleId)
        
        sharedPrefs.edit {
            putStringSet(Constants.PrefsKeys.LIKED_ARTICLES, likedArticles.map { it.toString() }.toSet())
            putStringSet(Constants.PrefsKeys.DISLIKED_ARTICLES, dislikedArticles.map { it.toString() }.toSet())
        }
    }
    
    fun getLikedArticles(): Set<Long> {
        val articles = sharedPrefs.getStringSet(Constants.PrefsKeys.LIKED_ARTICLES, emptySet())
            ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        Logger.d("AppSession: getLikedArticles returning ${articles.size} articles: $articles")
        return articles
    }
    
    fun getDislikedArticles(): Set<Long> {
        val articles = sharedPrefs.getStringSet(Constants.PrefsKeys.DISLIKED_ARTICLES, emptySet())
            ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        Logger.d("AppSession: getDislikedArticles returning ${articles.size} articles: $articles")
        return articles
    }
    
    fun getArticleLikeStatus(articleId: Long): Boolean? {
        return when {
            getLikedArticles().contains(articleId) -> true
            getDislikedArticles().contains(articleId) -> false
            else -> null
        }
    }
    
    fun clearViewsAndRatings() {
        sharedPrefs.edit {
            remove(Constants.PrefsKeys.VIEWED_ARTICLES)
            remove(Constants.PrefsKeys.LIKED_ARTICLES)
            remove(Constants.PrefsKeys.DISLIKED_ARTICLES)
        }
        articlesCache.clear()
    }
    
    fun cacheArticles(articles: List<Article>) {
        articles.forEach { article ->
            articlesCache[article.id] = article
        }
        Logger.d("AppSession: Cached ${articles.size} articles. Total in cache: ${articlesCache.size}")
    }
    
    fun getCachedArticle(articleId: Long): Article? {
        return articlesCache[articleId]
    }
    
    fun getCachedArticles(articleIds: Set<Long>): List<Article> {
        return articleIds.mapNotNull { id -> articlesCache[id] }
    }
    
    fun getAllCachedArticles(): List<Article> {
        return articlesCache.values.toList()
    }
    
    fun clearArticlesCache() {
        articlesCache.clear()
        Logger.d("AppSession: Articles cache cleared")
    }
}