@file:RequiresApi(Build.VERSION_CODES.O)

package de.armando.kalorientracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import de.armando.kalorientracker.data.model.ActivityEntry
import de.armando.kalorientracker.data.model.CalorieGoals
import de.armando.kalorientracker.data.model.FoodEntry
import de.armando.kalorientracker.data.UserPreferencesRepository
import de.armando.kalorientracker.data.model.FoodNutritionInfo
import de.armando.kalorientracker.ui.theme.AppThemes
import de.armando.kalorientracker.ui.theme.KalorientrackerTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

enum class Screen { Home, Profile }

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefsRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsRepository =
            UserPreferencesRepository(this)
        setContent {
            val selectedTheme = remember { mutableStateOf(prefsRepository.getTheme()) }
            var currentScreen by remember { mutableStateOf(Screen.Home) }

            KalorientrackerTheme(themeName = selectedTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Home -> CalorieTrackerScreen(
                            viewModel = viewModel,
                            onThemeChange = { themeName ->
                                prefsRepository.saveTheme(themeName)
                                selectedTheme.value = themeName
                            },
                            onNavigateToProfile = { currentScreen = Screen.Profile }
                        )

                        Screen.Profile -> ProfileScreen(
                            initialProfile = viewModel.uiState.collectAsState().value.userProfile,
                            onSave = { profile ->
                                viewModel.saveUserProfileAndRecalculateGoals(profile)
                                currentScreen = Screen.Home
                            },
                            onNavigateBack = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieTrackerScreen(
    viewModel: MainViewModel,
    onThemeChange: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingFood by remember { mutableStateOf<FoodEntry?>(null) }
    var editingActivity by remember { mutableStateOf<ActivityEntry?>(null) }

    // Dialog states
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var deletingFood by remember { mutableStateOf<FoodEntry?>(null) }
    var deletingActivity by remember { mutableStateOf<ActivityEntry?>(null) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showFabOptions by remember { mutableStateOf(false) }

    val dailyLog = uiState.selectedDayLog

    val totalCalories = dailyLog.foodEntries.sumOf { it.calories }
    val totalProtein = dailyLog.foodEntries.sumOf { it.protein }
    val totalCarbs = dailyLog.foodEntries.sumOf { it.carbs }
    val totalFat = dailyLog.foodEntries.sumOf { it.fat }
    val caloriesBurned = dailyLog.activityEntries.sumOf { it.caloriesBurned }
    val netCalories = totalCalories - caloriesBurned

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KI Kalorien-Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, "Profil & Ziele", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Box {
                        IconButton(onClick = { showThemePicker = true }) {
                            Icon(Icons.Default.Palette, "Design ändern", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        DropdownMenu(expanded = showThemePicker, onDismissRequest = { showThemePicker = false }) {
                            AppThemes.keys.forEach { themeName ->
                                DropdownMenuItem(text = { Text(themeName) }, onClick = {
                                    onThemeChange(themeName)
                                    showThemePicker = false
                                })
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedVisibility(visible = showFabOptions) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SmallFloatingActionButton(
                            onClick = { showAddActivityDialog = true; showFabOptions = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) { Icon(Icons.AutoMirrored.Filled.DirectionsRun, "Aktivität hinzufügen") }

                        SmallFloatingActionButton(
                            onClick = { showAddFoodDialog = true; showFabOptions = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) { Icon(Icons.Default.Fastfood, "Essen hinzufügen") }

                        SmallFloatingActionButton(
                            onClick = { showBarcodeScanner = true; showFabOptions = false },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) { Icon(Icons.Default.QrCodeScanner, "Barcode-Essen hinzufügen") }
                    }
                }
                FloatingActionButton(onClick = { showFabOptions = !showFabOptions }) {
                    Icon(
                        if (showFabOptions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Hinzufügen"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val germanLocale = Locale.GERMAN
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(germanLocale)
            val formattedDate = uiState.selectedDate.format(formatter)

            // Use a Row to align the buttons and text horizontally
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button for "Yesterday"
                IconButton(onClick = {
                    val yesterday = uiState.selectedDate.minusDays(1)
                    viewModel.changeDate(yesterday)
                }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Gestern")
                }

                // Clickable Date Text
                Text(
                    text = formattedDate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp) // Add some space around the text
                        .clickable { showDatePicker = true },
                    textAlign = TextAlign.Center
                )

                // Button for "Tomorrow"
                IconButton(onClick = {
                    val tomorrow = uiState.selectedDate.plusDays(1)
                    viewModel.changeDate(tomorrow)
                }) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Morgen")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            GoalsSummaryCard(
                netCalories = netCalories,
                totalProtein = totalProtein.toInt(),
                totalCarbs = totalCarbs.toInt(),
                totalFat = totalFat.toInt(),
                goals = uiState.goals,
                onSetGoals = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(dailyLog.foodEntries, key = { it.id }) { food ->
                        FoodItemRow(
                            food = food,
                            onEdit = { editingFood = food },
                            onDelete = { deletingFood = food })
                    }
                    items(dailyLog.activityEntries, key = { it.id }) { activity ->
                        ActivityItemRow(
                            activity = activity,
                            onEdit = { editingActivity = activity},
                            onDelete = { deletingActivity = activity })
                    }
                }
            }
        }
    }

    // --- DIALOGS & SCANNER ---
    if (showAddFoodDialog) AddFoodDialog(
        onDismiss = { showAddFoodDialog = false },
        onAddFood = { name, desc -> viewModel.addFoodItem(name, desc); showAddFoodDialog = false }
    )
    if (showBarcodeScanner) {
        BarcodeScanner(
            onBarcodeScanned = { barcode ->
                viewModel.fetchFoodInfoByBarcode(barcode)
                showBarcodeScanner = false
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }
    uiState.scannedFoodInfo?.let { foodInfo ->
        BarcodeScannerDialog(
            foodInfo = foodInfo,
            onDismiss = { viewModel.clearScannedFoodInfo() }, // Schließt den Dialog
            onConfirm = { grams ->
                // Ruft die neue Funktion auf, um die skalierten Daten zu speichern
                viewModel.addScannedFoodItem(foodInfo, grams)
                viewModel.clearScannedFoodInfo() // Schließt den Dialog nach dem Speichern
            }
        )
    }
    if (showAddActivityDialog) AddActivityDialog(
        onDismiss = { showAddActivityDialog = false },
        onAddActivity = { name -> viewModel.addActivityItem(name); showAddActivityDialog = false }
    )
    deletingFood?.let { food -> DeleteConfirmationDialog(
        itemName = food.name,
        onDismiss = { deletingFood = null },
        onConfirm = { viewModel.deleteFoodItem(food); deletingFood = null }
    )}
    deletingActivity?.let { activity -> DeleteConfirmationDialog(
        itemName = activity.name,
        onDismiss = { deletingActivity = null },
        onConfirm = { viewModel.deleteActivityItem(activity); deletingActivity = null }
    )}
    editingFood?.let { foodToEdit -> EditFoodDialog(
        foodEntry = foodToEdit,
        onDismiss = { editingFood = null },
        onSaveManual = { updatedFood ->
            viewModel.updateFoodItemManual(updatedFood)
            editingFood = null
        },
        onRecalculate = { updatedFood ->
            viewModel.reFetchFoodItem(updatedFood)
            editingFood = null
        }
    )
    }
    editingActivity?.let { activityToEdit -> EditActivityDialog(
        activityEntry = activityToEdit,
        onDismiss = { editingActivity = null },
        onSaveManual = { updatedActivity ->
            viewModel.updateActivityItemManual(updatedActivity)
            editingActivity = null
        },
        onRecalculate = { updatedActivity ->
            viewModel.reFetchActivityItem(updatedActivity)
            editingActivity = null
        }
    )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.changeDate(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Fehler") },
            text = { Text(message) },
            confirmButton = { Button(onClick = { viewModel.dismissError() }) { Text("OK") } }
        )
    }
}

@Composable
fun GoalsSummaryCard(
    netCalories: Int, totalProtein: Int, totalCarbs: Int, totalFat: Int,
    goals: CalorieGoals, onSetGoals: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        if (goals.calories <= 0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp), contentAlignment = Alignment.Center) {
                Button(onClick = onSetGoals) { Text("Setze deine Ziele, um zu starten!") }
            }
        } else {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Netto-Kalorien", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("$netCalories / ${goals.calories} kcal", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(
                    progress = { (netCalories.toFloat() / goals.calories.toFloat()).coerceAtLeast(0f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier
                        .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    MacroProgress("Protein", totalProtein, goals.proteinGrams)
                    MacroProgress("Kohlenh.", totalCarbs, goals.carbsGrams)
                    MacroProgress("Fett", totalFat, goals.fatGrams)
                }
            }
        }
    }
}

@Composable
fun MacroProgress(name: String, current: Int, goal: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val progress = if (goal > 0) (current.toFloat() / goal.toFloat()) else 0f
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Text("$current / ${goal}g", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .width(80.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun FoodItemRow(food: FoodEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("P: ${food.protein}g, K: ${food.carbs}g, F: ${food.fat}g", fontSize = 14.sp, color = Color.Gray)
            }
            Text("${food.calories} kcal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Bearbeiten") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Löschen") }
        }
    }
}

@Composable
fun ActivityItemRow(activity: ActivityEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(activity.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("-${activity.caloriesBurned} kcal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Bearbeiten") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Löschen") }
        }
    }
}

@Composable
fun AddActivityDialog(onDismiss: () -> Unit, onAddActivity: (String) -> Unit) {
    var activityName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aktivität hinzufügen") },
        text = {
            OutlinedTextField(
                value = activityName,
                onValueChange = { activityName = it },
                label = { Text("z.B., '30 Minuten laufen'") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (activityName.isNotBlank()) onAddActivity(activityName) }, enabled = activityName.isNotBlank()) {
                Text("Hinzufügen")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun DeleteConfirmationDialog(itemName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eintrag löschen?") },
        text = { Text("Möchtest du '$itemName' wirklich löschen?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Löschen")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun AddFoodDialog(onDismiss: () -> Unit, onAddFood: (String, String) -> Unit) {
    var foodName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Essen hinzufügen") },
        text = {
            Column {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Essen (z.B., '1 Apfel')") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung (z.B., 'mittelgroß')") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (foodName.isNotBlank()) onAddFood(foodName, description) },
                enabled = foodName.isNotBlank()
            ) { Text("Hinzufügen") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

@Composable
fun BarcodeScannerDialog(
    foodInfo: FoodNutritionInfo,
    onDismiss: () -> Unit,
    onConfirm: (grams: Int) -> Unit
) {
    var grams by remember { mutableStateOf("100") }
    val isGramsValid by remember(grams) {
        derivedStateOf { grams.toIntOrNull() != null && grams.toInt() > 0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(foodInfo.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nährwerte pro 100g:", fontWeight = FontWeight.Bold)
                Text("Kalorien: ${foodInfo.calories} kcal")
                Text("Protein: ${foodInfo.protein}g, Kohlenhydrate: ${foodInfo.carbs}g, Fett: ${foodInfo.fat}g")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it.filter { c -> c.isDigit() } },
                    label = { Text("Gegessene Menge (in Gramm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(grams.toInt()) },
                enabled = isGramsValid
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun EditActivityDialog(activityEntry: ActivityEntry, onDismiss: () -> Unit, onSaveManual: (ActivityEntry) -> Unit,
                       onRecalculate: (ActivityEntry) -> Unit){
    var name by remember { mutableStateOf(activityEntry.name) }
    var caloriesBurned by remember { mutableStateOf(activityEntry.caloriesBurned.toString()) }
    val isFormValid by remember(name, caloriesBurned) {
        derivedStateOf { name.isNotBlank() && caloriesBurned.toIntOrNull() != null }
    }
    val compactButtonPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eintrag bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name der Aktivität") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caloriesBurned,
                        onValueChange = { caloriesBurned = it.filter { c -> c.isDigit() } },
                        label = { Text("kcal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    contentPadding = compactButtonPadding) {
                    Text("Abbrechen")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        val updatedActivity = activityEntry.copy(name = name)
                        onRecalculate(updatedActivity)
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("KI")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val updatedActivity = activityEntry.copy(
                            name = name,
                            caloriesBurned = caloriesBurned.toIntOrNull() ?: activityEntry.caloriesBurned,
                        )
                        onSaveManual(updatedActivity)
                    },
                    enabled = isFormValid,
                    contentPadding = compactButtonPadding
                ) {
                    Text("Speichern")
                }
            }
        },

        dismissButton = {}
    )
}

@Composable
fun EditFoodDialog(
    foodEntry: FoodEntry,
    onDismiss: () -> Unit,
    onSaveManual: (FoodEntry) -> Unit,
    onRecalculate: (FoodEntry) -> Unit
) {
    var name by remember { mutableStateOf(foodEntry.name) }
    var calories by remember { mutableStateOf(foodEntry.calories.toString()) }
    var protein by remember { mutableStateOf(foodEntry.protein.toString()) }
    var carbs by remember { mutableStateOf(foodEntry.carbs.toString()) }
    var fat by remember { mutableStateOf(foodEntry.fat.toString()) }
    val compactButtonPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

    val isFormValid by remember(name, calories, protein, carbs, fat) {
        derivedStateOf {
            name.isNotBlank() &&
                    calories.toIntOrNull() != null &&
                    protein.toDoubleOrNull() != null &&
                    carbs.toDoubleOrNull() != null &&
                    fat.toDoubleOrNull() != null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eintrag bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name der Mahlzeit") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it.filter { c -> c.isDigit() } },
                        label = { Text("kcal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Kohlenh. (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Fett (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    contentPadding = compactButtonPadding) {
                    Text("Abbrechen")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        val updatedFood = foodEntry.copy(name = name)
                        onRecalculate(updatedFood)
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("KI")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val updatedFood = foodEntry.copy(
                            name = name,
                            calories = calories.toIntOrNull() ?: foodEntry.calories,
                            protein = protein.toDoubleOrNull() ?: foodEntry.protein,
                            carbs = carbs.toDoubleOrNull() ?: foodEntry.carbs,
                            fat = fat.toDoubleOrNull() ?: foodEntry.fat
                        )
                        onSaveManual(updatedFood)
                    },
                    enabled = isFormValid,
                    contentPadding = compactButtonPadding
                ) {
                    Text("Speichern")
                }
            }
        },

        dismissButton = {}
    )
}