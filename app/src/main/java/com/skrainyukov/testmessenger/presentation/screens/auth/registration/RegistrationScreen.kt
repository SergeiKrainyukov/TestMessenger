package com.skrainyukov.testmessenger.presentation.screens.auth.registration

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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegistrationScreen(
    onNavigateToChats: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                RegistrationEffect.NavigateToChats -> onNavigateToChats()
                is RegistrationEffect.ShowError -> {
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
            text = "Регистрация",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.phone,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onEvent(RegistrationEvent.OnNameChanged(it)) },
            label = { Text("Имя") },
            placeholder = { Text("Иван Иванов") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = { viewModel.onEvent(RegistrationEvent.OnUsernameChanged(it)) },
            label = { Text("Username") },
            placeholder = { Text("ivan_ivanov") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Только латинские буквы, цифры, - и _") }
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
            text = "Зарегистрироваться",
            onClick = { viewModel.onEvent(RegistrationEvent.OnRegisterClick) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading,
            enabled = state.isFormValid
        )
    }
}