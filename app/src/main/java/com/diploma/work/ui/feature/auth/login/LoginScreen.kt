package com.diploma.work.ui.feature.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.data.AppSession
import com.diploma.work.ui.DiplomPasswordTextField
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.ThemeToggleButton
import com.diploma.work.ui.navigation.Home
import com.diploma.work.ui.navigation.Register
import com.diploma.work.ui.navigation.safeNavigate
import com.diploma.work.ui.navigation.safeNavigateBack
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.diploma.work.ui.theme.Theme
import com.diploma.work.utils.ValidationUtils

@Composable
fun LoginScreen(
    navController: NavController,
    session: AppSession,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val password by viewModel.password.collectAsState()
    val loginEnabled by viewModel.loginEnabled.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val username by viewModel.username.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    if (loginSuccess) {
        navController.safeNavigate("Home", clearStack = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        ThemeToggleButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { navController.safeNavigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.login), style = TextStyle.TitleLarge.value)

        DiplomTextField(
            value = username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text(stringResource(R.string.email), style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 16.dp)
        )
        
        val emailValidation = ValidationUtils.validateEmail(username)
        if (!emailValidation.isValid && username.isNotBlank()) {
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
            label = { Text(stringResource(R.string.password), style = TextStyle.BodyLarge.value) },
            modifier = Modifier.padding(top = 8.dp)
        )
          val passwordValidation = ValidationUtils.validateStrongPassword(password)
        if (!passwordValidation.isValid && password.isNotBlank()) {
            Text(
                text = stringResource(R.string.password_requirements),
                color = Color.Red,
                fontSize = 11.sp,
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
                Text(stringResource(R.string.login), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.no_account), style = TextStyle.BodyMedium.value)
            Text(
                stringResource(R.string.register),
                style = TextStyle.Link.value,
                color = Theme.extendedColorScheme.onBackgroundPositive,                
                modifier = Modifier.clickable {
                    navController.safeNavigate("Register")
                }
            )
        }
        }
    }
}