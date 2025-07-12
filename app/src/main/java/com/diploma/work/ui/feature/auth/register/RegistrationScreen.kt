package com.diploma.work.ui.feature.auth.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.data.AppSession
import com.diploma.work.ui.DiplomPasswordTextField
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.navigation.Login
import com.diploma.work.ui.navigation.safeNavigate
import com.diploma.work.ui.navigation.safeNavigateBack
import com.diploma.work.ui.theme.TextStyle
import com.diploma.work.ui.theme.Theme
import com.diploma.work.utils.ValidationUtils

@Composable
fun RegistrationScreen(
    navController: NavController,
    session: AppSession,
    modifier: Modifier = Modifier,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val registerEnabled by viewModel.registerEnabled.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigationChannel.collect { destination ->
            navController.safeNavigate(destination, clearStack = true)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { navController.safeNavigateBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }
        Text("Регистрация", style = TextStyle.TitleLarge.value)
        
        DiplomTextField(
            value = email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Email", style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 8.dp)
        )
        
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid && email.isNotBlank()) {
            Text(
                text = emailValidation.errorMessage ?: "",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.Start)
            )
        }
        
        DiplomPasswordTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Пароль", style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 8.dp)
        )        
        if (password.isNotBlank()) {
            val passwordValidation = ValidationUtils.validateStrongPassword(password)
            if (!passwordValidation.isValid) {
                Text(
                    text = "Пароль: мин. 8 символов, цифра, заглавная/строчная буквы, спецсимвол (@#$%^&+=)",
                    color = Color.Red,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.Start)
                )
            }
        }
        DiplomPasswordTextField(
            value = confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
            label = { Text("Подтвердите пароль", style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 8.dp)
        )
        
        if (confirmPassword.isNotBlank() && password != confirmPassword) {
            Text(
                text = "Пароли не совпадают",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.Start)
            )
        }
        if (errorMessage != null) {
            ErrorCard(
                error = errorMessage!!,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.onRegisterClicked(session) },
            enabled = registerEnabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Зарегистрироваться", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Text("Уже зарегистрированы? ", style = TextStyle.BodyMedium.value)
            Text(
                "Войти",
                style = TextStyle.Link.value,
                color = Theme.extendedColorScheme.onBackgroundPositive,
                modifier = Modifier.clickable {
                    navController.safeNavigate("Login")
                }
            )
        }
    }
}
