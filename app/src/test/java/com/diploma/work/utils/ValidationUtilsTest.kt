package com.diploma.work.utils

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ValidationUtilsTest {
    
    @Test
    fun validateEmail_validEmail_returnsValid() {
        val result = ValidationUtils.validateEmail("test@example.com")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validateEmail_invalidEmail_returnsInvalid() {
        val result = ValidationUtils.validateEmail("invalid-email")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }
    
    @Test
    fun validateEmail_emptyEmail_returnsInvalid() {
        val result = ValidationUtils.validateEmail("")
        assertFalse(result.isValid)
        assertEquals("Email не может быть пустым", result.errorMessage)
    }
    
    @Test
    fun validateEmail_nullEmail_returnsInvalid() {
        val result = ValidationUtils.validateEmail(null)
        assertFalse(result.isValid)
        assertEquals("Email не может быть пустым", result.errorMessage)
    }
    
    @Test
    fun validatePassword_validPassword_returnsValid() {
        val result = ValidationUtils.validatePassword("password123")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validatePassword_shortPassword_returnsInvalid() {
        val result = ValidationUtils.validatePassword("123")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("минимум"))
    }
    
    @Test
    fun validatePassword_longPassword_returnsInvalid() {
        val longPassword = "a".repeat(130)
        val result = ValidationUtils.validatePassword(longPassword)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("более"))
    }
    
    @Test
    fun validatePassword_emptyPassword_returnsInvalid() {
        val result = ValidationUtils.validatePassword("")
        assertFalse(result.isValid)
        assertEquals("Пароль не может быть пустым", result.errorMessage)
    }
    
    @Test
    fun validatePasswordConfirmation_matchingPasswords_returnsValid() {
        val result = ValidationUtils.validatePasswordConfirmation("password", "password")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validatePasswordConfirmation_nonMatchingPasswords_returnsInvalid() {
        val result = ValidationUtils.validatePasswordConfirmation("password1", "password2")
        assertFalse(result.isValid)
        assertEquals("Пароли не совпадают", result.errorMessage)
    }
    
    @Test
    fun validateUsername_validUsername_returnsValid() {
        val result = ValidationUtils.validateUsername("validuser123")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validateUsername_shortUsername_returnsInvalid() {
        val result = ValidationUtils.validateUsername("ab")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("минимум"))
    }
    
    @Test
    fun validateUsername_longUsername_returnsInvalid() {
        val longUsername = "a".repeat(32)
        val result = ValidationUtils.validateUsername(longUsername)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("более"))
    }
    
    @Test
    fun validateUsername_invalidCharacters_returnsInvalid() {
        val result = ValidationUtils.validateUsername("user@name")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("латинские"))
    }
    
    @Test
    fun validateConfirmationCode_validCode_returnsValid() {
        val result = ValidationUtils.validateConfirmationCode("123456")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validateConfirmationCode_shortCode_returnsInvalid() {
        val result = ValidationUtils.validateConfirmationCode("1234")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("6 цифр"))
    }
    
    @Test
    fun validateConfirmationCode_nonNumericCode_returnsInvalid() {
        val result = ValidationUtils.validateConfirmationCode("12ab56")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("только цифры"))
    }
    
    @Test
    fun validateArticlesPerDay_validCount_returnsValid() {
        val result = ValidationUtils.validateArticlesPerDay(5)
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validateArticlesPerDay_belowMinimum_returnsInvalid() {
        val result = ValidationUtils.validateArticlesPerDay(0)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("Минимальное"))
    }
    
    @Test
    fun validateArticlesPerDay_aboveMaximum_returnsInvalid() {
        val result = ValidationUtils.validateArticlesPerDay(150)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("Максимальное"))
    }
    
    @Test
    fun validateUrl_validUrl_returnsValid() {
        val result = ValidationUtils.validateUrl("https://example.com")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun validateUrl_invalidUrl_returnsInvalid() {
        val result = ValidationUtils.validateUrl("not-a-url")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("Неверный формат"))
    }
}
