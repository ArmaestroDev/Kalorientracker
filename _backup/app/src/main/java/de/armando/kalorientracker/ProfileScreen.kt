@file:OptIn(ExperimentalMaterial3Api::class)
@file:RequiresApi(Build.VERSION_CODES.O)

package de.armando.kalorientracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.armando.kalorientracker.data.model.*

@Composable
fun ProfileScreen(
    initialProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onNavigateBack: () -> Unit
) {
    var apikey by remember { mutableStateOf(initialProfile.apikey) }
    var claudeApiKey by remember { mutableStateOf(initialProfile.claudeApiKey) }
    var selectedProvider by remember { mutableStateOf(initialProfile.selectedProvider) }
    var age by remember { mutableStateOf(initialProfile.age.takeIf { it > 0 }?.toString() ?: "") }
    var weight by remember { mutableStateOf(initialProfile.weightKg.takeIf { it > 0 }?.toString() ?: "") }
    var height by remember { mutableStateOf(initialProfile.heightCm.takeIf { it > 0 }?.toString() ?: "") }
    var gender by remember { mutableStateOf(initialProfile.gender) }
    var activityLevel by remember { mutableStateOf(initialProfile.activityLevel) }
    var goal by remember { mutableStateOf(initialProfile.goal) }

    val isFormValid by remember(age, weight, height) {
        derivedStateOf {
            val ageValue = age.toIntOrNull() ?: 0
            val weightValue = weight.toDoubleOrNull() ?: 0.0
            val heightValue = height.toDoubleOrNull() ?: 0.0
            ageValue > 0 && weightValue > 0.0 && heightValue > 0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dein Profil & Ziele") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DropdownSelector(
                label = "KI-Anbieter",
                options = AiProvider.entries,
                selectedOption = selectedProvider,
                onOptionSelected = { selectedProvider = it },
                optionToString = { it.name }
            )

            if (selectedProvider == AiProvider.GEMINI) {
                OutlinedTextField(
                    value = apikey,
                    onValueChange = { apikey = it },
                    label = { Text("Gemini API Schlüssel") },
                    placeholder = { Text("Gib deinen Gemini API-Schlüssel ein") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = claudeApiKey,
                    onValueChange = { claudeApiKey = it },
                    label = { Text("Claude API Schlüssel") },
                    placeholder = { Text("Gib deinen Claude API-Schlüssel ein") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = age,
                onValueChange = { newValue: String -> age = newValue.filter { it.isDigit() } },
                label = { Text("Alter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = age.isNotEmpty() && (age.toIntOrNull() ?: 0) <= 0
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { newValue: String ->
                    if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) { weight = newValue }
                },
                label = { Text("Gewicht (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = weight.isNotEmpty() && (weight.toDoubleOrNull() ?: 0.0) <= 0.0
            )
            OutlinedTextField(
                value = height,
                onValueChange = { newValue: String -> height = newValue.filter { it.isDigit() } },
                label = { Text("Größe (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = height.isNotEmpty() && (height.toDoubleOrNull() ?: 0.0) <= 0.0
            )

            DropdownSelector(
                label = "Geschlecht",
                options = Gender.entries,
                selectedOption = gender,
                onOptionSelected = { gender = it },
                optionToString = { if (it == Gender.MALE) "Männlich" else "Weiblich" }
            )
            DropdownSelector(
                label = "Aktivitätslevel",
                options = ActivityLevel.entries,
                selectedOption = activityLevel,
                onOptionSelected = { activityLevel = it },
                optionToString = { it.description }
            )
            DropdownSelector(
                label = "Fitness-Ziel",
                options = FitnessGoal.entries,
                selectedOption = goal,
                onOptionSelected = { goal = it },
                optionToString = { it.description }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val profile = UserProfile(
                        apikey = apikey,
                        claudeApiKey = claudeApiKey,
                        selectedProvider = selectedProvider,
                        age = age.toInt(),
                        weightKg = weight.toDouble(),
                        heightCm = height.toDouble(),
                        gender = gender,
                        activityLevel = activityLevel,
                        goal = goal
                    )
                    onSave(profile)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Speichern und Ziele neu berechnen")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelector(
    label: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = optionToString(selectedOption),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionToString(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}