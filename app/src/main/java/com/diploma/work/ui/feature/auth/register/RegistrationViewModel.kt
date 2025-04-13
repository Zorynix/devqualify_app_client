package com.diploma.work.ui.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RegistrationViewModel : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    val registerEnabled: StateFlow<Boolean> = combine(username, email, password, confirmPassword) { u, e, p, cp ->
        u.isNotEmpty() && e.isNotEmpty() && p.isNotEmpty() && cp == p
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun onUsernameChanged(newValue: String) {
        _username.value = newValue
    }

    fun onEmailChanged(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
    }

    fun onConfirmPasswordChanged(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun onRegisterClicked() {

    }
}