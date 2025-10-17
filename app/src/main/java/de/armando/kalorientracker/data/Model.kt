package de.armando.kalorientracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,
    var protein: Int,
    val carbs: Int,
    val fat: Int,
    val date: LocalDate
)

@Entity(tableName = "activity_entries")
data class ActivityEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val caloriesBurned: Int,
    val date: LocalDate
)

// Wrapper for a single day's data - Not a database table, just for the UI state
data class DailyLog(
    val foodEntries: List<FoodEntry> = emptyList(),
    val activityEntries: List<ActivityEntry> = emptyList()
)

// --- User Profile and Goals (Stored in SharedPreferences) ---

@Serializable
data class UserProfile(
    val apikey: String = "",
    val age: Int = 0,
    val weightKg: Double = 0.0,
    val heightCm: Double = 0.0,
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val goal: FitnessGoal = FitnessGoal.MAINTAIN_WEIGHT
)

@Serializable
data class CalorieGoals(
    val calories: Int = 0,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0
)

enum class Gender { MALE, FEMALE }

enum class ActivityLevel(val multiplier: Double, val description: String) {
    SEDENTARY(1.2, "Sitzend (wenig oder kein Training)"),
    LIGHTLY_ACTIVE(1.375, "Leicht aktiv (1-3 Tage/Woche)"),
    MODERATELY_ACTIVE(1.55, "Mäßig aktiv (3-5 Tage/Woche)"),
    VERY_ACTIVE(1.725, "Sehr aktiv (6-7 Tage/Woche)"),
    EXTRA_ACTIVE(1.9, "Extrem aktiv (sehr hartes Training)")
}

enum class FitnessGoal(val calorieModifier: Int, val description: String) {
    LOSE_WEIGHT(-500, "Gewicht verlieren (-500 kcal Defizit)"),
    MAINTAIN_WEIGHT(0, "Gewicht halten"),
    GAIN_WEIGHT(500, "Gewicht zunehmen (+500 kcal Überschuss)")
}

// --- API Response Models ---

@Serializable
data class FoodNutritionInfo(
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Serializable
data class ActivityInfo(
    val name: String,
    val calories_burned: Int
)