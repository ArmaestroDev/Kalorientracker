package de.armando.kalorientracker

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.armando.kalorientracker.data.*
import de.armando.kalorientracker.data.db.AppDatabase
import de.armando.kalorientracker.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class MainUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDayLog: DailyLog = DailyLog(),
    val userProfile: UserProfile = UserProfile(),
    val goals: CalorieGoals = CalorieGoals(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val scannedFoodInfo: FoodNutritionInfo? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var apiServiceRepository: ApiServiceRepository
    private val prefsRepository = UserPreferencesRepository(application)
    private val logDao = AppDatabase.getDatabase(application).logDao()
    private val logRepository = LogRepository(logDao)

    private val _uiState = MutableStateFlow(MainUiState())
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val _dailyLogStream = _selectedDate.flatMapLatest { date ->
        logRepository.getDailyLog(date)
    }

    val uiState: StateFlow<MainUiState> = combine(
        _uiState, _selectedDate, _dailyLogStream
    ) { state, date, log ->
        state.copy(
            selectedDate = date,
            selectedDayLog = log
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = prefsRepository.loadUserProfile()
            initializeApiService(profile.apikey)
            val goals = prefsRepository.loadCalorieGoals()
            _uiState.update {
                it.copy(
                    userProfile = profile,
                    goals = goals,
                    isLoading = false
                )
            }
        }
    }

    private fun initializeApiService(apiKey: String) {
        val geminiApiService = GeminiApiService(apiKey)
        apiServiceRepository = ApiServiceRepository(geminiApiService)
    }

    // NEU: Hilfsfunktion, die prüft, ob der API-Schlüssel fehlt.
    private fun isApiKeyMissing(): Boolean {
        if (_uiState.value.userProfile.apikey.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Bitte gib zuerst deinen API-Schlüssel im Profil ein.") }
            return true
        }
        return false
    }

    fun changeDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }

    fun addFoodItem(foodName: String, description: String) {
        // HINZUGEFÜGT: Überprüfung vor dem API-Aufruf
        if (isApiKeyMissing()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val nutritionInfo = apiServiceRepository.fetchFoodNutrition(foodName, description)
                if (nutritionInfo != null && nutritionInfo.calories > 0) {
                    val newEntry = FoodEntry(
                        name = nutritionInfo.name,
                        calories = (nutritionInfo.calories),
                        protein = (nutritionInfo.protein).toInt(),
                        carbs = (nutritionInfo.carbs).toInt(),
                        fat = (nutritionInfo.fat).toInt(),
                        date = _selectedDate.value
                    )
                    logRepository.addFoodEntry(newEntry)
                } else {
                    _uiState.update { it.copy(errorMessage = "Nährwertdaten konnten nicht abgerufen werden.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ein unerwarteter Fehler ist aufgetreten: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fetchFoodInfoByBarcode(code: String) {
        // Diese Funktion verwendet nicht die KI-API, daher keine Überprüfung nötig.
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val nutritionInfo = apiServiceRepository.fetchBarCodeNutrition(code)
                if (nutritionInfo != null && nutritionInfo.calories >= 0) {
                    _uiState.update { it.copy(isLoading = false, scannedFoodInfo = nutritionInfo) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Produkt nicht gefunden oder keine Nährwertdaten verfügbar.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Ein unerwarteter Fehler ist aufgetreten: ${e.message}") }
            }
        }
    }

    fun addScannedFoodItem(foodInfo: FoodNutritionInfo, grams: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val factor = grams / 100.0
            val newEntry = FoodEntry(
                name = "${foodInfo.name} (${grams}g)",
                calories = (foodInfo.calories * factor).toInt(),
                protein = (foodInfo.protein * factor).toInt(),
                carbs = (foodInfo.carbs * factor).toInt(),
                fat = (foodInfo.fat * factor).toInt(),
                date = _selectedDate.value
            )
            logRepository.addFoodEntry(newEntry)
        }
    }

    fun clearScannedFoodInfo() {
        _uiState.update { it.copy(scannedFoodInfo = null) }
    }

    fun addActivityItem(activityName: String) {
        // HINZUGEFÜGT: Überprüfung vor dem API-Aufruf
        if (isApiKeyMissing()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val activityInfo = apiServiceRepository.fetchActivityCalories(activityName)
                if (activityInfo != null && activityInfo.calories_burned > 0) {
                    val newEntry = ActivityEntry(
                        name = activityName,
                        caloriesBurned = activityInfo.calories_burned,
                        date = _selectedDate.value
                    )
                    logRepository.addActivityEntry(newEntry)
                } else {
                    _uiState.update { it.copy(errorMessage = "Verbrannte Kalorien konnten nicht geschätzt werden.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ein unerwarteter Fehler ist aufgetreten: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reFetchFoodItem(foodEntry: FoodEntry) {
        // HINZUGEFÜGT: Überprüfung vor dem API-Aufruf
        if (isApiKeyMissing()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val nutritionInfo = apiServiceRepository.fetchFoodNutrition(foodEntry.name, "")
                if (nutritionInfo != null && nutritionInfo.calories > 0) {
                    val updatedEntry = foodEntry.copy(
                        name = nutritionInfo.name,
                        calories = (nutritionInfo.calories),
                        protein = (nutritionInfo.protein).toInt(),
                        carbs = (nutritionInfo.carbs).toInt(),
                        fat = (nutritionInfo.fat).toInt(),
                    )
                    logRepository.updateFoodEntry(updatedEntry)
                } else {
                    _uiState.update { it.copy(errorMessage = "Nährwertdaten konnten nicht abgerufen werden.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ein unerwarteter Fehler ist aufgetreten: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reFetchActivityItem(activityEntry: ActivityEntry) {
        // HINZUGEFÜGT: Überprüfung vor dem API-Aufruf
        if (isApiKeyMissing()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val caloriesInfo = apiServiceRepository.fetchActivityCalories(activityEntry.name)
                if (caloriesInfo != null && caloriesInfo.calories_burned > 0) {
                    val updatedEntry = activityEntry.copy(
                        name = caloriesInfo.name,
                        caloriesBurned = caloriesInfo.calories_burned
                    )
                    logRepository.updateActivityEntry(updatedEntry)
                } else {
                    _uiState.update { it.copy(errorMessage = "Kaloriendaten konnten nicht abgerufen werden.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ein unerwarteter Fehler ist aufgetreten: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateFoodItemManual(foodEntry: FoodEntry) {
        viewModelScope.launch(Dispatchers.IO) { logRepository.updateFoodEntry(foodEntry) }
    }

    fun updateActivityItemManual(activityEntry: ActivityEntry) {
        viewModelScope.launch(Dispatchers.IO) { logRepository.updateActivityEntry(activityEntry) }
    }

    fun deleteFoodItem(foodEntry: FoodEntry) {
        viewModelScope.launch(Dispatchers.IO) { logRepository.deleteFoodEntry(foodEntry) }
    }

    fun deleteActivityItem(activityEntry: ActivityEntry) {
        viewModelScope.launch(Dispatchers.IO) { logRepository.deleteActivityEntry(activityEntry) }
    }

    fun saveUserProfileAndRecalculateGoals(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("GOALS_DEBUG", "Received Profile in ViewModel: $profile")
            initializeApiService(profile.apikey)
            val goals = GoalsCalculator.calculateGoals(profile)
            Log.d("GOALS_DEBUG", "Calculated Goals: $goals")
            prefsRepository.saveUserProfile(profile)
            prefsRepository.saveCalorieGoals(goals)
            _uiState.update { it.copy(userProfile = profile, goals = goals) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}