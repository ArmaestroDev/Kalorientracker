package de.armando.kalorientracker.data

import de.armando.kalorientracker.data.model.CalorieGoals
import de.armando.kalorientracker.data.model.Gender
import de.armando.kalorientracker.data.model.UserProfile
import kotlin.math.roundToInt

object GoalsCalculator {

    fun calculateGoals(profile: UserProfile): CalorieGoals {
        if (profile.weightKg <= 0 || profile.heightCm <= 0 || profile.age <= 0) {
            return CalorieGoals() // Return default/empty if profile is incomplete
        }

        // 1. Calculate Basal Metabolic Rate (BMR) using Harris-Benedict Equation
        val bmr = if (profile.gender == Gender.MALE) {
            88.362 + (13.397 * profile.weightKg) + (4.799 * profile.heightCm) - (5.677 * profile.age)
        } else {
            447.593 + (9.247 * profile.weightKg) + (3.098 * profile.heightCm) - (4.330 * profile.age)
        }

        // 2. Calculate Total Daily Energy Expenditure (TDEE)
        val tdee = bmr * profile.activityLevel.multiplier

        // 3. Adjust for fitness goal and add a safety floor
        val targetCalories = (tdee + profile.goal.calorieModifier).roundToInt().coerceAtLeast(1200)

        // 4. Calculate Macronutrients (example: 40% Carbs, 30% Protein, 30% Fat)
        val proteinGrams = ((targetCalories * 0.30) / 4).roundToInt() // 4 calories per gram of protein
        val carbsGrams = ((targetCalories * 0.40) / 4).roundToInt()   // 4 calories per gram of carbs
        val fatGrams = ((targetCalories * 0.30) / 9).roundToInt()     // 9 calories per gram of fat

        return CalorieGoals(
            calories = targetCalories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams
        )
    }
}