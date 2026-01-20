package com.skrainyukov.testmessenger.presentation.screens.auth.phone

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skrainyukov.testmessenger.presentation.components.LoadingButton
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PhoneScreen(
    onNavigateToCode: () -> Unit,
    viewModel: PhoneViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PhoneEffect.NavigateToCode -> onNavigateToCode()
                is PhoneEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Вход",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Введите номер телефона",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.phone,
            onValueChange = { viewModel.onEvent(PhoneEvent.OnPhoneChanged(it)) },
            label = { Text("Номер телефона") },
            placeholder = { Text("+79219999999") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = state.error != null
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LoadingButton(
            text = "Получить код",
            onClick = { viewModel.onEvent(PhoneEvent.OnSendCodeClick) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
            enabled = state.isPhoneValid
        )
    }
}
