package com.skrainyukov.testmessenger.presentation.screens.profile.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Refresh profile when screen becomes visible (after returning from edit screen)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshProfile()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                ProfileEffect.NavigateToEdit -> onNavigateToEdit()
                ProfileEffect.NavigateToAuth -> onNavigateToAuth()
                is ProfileEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ProfileEvent.OnEditClick) }) {
                        Icon(Icons.Default.Edit, "Редактировать")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && state.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    if (state.user?.avatar != null) {
                        AsyncImage(
                            model = state.user!!.avatar,
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.user?.name?.firstOrNull()?.toString() ?: "?",
                                    fontSize = 48.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = state.user?.name ?: "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "@${state.user?.username}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Все поля должны отображаться всегда согласно ТЗ
                    ProfileInfoItem("Телефон", state.user?.phone ?: "")
                    ProfileInfoItem("Никнейм", state.user?.username ?: "")
                    ProfileInfoItem("Город", state.user?.city ?: "Не указан")
                    ProfileInfoItem(
                        "Дата рождения",
                        state.user?.birthday?.let { formatDateForDisplay(it) } ?: "Не указана"
                    )
                    ProfileInfoItem("Знак зодиака", state.user?.zodiacSign?.displayName ?: "Не определен")
                    ProfileInfoItem("О себе", state.user?.about?.ifEmpty { "Не указано" } ?: "Не указано")

                    Spacer(modifier = Modifier.height(32.dp))

                    // Logout button
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выйти из аккаунта")
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход") },
            text = { Text("Вы уверены, что хотите выйти из аккаунта?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.onEvent(ProfileEvent.OnLogoutClick)
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp
        )
    }
}

// Convert yyyy-MM-dd to dd.MM.yyyy for display
private fun formatDateForDisplay(date: String): String {
    return if (date.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        val parts = date.split("-")
        "${parts[2]}.${parts[1]}.${parts[0]}"
    } else {
        date
    }
}
