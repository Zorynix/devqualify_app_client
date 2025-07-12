package com.diploma.work.ui.feature.auth.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.ui.navigation.Register
import com.diploma.work.ui.navigation.safeNavigate
import com.diploma.work.ui.navigation.safeNavigateBack
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.diploma.work.ui.theme.Theme

@Composable
fun EmailConfirmationScreen(
    navController: NavController,
    email: String,
    modifier: Modifier = Modifier,
    viewModel: EmailConfirmationViewModel = hiltViewModel()
) {
    val code by viewModel.code.collectAsState()
    val confirmEnabled by viewModel.confirmEnabled.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val resendCooldownSeconds by viewModel.resendCooldownSeconds.collectAsState()
    val resendEnabled by viewModel.resendEnabled.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.onSendCodeClicked()
        viewModel.navigationChannel.collect { destination ->
            navController.safeNavigate(destination, clearStack = true)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {        IconButton(
            onClick = { 
                if (!navController.safeNavigateBack()) {
                    navController.safeNavigate("Register")
                }
            },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }
        Text("Подтверждение почты", style = TextStyle.TitleLarge.value, modifier = Modifier.padding(16.dp))
        Text("Введите 6-значный код, отправленный на $email", style = TextStyle.BodyMedium.value, modifier = Modifier.padding(16.dp))

        val focusRequester = remember { FocusRequester() }
        Box {
            TextField(
                value = code,
                onValueChange = { viewModel.onCodeChanged(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0f)
                    .focusRequester(focusRequester),
                singleLine = true
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                for (i in 0 until 6) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(4.dp)
                            .clickable(enabled = !isLoading) { focusRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {                        Text(text = if (i < code.length) code[i].toString() else "", style = TextStyle.BodyMedium.value, color = Color.Black)
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        errorMessage?.let {
            Text(
                text = it,
                color = Theme.extendedColorScheme.outlineDanger,
                style = TextStyle.BodySmall.value,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        successMessage?.let {
            Text(
                text = it,
                color = Theme.extendedColorScheme.onBackgroundPositive,
                style = TextStyle.BodySmall.value,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = { viewModel.onConfirmClicked() },
            enabled = confirmEnabled && !isLoading,
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
                Text("Подтвердить", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Text(
            text = if (resendCooldownSeconds > 0) 
                "Отправить код повторно (${resendCooldownSeconds}с)" 
                else "Отправить код повторно",
            style = TextStyle.Link.value,
            color = if (resendEnabled) Theme.extendedColorScheme.onBackgroundPositive 
                else Theme.extendedColorScheme.onBackgroundHint,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable(enabled = resendEnabled) { viewModel.onResendCodeClicked() }
        )
    }
}