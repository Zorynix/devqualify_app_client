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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.diploma.work.ui.DiplomPasswordTextField
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.navigation.Login
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle

@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = viewModel()
) {
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val registerEnabled by viewModel.registerEnabled.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()

    if (registerSuccess) {
        navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }

        Text("Регистрация", style = TextStyle.titleLarge)

        DiplomTextField(
            value = username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Имя пользователя", style = TextStyle.bodySmall) },
            modifier = Modifier.padding(top = 16.dp)
        )

        DiplomTextField(
            value = email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = { Text("Email", style = TextStyle.bodySmall) },
            modifier = Modifier.padding(top = 8.dp)
        )

        DiplomPasswordTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Пароль", style = TextStyle.bodySmall) },
            modifier = Modifier.padding(top = 8.dp)
        )

        DiplomPasswordTextField(
            value = confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
            label = { Text("Подтвердите пароль", style = TextStyle.bodySmall) },
            modifier = Modifier.padding(top = 8.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = TextStyle.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.onRegisterClicked() },
            enabled = registerEnabled && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Зарегистрироваться", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Row(modifier = Modifier.padding(top = 16.dp)) {
            Text("Уже зарегистрированы? ", style = TextStyle.bodySmall)
            Text(
                "Войти",
                style = TextStyle.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate(Login)
                }
            )
        }
    }
}