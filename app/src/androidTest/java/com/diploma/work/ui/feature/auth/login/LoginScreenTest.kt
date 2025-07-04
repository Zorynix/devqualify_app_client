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
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign up").assertIsDisplayed()
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
    fun loginScreen_loginButton_isInitiallyDisabled() {
        composeTestRule.onNodeWithTag("loginButton")
            .assertIsNotEnabled()
    }
    
    @Test
    fun loginScreen_loginButton_enabledWithValidInput() {
        composeTestRule.onNodeWithTag("emailField")
            .performTextInput("test@example.com")
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("loginButton")
            .assertIsEnabled()
    }
    
    @Test
    fun loginScreen_signUpButton_isClickable() {
        composeTestRule.onNodeWithText("Sign up")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
    
    @Test
    fun loginScreen_passwordField_isPassword() {
        composeTestRule.onNodeWithTag("passwordField")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("passwordField")
            .assertTextContains("•••••••••••")
    }
    
    @Test
    fun loginScreen_emptyEmailShowsError() {
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("loginButton")
            .performClick()
        
        composeTestRule.onNodeWithText("Please enter a valid email address")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_invalidEmailShowsError() {
        composeTestRule.onNodeWithTag("emailField")
            .performTextInput("invalid-email")
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("loginButton")
            .performClick()
        
        composeTestRule.onNodeWithText("Please enter a valid email address")
            .assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_shortPasswordShowsError() {
        composeTestRule.onNodeWithTag("emailField")
            .performTextInput("test@example.com")
        
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("123")
        
        composeTestRule.onNodeWithTag("loginButton")
            .performClick()
        
        composeTestRule.onNodeWithText("Password must be at least 6 characters")
            .assertIsDisplayed()
    }
}
