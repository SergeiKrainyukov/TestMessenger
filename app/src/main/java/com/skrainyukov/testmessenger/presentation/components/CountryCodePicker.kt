package com.skrainyukov.testmessenger.presentation.components

import android.telephony.TelephonyManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class Country(
    val name: String,
    val code: String,
    val dialCode: String,
    val flag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Номер телефона",
    isError: Boolean = false
) {
    var showCountryPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { value ->
            // Allow only digits and filter out country code if it's being typed
            val filtered = value.filter { it.isDigit() }
            onPhoneNumberChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text("9219999999") },
        leadingIcon = {
            Row(
                modifier = Modifier
                    .clickable { showCountryPicker = true }
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCountry.flag,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedCountry.dialCode,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Выбрать страну",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        isError = isError,
        modifier = modifier
    )

    if (showCountryPicker) {
        CountryPickerDialog(
            countries = getCountries(),
            onCountrySelected = { country ->
                onCountrySelected(country)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerDialog(
    countries: List<Country>,
    onCountrySelected: (Country) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column {
                // Header
                Text(
                    text = "Выберите страну",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()

                // Country list
                LazyColumn {
                    items(countries) { country ->
                        ListItem(
                            headlineContent = { Text(country.name) },
                            leadingContent = {
                                Text(
                                    text = country.flag,
                                    fontSize = 28.sp
                                )
                            },
                            trailingContent = {
                                Text(
                                    text = country.dialCode,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.clickable {
                                onCountrySelected(country)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberCurrentCountry(): Country {
    val context = LocalContext.current
    val telephonyManager = context.getSystemService(TelephonyManager::class.java)
    val countryCode = telephonyManager?.simCountryIso?.uppercase() ?: "RU"

    return remember(countryCode) {
        getCountries().find { it.code == countryCode } ?: getCountries().first { it.code == "RU" }
    }
}

fun getCountries(): List<Country> {
    return listOf(
        Country("Россия", "RU", "+7", "🇷🇺"),
        Country("Казахстан", "KZ", "+7", "🇰🇿"),
        Country("Украина", "UA", "+380", "🇺🇦"),
        Country("Беларусь", "BY", "+375", "🇧🇾"),
        Country("США", "US", "+1", "🇺🇸"),
        Country("Великобритания", "GB", "+44", "🇬🇧"),
        Country("Германия", "DE", "+49", "🇩🇪"),
        Country("Франция", "FR", "+33", "🇫🇷"),
        Country("Италия", "IT", "+39", "🇮🇹"),
        Country("Испания", "ES", "+34", "🇪🇸"),
        Country("Польша", "PL", "+48", "🇵🇱"),
        Country("Турция", "TR", "+90", "🇹🇷"),
        Country("Грузия", "GE", "+995", "🇬🇪"),
        Country("Армения", "AM", "+374", "🇦🇲"),
        Country("Азербайджан", "AZ", "+994", "🇦🇿"),
        Country("Узбекистан", "UZ", "+998", "🇺🇿"),
        Country("Киргизия", "KG", "+996", "🇰🇬"),
        Country("Таджикистан", "TJ", "+992", "🇹🇯"),
        Country("Туркменистан", "TM", "+993", "🇹🇲"),
        Country("Молдова", "MD", "+373", "🇲🇩"),
        Country("Литва", "LT", "+370", "🇱🇹"),
        Country("Латвия", "LV", "+371", "🇱🇻"),
        Country("Эстония", "EE", "+372", "🇪🇪"),
        Country("Китай", "CN", "+86", "🇨🇳"),
        Country("Япония", "JP", "+81", "🇯🇵"),
        Country("Индия", "IN", "+91", "🇮🇳"),
        Country("Бразилия", "BR", "+55", "🇧🇷"),
        Country("Австралия", "AU", "+61", "🇦🇺"),
        Country("Канада", "CA", "+1", "🇨🇦"),
        Country("Мексика", "MX", "+52", "🇲🇽")
    ).sortedBy { it.name }
}