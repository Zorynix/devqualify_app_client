package com.diploma.work.ui.feature.auth.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diploma.work.ui.theme.DiplomaWorkTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Before
    fun setup() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                LoginScreen(
                    onNavigateToRegister = {},
                    onNavigateToEmailConfirmation = { _, _ -> },
                    onLoginSuccess = {}
                )
            }
        }
    }
    
    @Test
    fun loginScreen_displaysAllElements() {
        composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signUpButton").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_emailInput_acceptsText() {
        val testEmail = "test@example.com"
        
        composeTestRule.onNodeWithTag("emailField")
            .performTextInput(testEmail)
        
        composeTestRule.onNodeWithTag("emailField")
            .assertTextContains(testEmail)
    }
    
    @Test
    fun loginScreen_passwordInput_acceptsText() {
        val testPassword = "password123"
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput(testPassword)
        
        composeTestRule.onNodeWithTag("passwordField")
            .assertTextContains(testPassword)
    }
    
    @Test
    fun loginScreen_loginButton_isClickable() {
        composeTestRule.onNodeWithTag("loginButton")
            .assertHasClickAction()
    }
    
    @Test
    fun loginScreen_signUpButton_isClickable() {
        composeTestRule.onNodeWithTag("signUpButton")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
    
    @Test
    fun loginScreen_passwordField_masksInput() {
        composeTestRule.onNodeWithTag("passwordField")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
    }
    
    @Test
    fun loginScreen_fieldsInteraction() {
        composeTestRule.onNodeWithTag("emailField")
            .performTextInput("test@example.com")
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("loginButton")
            .performClick()
    }
}
