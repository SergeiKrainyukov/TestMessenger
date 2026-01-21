package com.skrainyukov.testmessenger.presentation.screens.profile.edit

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.skrainyukov.testmessenger.presentation.components.LoadingButton
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(EditProfileEvent.OnImageSelected(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                EditProfileEffect.NavigateBack -> onNavigateBack()
                EditProfileEffect.NavigateToAuth -> onNavigateToAuth()
                is EditProfileEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                EditProfileEffect.ShowSuccess -> {
                    Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isInitialLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar section
                Box(
                    modifier = Modifier.padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar image
                    val imageModel = state.selectedImageUri ?: state.currentAvatarUrl

                    if (imageModel != null) {
                        Box {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Аватар",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )

                            // Remove button
                            if (state.selectedImageUri != null) {
                                IconButton(
                                    onClick = { viewModel.onEvent(EditProfileEvent.OnRemoveImage) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(32.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Placeholder with add button
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Добавить фото",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Нажмите для изменения",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.OnNameChanged(it)) },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                DateInputField(
                    value = state.birthday,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.OnBirthdayChanged(it)) },
                    label = "Дата рождения",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.city,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.OnCityChanged(it)) },
                    label = { Text("Город") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.about,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.OnAboutChanged(it)) },
                    label = { Text("О себе") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                LoadingButton(
                    text = "Сохранить",
                    onClick = { viewModel.onEvent(EditProfileEvent.OnSaveClick) },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = state.isLoading,
                    enabled = state.name.isNotBlank()
                )
            }
        }
    }
}

@Composable
fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    // Store only digits internally
    val digitsOnly = remember(value) {
        if (value.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
            // Convert yyyy-MM-dd to ddMMyyyy
            val parts = value.split("-")
            "${parts[2]}${parts[1]}${parts[0]}"
        } else {
            value.filter { it.isDigit() }
        }
    }

    OutlinedTextField(
        value = digitsOnly,
        onValueChange = { input ->
            // Filter only digits and limit to 8
            val filtered = input.filter { it.isDigit() }.take(8)

            // Convert to yyyy-MM-dd format when complete (8 digits)
            if (filtered.length == 8) {
                val day = filtered.substring(0, 2)
                val month = filtered.substring(2, 4)
                val year = filtered.substring(4, 8)

                // Basic validation
                val dayInt = day.toIntOrNull() ?: 0
                val monthInt = month.toIntOrNull() ?: 0

                if (dayInt in 1..31 && monthInt in 1..12) {
                    onValueChange("$year-$month-$day")
                } else {
                    onValueChange(filtered)
                }
            } else {
                onValueChange(filtered)
            }
        },
        label = { Text(label) },
        placeholder = { Text("дд.мм.гггг") },
        visualTransformation = DateVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true
    )
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text

        // Format: dd.MM.yyyy
        val formatted = buildString {
            for (i in digitsOnly.indices) {
                append(digitsOnly[i])
                if (i == 1 || i == 3) {
                    append('.')
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Add dots: after position 2 and 4 in original
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> offset + 2
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Remove dots from transformed position
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}