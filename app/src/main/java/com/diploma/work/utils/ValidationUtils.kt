package com.diploma.work.utils

import android.util.Patterns
import java.util.regex.Pattern


object ValidationUtils {
    
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 30
    
    private val STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )
    
    private val USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]{3,30}$"
    )
      sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
        
        val isValid: Boolean get() = this is Valid
        val errorMessage: String? get() = (this as? Invalid)?.message
    }
    

    fun validateEmail(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.Invalid("Email не может быть пустым")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult.Invalid("Неверный формат email")
            else -> ValidationResult.Valid
        }
    }
    

    fun validatePassword(password: String?): ValidationResult {
        return when {
            password.isNullOrBlank() -> ValidationResult.Invalid("Пароль не может быть пустым")
            password.length < MIN_PASSWORD_LENGTH -> 
                ValidationResult.Invalid("Пароль должен содержать минимум $MIN_PASSWORD_LENGTH символов")
            password.length > MAX_PASSWORD_LENGTH -> 
                ValidationResult.Invalid("Пароль не может содержать более $MAX_PASSWORD_LENGTH символов")
            else -> ValidationResult.Valid
        }
    }
    

    fun validateStrongPassword(password: String?): ValidationResult {
        val basicValidation = validatePassword(password)
        if (basicValidation !is ValidationResult.Valid) return basicValidation
        
        return when {
            !STRONG_PASSWORD_PATTERN.matcher(password!!).matches() -> 
                ValidationResult.Invalid("Пароль должен содержать хотя бы одну цифру, заглавную и строчную буквы, специальный символ")
            else -> ValidationResult.Valid
        }
    }
    

    fun validatePasswordConfirmation(
        password: String?, 
        confirmPassword: String?
    ): ValidationResult {
        return when {
            confirmPassword.isNullOrBlank() -> 
                ValidationResult.Invalid("Подтверждение пароля не может быть пустым")
            password != confirmPassword -> 
                ValidationResult.Invalid("Пароли не совпадают")
            else -> ValidationResult.Valid
        }
    }
    

    fun validateUsername(username: String?): ValidationResult {
        return when {
            username.isNullOrBlank() -> ValidationResult.Invalid("Имя пользователя не может быть пустым")
            username.length < MIN_USERNAME_LENGTH -> 
                ValidationResult.Invalid("Имя пользователя должно содержать минимум $MIN_USERNAME_LENGTH символа")
            username.length > MAX_USERNAME_LENGTH -> 
                ValidationResult.Invalid("Имя пользователя не может содержать более $MAX_USERNAME_LENGTH символов")
            !USERNAME_PATTERN.matcher(username).matches() -> 
                ValidationResult.Invalid("Имя пользователя может содержать только латинские буквы, цифры, дефис и подчеркивание")
            else -> ValidationResult.Valid
        }
    }
    

    fun validateConfirmationCode(code: String?): ValidationResult {
        return when {
            code.isNullOrBlank() -> ValidationResult.Invalid("Код подтверждения не может быть пустым")
            code.length != 6 -> ValidationResult.Invalid("Код подтверждения должен содержать 6 цифр")
            !code.all { it.isDigit() } -> ValidationResult.Invalid("Код подтверждения должен содержать только цифры")
            else -> ValidationResult.Valid
        }
    }
    

    fun validateArticlesPerDay(count: Int): ValidationResult {
        return when {
            count < Constants.Business.MIN_ARTICLES_PER_DAY -> 
                ValidationResult.Invalid("Минимальное количество статей: ${Constants.Business.MIN_ARTICLES_PER_DAY}")
            count > Constants.Business.MAX_ARTICLES_PER_DAY -> 
                ValidationResult.Invalid("Максимальное количество статей: ${Constants.Business.MAX_ARTICLES_PER_DAY}")
            else -> ValidationResult.Valid
        }
    }
    
    fun validateUrl(url: String?): ValidationResult {
        return when {
            url.isNullOrBlank() -> ValidationResult.Invalid("URL не может быть пустым")
            !Patterns.WEB_URL.matcher(url).matches() -> 
                ValidationResult.Invalid("Неверный формат URL")
            else -> ValidationResult.Valid
        }
    }
}
