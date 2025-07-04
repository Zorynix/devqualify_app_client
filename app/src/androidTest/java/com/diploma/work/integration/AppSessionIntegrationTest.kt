package com.diploma.work.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class AppSessionIntegrationTest {
    
    private lateinit var appSession: AppSession
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        appSession = AppSession(context)
        appSession.clearToken()
    }
    
    @Test
    fun appSession_tokenStorageFlow() {
        val token = "test_access_token"
        
        assertNull(appSession.getToken())
        
        appSession.storeToken(token)
        
        assertEquals(token, appSession.getToken())
        
        appSession.clearToken()
        
        assertNull(appSession.getToken())
    }
    
    @Test
    fun appSession_userIdStorageFlow() {
        val userId = 123L
        
        assertNull(appSession.getUserId())
        
        appSession.storeUserId(userId)
        
        assertEquals(userId, appSession.getUserId())
        
        appSession.clearToken()
        
        assertNull(appSession.getUserId())
    }
    
    @Test
    fun appSession_usernameStorageFlow() {
        val username = "testuser"
        
        assertNull(appSession.getUsername())
        
        appSession.storeUsername(username)
        
        assertEquals(username, appSession.getUsername())
        
        appSession.clearToken()
        
        assertNull(appSession.getUsername())
    }
    
    @Test
    fun appSession_userPreferencesStorageFlow() {
        val preferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(1L, 2L, 3L),
            directions = listOf(ArticleDirection.BACKEND, ArticleDirection.FRONTEND),
            deliveryFrequency = DeliveryFrequency.WEEKLY,
            emailNotifications = true,
            pushNotifications = false,
            excludedSources = listOf("source1", "source2"),
            articlesPerDay = 5,
            updatedAt = Instant.now()
        )
        
        assertNull(appSession.getUserPreferences())
        
        appSession.storeUserPreferences(preferences)
        
        val storedPreferences = appSession.getUserPreferences()
        
        assertNotNull(storedPreferences)
        assertEquals(preferences.userId, storedPreferences!!.userId)
        assertEquals(preferences.technologyIds, storedPreferences.technologyIds)
        assertEquals(preferences.directions, storedPreferences.directions)
        assertEquals(preferences.deliveryFrequency, storedPreferences.deliveryFrequency)
        assertEquals(preferences.emailNotifications, storedPreferences.emailNotifications)
        assertEquals(preferences.pushNotifications, storedPreferences.pushNotifications)
        assertEquals(preferences.articlesPerDay, storedPreferences.articlesPerDay)
        
        appSession.clearUserPreferences()
        
        assertNull(appSession.getUserPreferences())
    }
    
    @Test
    fun appSession_avatarStorageFlow() {
        val avatarUrl = "https://example.com/avatar.jpg"
        
        assertNull(appSession.getAvatarUrl())
        
        appSession.storeAvatarUrl(avatarUrl)
        
        assertEquals(avatarUrl, appSession.getAvatarUrl())
        
        appSession.clearToken()
        
        assertNull(appSession.getAvatarUrl())
    }
    
    @Test
    fun appSession_fullUserSessionFlow() {
        val token = "access_token_123"
        val userId = 456L
        val username = "integrationtestuser"
        val avatarUrl = "https://example.com/avatar.jpg"
        val preferences = UserPreferences(
            userId = userId,
            technologyIds = listOf(1L, 2L),
            directions = listOf(ArticleDirection.BACKEND),
            deliveryFrequency = DeliveryFrequency.DAILY,
            emailNotifications = true,
            pushNotifications = true,
            excludedSources = emptyList(),
            articlesPerDay = 3,
            updatedAt = Instant.now()
        )
        
        appSession.storeToken(token)
        appSession.storeUserId(userId)
        appSession.storeUsername(username)
        appSession.storeAvatarUrl(avatarUrl)
        appSession.storeUserPreferences(preferences)
        
        assertEquals(token, appSession.getToken())
        assertEquals(userId, appSession.getUserId())
        assertEquals(username, appSession.getUsername())
        assertEquals(avatarUrl, appSession.getAvatarUrl())
        
        val storedPreferences = appSession.getUserPreferences()
        assertNotNull(storedPreferences)
        assertEquals(preferences.userId, storedPreferences!!.userId)
        assertEquals(preferences.deliveryFrequency, storedPreferences.deliveryFrequency)
        
        appSession.clearToken()
        
        assertNull(appSession.getToken())
        assertNull(appSession.getUserId())
        assertNull(appSession.getUsername())
        assertNull(appSession.getAvatarUrl())
        assertNull(appSession.getUserPreferences())
    }
    
    @Test
    fun appSession_persistsAcrossInstances() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val token = "persistent_token"
        val userId = 789L
        
        val session1 = AppSession(context)
        session1.storeToken(token)
        session1.storeUserId(userId)
        
        val session2 = AppSession(context)
        
        assertEquals(token, session2.getToken())
        assertEquals(userId, session2.getUserId())
        
        session2.clearToken()
    }
}
