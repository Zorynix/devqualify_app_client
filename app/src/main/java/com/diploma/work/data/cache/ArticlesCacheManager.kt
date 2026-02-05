package com.diploma.work.data.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.LruCache
import androidx.core.content.edit
import com.diploma.work.data.models.Article
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ArticlesCacheManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "articles_cache_prefs"
        private const val KEY_VIEWED_ARTICLES = "viewed_articles"
        private const val KEY_LIKED_ARTICLES = "liked_articles"
        private const val KEY_DISLIKED_ARTICLES = "disliked_articles"
        private const val MAX_CACHE_SIZE = 100
        private const val TAG = "ArticlesCacheManager"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val articlesCache = object : LruCache<Long, Article>(MAX_CACHE_SIZE) {
        override fun sizeOf(key: Long, value: Article): Int = 1
    }

    fun markArticleAsViewed(articleId: Long) {
        val viewedArticles = getViewedArticles().toMutableSet()
        viewedArticles.add(articleId)
        prefs.edit {
            putStringSet(KEY_VIEWED_ARTICLES, viewedArticles.map { it.toString() }.toSet())
        }
        logDebug("Marked article $articleId as viewed. Total viewed: ${viewedArticles.size}")
    }

    fun getViewedArticles(): Set<Long> {
        return prefs.getStringSet(KEY_VIEWED_ARTICLES, emptySet())
            ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    }

    fun isArticleViewed(articleId: Long): Boolean {
        return getViewedArticles().contains(articleId)
    }

    fun likeArticle(articleId: Long) {
        val likedArticles = getLikedArticles().toMutableSet()
        val dislikedArticles = getDislikedArticles().toMutableSet()

        likedArticles.add(articleId)
        dislikedArticles.remove(articleId)

        prefs.edit {
            putStringSet(KEY_LIKED_ARTICLES, likedArticles.map { it.toString() }.toSet())
            putStringSet(KEY_DISLIKED_ARTICLES, dislikedArticles.map { it.toString() }.toSet())
        }
        logDebug("Liked article $articleId")
    }

    fun dislikeArticle(articleId: Long) {
        val likedArticles = getLikedArticles().toMutableSet()
        val dislikedArticles = getDislikedArticles().toMutableSet()

        likedArticles.remove(articleId)
        dislikedArticles.add(articleId)

        prefs.edit {
            putStringSet(KEY_LIKED_ARTICLES, likedArticles.map { it.toString() }.toSet())
            putStringSet(KEY_DISLIKED_ARTICLES, dislikedArticles.map { it.toString() }.toSet())
        }
        logDebug("Disliked article $articleId")
    }

    fun removeLikeDislike(articleId: Long) {
        val likedArticles = getLikedArticles().toMutableSet()
        val dislikedArticles = getDislikedArticles().toMutableSet()

        likedArticles.remove(articleId)
        dislikedArticles.remove(articleId)

        prefs.edit {
            putStringSet(KEY_LIKED_ARTICLES, likedArticles.map { it.toString() }.toSet())
            putStringSet(KEY_DISLIKED_ARTICLES, dislikedArticles.map { it.toString() }.toSet())
        }
        logDebug("Removed like/dislike for article $articleId")
    }

    fun getLikedArticles(): Set<Long> {
        return prefs.getStringSet(KEY_LIKED_ARTICLES, emptySet())
            ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    }

    fun getDislikedArticles(): Set<Long> {
        return prefs.getStringSet(KEY_DISLIKED_ARTICLES, emptySet())
            ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    }


    fun getArticleLikeStatus(articleId: Long): Boolean? {
        return when {
            getLikedArticles().contains(articleId) -> true
            getDislikedArticles().contains(articleId) -> false
            else -> null
        }
    }

    fun cacheArticle(article: Article) {
        articlesCache.put(article.id, article)
        logDebug("Cached article ${article.id}. Cache size: ${articlesCache.size()}")
    }

    fun cacheArticles(articles: List<Article>) {
        articles.forEach { article ->
            articlesCache.put(article.id, article)
        }
        logDebug("Cached ${articles.size} articles. Cache size: ${articlesCache.size()}")
    }

    fun getCachedArticle(articleId: Long): Article? {
        return articlesCache.get(articleId)
    }

    fun getCachedArticles(articleIds: Set<Long>): List<Article> {
        return articleIds.mapNotNull { id -> articlesCache.get(id) }
    }

    fun getAllCachedArticles(): List<Article> {
        val snapshot = articlesCache.snapshot()
        return snapshot.values.toList()
    }

    fun clearArticlesCache() {
        articlesCache.evictAll()
        logDebug("Articles cache cleared")
    }

    fun getCacheSize(): Int = articlesCache.size()

    fun getMaxCacheSize(): Int = MAX_CACHE_SIZE

    fun clearViewsAndRatings() {
        prefs.edit {
            remove(KEY_VIEWED_ARTICLES)
            remove(KEY_LIKED_ARTICLES)
            remove(KEY_DISLIKED_ARTICLES)
        }
        clearArticlesCache()
        logDebug("All views and ratings cleared")
    }

    fun clearAll() {
        prefs.edit { clear() }
        clearArticlesCache()
        logDebug("All article cache data cleared")
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$TAG: $message")
        }
    }
}
