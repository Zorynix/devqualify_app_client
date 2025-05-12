package com.diploma.work.ui.feature.auth.login

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.data.AppSession
import com.diploma.work.ui.DiplomPasswordTextField
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.navigation.Home
import com.diploma.work.ui.navigation.Register
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.diploma.work.ui.theme.Theme

@Composable
fun LoginScreen(
    navController: NavController,
    session: AppSession,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val password by viewModel.password.collectAsState()
    val loginEnabled by viewModel.loginEnabled.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val username by viewModel.username.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    if (loginSuccess) {
        navController.navigate(Home::class.simpleName.toString()) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
        Text("Войти", style = TextStyle.TitleLarge.value)
        DiplomTextField(
            value = username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Email", style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 16.dp)
        )
        DiplomPasswordTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Пароль", style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 8.dp)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Theme.extendedColorScheme.outlineDanger,
                style = TextStyle.BodySmall.value,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = { viewModel.onLoginClicked(session) },
            enabled = loginEnabled && !isLoading,
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
                Text("Войти", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Text("Нет аккаунта? ", style = TextStyle.BodyMedium.value)
            Text(
                "Зарегистрироваться",
                style = TextStyle.Link.value,
                color = Theme.extendedColorScheme.onBackgroundPositive,
                modifier = Modifier.clickable {
                    navController.navigate(Register::class.simpleName.toString())
                }
            )
        }
    }
}