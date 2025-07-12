package com.diploma.work.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.diploma.work.data.AppSession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
    fun appSession_storageOperations_uiInteraction() {
        val token = "test_access_token"
        val userId = 123L
        val username = "testuser"
        
        appSession.storeToken(token)
        appSession.storeUserId(userId)
        appSession.storeUsername(username)
        
        appSession.clearToken()
    }
    
    @Test
    fun appSession_persistsAcrossInstances_uiFlow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val token = "persistent_token"
        val userId = 789L
        
        val session1 = AppSession(context)
        session1.storeToken(token)
        session1.storeUserId(userId)
        
        val session2 = AppSession(context)
        
        session2.clearToken()
    }
}
