package com.diploma.work.data

import android.content.Context
import android.content.SharedPreferences
import com.diploma.work.data.models.*
import com.diploma.work.utils.Constants
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant

class AppSessionTest {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var appSession: AppSession
    
    @Before
    fun setup() {
        clearAllMocks()
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk(relaxed = true)
        
        every { context.getSharedPreferences(Constants.PrefsKeys.APP_SESSION, Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.getString(any(), any()) } returns null
        every { sharedPreferences.getLong(any(), any()) } returns -1L
        every { sharedPreferences.getBoolean(any(), any()) } returns false
        every { sharedPreferences.getInt(any(), any()) } returns 0
        every { sharedPreferences.getStringSet(any(), any()) } returns null
        every { sharedPreferences.contains(any()) } returns false
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putStringSet(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        
        appSession = AppSession(context)
    }
    
    @Test
    fun `storeToken stores token correctly`() {
        val token = "test_token"
        
        appSession.storeToken(token)
        
        verify { editor.putString(Constants.PrefsKeys.ACCESS_TOKEN, token) }
        verify { editor.apply() }
    }
    
    @Test
    fun `getToken returns stored token`() {
        val token = "test_token"
        every { sharedPreferences.getString(Constants.PrefsKeys.ACCESS_TOKEN, null) } returns token
        
        val result = appSession.getToken()
        
        assertEquals(token, result)
    }
    
    @Test
    fun `getToken returns null when no token stored`() {
        every { sharedPreferences.getString(Constants.PrefsKeys.ACCESS_TOKEN, null) } returns null
        
        val result = appSession.getToken()
        
        assertNull(result)
    }
    
    @Test
    fun `clearToken removes all user data`() {
        appSession.clearToken()
        
        verify { editor.remove(Constants.PrefsKeys.ACCESS_TOKEN) }
        verify { editor.remove(Constants.PrefsKeys.USER_ID) }
        verify { editor.remove(Constants.PrefsKeys.USERNAME) }
        verify { editor.remove(Constants.PrefsKeys.AVATAR_URL) }
        verify { editor.remove(Constants.PrefsKeys.AVATAR_DATA) }
        verify { editor.apply() }
    }
    
    @Test
    fun `storeUserId stores user id correctly`() {
        val userId = 123L
        
        appSession.storeUserId(userId)
        
        verify { editor.putLong(Constants.PrefsKeys.USER_ID, userId) }
        verify { editor.apply() }
    }
    
    @Test
    fun `getUserId returns stored user id`() {
        val userId = 123L
        every { sharedPreferences.contains(Constants.PrefsKeys.USER_ID) } returns true
        every { sharedPreferences.getLong(Constants.PrefsKeys.USER_ID, -1) } returns userId
        
        val result = appSession.getUserId()
        
        assertEquals(userId, result)
    }
    
    @Test
    fun `getUserId returns null when no user id stored`() {
        every { sharedPreferences.contains(Constants.PrefsKeys.USER_ID) } returns false
        every { sharedPreferences.getLong(Constants.PrefsKeys.USER_ID, -1) } returns -1L
        
        val result = appSession.getUserId()
        
        assertNull(result)
    }
    
    @Test
    fun `storeUsername stores username correctly`() {
        val username = "testuser"
        
        appSession.storeUsername(username)
        
        verify { editor.putString(Constants.PrefsKeys.USERNAME, username) }
        verify { editor.apply() }
    }
    
    @Test
    fun `getUsername returns stored username`() {
        val username = "testuser"
        every { sharedPreferences.getString(Constants.PrefsKeys.USERNAME, null) } returns username
        
        val result = appSession.getUsername()
        
        assertEquals(username, result)
    }
    
    @Test
    fun `storeUserPreferences stores preferences correctly`() {
        val preferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(1L, 2L),
            directions = listOf(ArticleDirection.BACKEND),
            deliveryFrequency = DeliveryFrequency.WEEKLY,
            emailNotifications = true,
            pushNotifications = false,
            excludedSources = emptyList(),
            articlesPerDay = 5,
            updatedAt = Instant.now()
        )
        
        appSession.storeUserPreferences(preferences)
        
        verify { editor.putLong(Constants.PrefsKeys.USER_ID, preferences.userId) }
        verify { editor.putStringSet(Constants.PrefsKeys.TECHNOLOGY_IDS, preferences.technologyIds.map { it.toString() }.toSet()) }
        verify { editor.putStringSet(Constants.PrefsKeys.DIRECTIONS, preferences.directions.map { it.name }.toSet()) }
        verify { editor.putString(Constants.PrefsKeys.DELIVERY_FREQUENCY, preferences.deliveryFrequency.name) }
        verify { editor.putBoolean(Constants.PrefsKeys.EMAIL_NOTIFICATIONS, preferences.emailNotifications) }
        verify { editor.putBoolean(Constants.PrefsKeys.PUSH_NOTIFICATIONS, preferences.pushNotifications) }
        verify { editor.putInt(Constants.PrefsKeys.ARTICLES_PER_DAY, preferences.articlesPerDay) }
        verify { editor.apply() }
    }
    
    @Test
    fun `clearUserPreferences removes all preference data`() {
        appSession.clearUserPreferences()
        
        verify { editor.remove(Constants.PrefsKeys.TECHNOLOGY_IDS) }
        verify { editor.remove(Constants.PrefsKeys.DIRECTIONS) }
        verify { editor.remove(Constants.PrefsKeys.DELIVERY_FREQUENCY) }
        verify { editor.remove(Constants.PrefsKeys.EMAIL_NOTIFICATIONS) }
        verify { editor.remove(Constants.PrefsKeys.PUSH_NOTIFICATIONS) }
        verify { editor.remove(Constants.PrefsKeys.ARTICLES_PER_DAY) }
        verify { editor.apply() }
    }
}
