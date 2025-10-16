package de.armando.kalorientracker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.armando.kalorientracker.data.model.CalorieGoals
import de.armando.kalorientracker.data.model.UserProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEY_THEME = "selected_theme"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_CALORIE_GOALS = "calorie_goals"
        const val DEFAULT_THEME = "Default"
    }

    // --- Theme ---
    fun saveTheme(themeName: String) {
        prefs.edit { putString(KEY_THEME, themeName) }
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    // --- User Profile and Goals ---
    fun saveUserProfile(profile: UserProfile) {
        val jsonString = json.encodeToString(profile)
        prefs.edit { putString(KEY_USER_PROFILE, jsonString) }
    }

    fun loadUserProfile(): UserProfile {
        val jsonString = prefs.getString(KEY_USER_PROFILE, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString<UserProfile>(jsonString)
            } catch (e: Exception) {
                UserProfile() // Return default on parsing error
            }
        } else {
            UserProfile() // Return default empty profile
        }
    }

    fun saveCalorieGoals(goals: CalorieGoals) {
        val jsonString = json.encodeToString(goals)
        prefs.edit { putString(KEY_CALORIE_GOALS, jsonString) }
    }

    fun loadCalorieGoals(): CalorieGoals {
        val jsonString = prefs.getString(KEY_CALORIE_GOALS, null)
        return if (jsonString != null) {
            try {
                json.decodeFromString<CalorieGoals>(jsonString)
            } catch (e: Exception) {
                CalorieGoals() // Return default on parsing error
            }
        } else {
            CalorieGoals() // Return default goals
        }
    }
}