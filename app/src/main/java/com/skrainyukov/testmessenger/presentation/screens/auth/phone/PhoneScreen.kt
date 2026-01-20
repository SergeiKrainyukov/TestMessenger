package com.skrainyukov.testmessenger.presentation.screens.auth.phone

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skrainyukov.testmessenger.presentation.components.LoadingButton
import com.skrainyukov.testmessenger.presentation.components.PhoneNumberInput
import com.skrainyukov.testmessenger.presentation.components.rememberCurrentCountry
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PhoneScreen(
    onNavigateToCode: () -> Unit,
    viewModel: PhoneViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Get current region as default
    val currentCountry = rememberCurrentCountry()

    // Set initial country once
    LaunchedEffect(currentCountry) {
        viewModel.setInitialCountry(currentCountry)
    }

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

        // Use the new phone number input with country picker
        state.selectedCountry?.let { country ->
            PhoneNumberInput(
                phoneNumber = state.phoneNumber,
                onPhoneNumberChange = { viewModel.onEvent(PhoneEvent.OnPhoneNumberChanged(it)) },
                selectedCountry = country,
                onCountrySelected = { viewModel.onEvent(PhoneEvent.OnCountrySelected(it)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.error != null
            )
        }

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
