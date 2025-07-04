package com.diploma.work.utils

import org.junit.Test
import org.junit.Assert.*

class ConstantsTest {
    
    @Test
    fun `PrefsKeys constants are not empty`() {
        assertFalse(Constants.PrefsKeys.APP_SESSION.isEmpty())
        assertFalse(Constants.PrefsKeys.ACCESS_TOKEN.isEmpty())
        assertFalse(Constants.PrefsKeys.USER_ID.isEmpty())
        assertFalse(Constants.PrefsKeys.USERNAME.isEmpty())
        assertFalse(Constants.PrefsKeys.AVATAR_URL.isEmpty())
        assertFalse(Constants.PrefsKeys.AVATAR_DATA.isEmpty())
    }
    
    @Test
    fun `PrefsKeys constants have expected values`() {
        assertEquals("app_session", Constants.PrefsKeys.APP_SESSION)
        assertEquals("access_token", Constants.PrefsKeys.ACCESS_TOKEN)
        assertEquals("user_id", Constants.PrefsKeys.USER_ID)
        assertEquals("username", Constants.PrefsKeys.USERNAME)
        assertEquals("avatar_url", Constants.PrefsKeys.AVATAR_URL)
        assertEquals("avatar_data", Constants.PrefsKeys.AVATAR_DATA)
    }
    
    @Test
    fun `UserPreferences constants are not empty`() {
        assertFalse(Constants.PrefsKeys.TECHNOLOGY_IDS.isEmpty())
        assertFalse(Constants.PrefsKeys.DIRECTIONS.isEmpty())
        assertFalse(Constants.PrefsKeys.DELIVERY_FREQUENCY.isEmpty())
        assertFalse(Constants.PrefsKeys.EMAIL_NOTIFICATIONS.isEmpty())
        assertFalse(Constants.PrefsKeys.PUSH_NOTIFICATIONS.isEmpty())
        assertFalse(Constants.PrefsKeys.ARTICLES_PER_DAY.isEmpty())
    }
    
    @Test
    fun `UserPreferences constants have expected values`() {
        assertEquals("technology_ids", Constants.PrefsKeys.TECHNOLOGY_IDS)
        assertEquals("directions", Constants.PrefsKeys.DIRECTIONS)
        assertEquals("delivery_frequency", Constants.PrefsKeys.DELIVERY_FREQUENCY)
        assertEquals("email_notifications", Constants.PrefsKeys.EMAIL_NOTIFICATIONS)
        assertEquals("push_notifications", Constants.PrefsKeys.PUSH_NOTIFICATIONS)
        assertEquals("articles_per_day", Constants.PrefsKeys.ARTICLES_PER_DAY)
    }
    
    @Test
    fun `Network constants are defined`() {
        assertTrue(Constants.Network.TIMEOUT_SECONDS > 0)
        assertTrue(Constants.Network.GRPC_PORT > 0)
        assertFalse(Constants.Network.BASE_URL.isEmpty())
    }
    
    @Test
    fun `App constants are defined`() {
        assertTrue(Constants.App.APP_ID > 0)
        assertTrue(Constants.App.MIN_PASSWORD_LENGTH > 0)
        assertTrue(Constants.App.MAX_USERNAME_LENGTH > 0)
        assertFalse(Constants.App.DEFAULT_AVATAR_URL.isEmpty())
    }
    
    @Test
    fun `Test constants are defined`() {
        assertTrue(Constants.Test.MAX_QUESTIONS_PER_TEST > 0)
        assertTrue(Constants.Test.MIN_QUESTIONS_PER_TEST > 0)
        assertTrue(Constants.Test.DEFAULT_TIME_LIMIT_MINUTES > 0)
        assertTrue(Constants.Test.PASSING_SCORE_PERCENTAGE > 0)
        assertTrue(Constants.Test.PASSING_SCORE_PERCENTAGE <= 100)
    }
    
    @Test
    fun `Article constants are defined`() {
        assertTrue(Constants.Article.DEFAULT_PAGE_SIZE > 0)
        assertTrue(Constants.Article.MAX_PAGE_SIZE > 0)
        assertTrue(Constants.Article.MIN_READ_TIME > 0)
        assertTrue(Constants.Article.MAX_READ_TIME > 0)
    }
    
    @Test
    fun `all constant values are reasonable`() {
        assertTrue(Constants.Network.TIMEOUT_SECONDS >= 10)
        assertTrue(Constants.Network.TIMEOUT_SECONDS <= 60)
        
        assertTrue(Constants.App.MIN_PASSWORD_LENGTH >= 6)
        assertTrue(Constants.App.MIN_PASSWORD_LENGTH <= 20)
        
        assertTrue(Constants.App.MAX_USERNAME_LENGTH >= 20)
        assertTrue(Constants.App.MAX_USERNAME_LENGTH <= 100)
        
        assertTrue(Constants.Test.MIN_QUESTIONS_PER_TEST >= 1)
        assertTrue(Constants.Test.MAX_QUESTIONS_PER_TEST >= Constants.Test.MIN_QUESTIONS_PER_TEST)
        
        assertTrue(Constants.Test.DEFAULT_TIME_LIMIT_MINUTES >= 5)
        assertTrue(Constants.Test.DEFAULT_TIME_LIMIT_MINUTES <= 180)
        
        assertTrue(Constants.Test.PASSING_SCORE_PERCENTAGE >= 50)
        
        assertTrue(Constants.Article.DEFAULT_PAGE_SIZE >= 10)
        assertTrue(Constants.Article.DEFAULT_PAGE_SIZE <= 100)
        
        assertTrue(Constants.Article.MAX_PAGE_SIZE >= Constants.Article.DEFAULT_PAGE_SIZE)
    }
}
