package de.armando.kalorientracker.data

import de.armando.kalorientracker.data.model.ActivityInfo
import de.armando.kalorientracker.data.model.FoodNutritionInfo
import de.armando.kalorientracker.network.GenerativeService
import kotlinx.serialization.json.Json
import de.armando.kalorientracker.network.FoodApiService
import de.armando.kalorientracker.network.RetrofitInstance
import kotlinx.serialization.InternalSerializationApi

class ApiServiceRepository(private val apiService: GenerativeService) {
    // Initialisiert den API-Service für die Barcode-Abfrage
    private val foodApiService: FoodApiService = RetrofitInstance.api
    private val jsonParser = Json { ignoreUnknownKeys = true }

    @OptIn(InternalSerializationApi::class)
    suspend fun fetchFoodNutrition(foodName: String, description: String): FoodNutritionInfo? {
        val prompt = buildFoodPrompt(foodName, description)
        val jsonString = apiService.getApiResponse(prompt)
        return jsonString?.let { parseJson<FoodNutritionInfo>(it) }
    }

    suspend fun fetchBarCodeNutrition(code: String): FoodNutritionInfo? {
        return try {
            // Führt den Netzwerkaufruf über Retrofit aus
            val response = foodApiService.getProductByBarcode(code)

            // Prüft, ob das Produkt gefunden wurde (status == 1)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                val nutriments = product.nutriments

                // Erstellt das FoodNutritionInfo-Objekt mit den Daten aus der API-Antwort
                FoodNutritionInfo(
                    name = product.productName ?: "Unbekanntes Produkt",
                    calories = nutriments?.energyKcal100g?.toInt() ?: 0,
                    protein = nutriments?.proteins100g ?: 0.0,
                    carbs = nutriments?.carbohydrates100g ?: 0.0,
                    fat = nutriments?.fat100g ?: 0.0
                )
            } else {
                null // Produkt nicht gefunden
            }
        } catch (e: Exception) {
            // Fängt Fehler ab (z.B. Netzwerkprobleme)
            e.printStackTrace()
            null
        }
    }


    @OptIn(InternalSerializationApi::class)
    suspend fun fetchActivityCalories(activityName: String): ActivityInfo? {
        val prompt = buildActivityPrompt(activityName)
        val jsonString = apiService.getApiResponse(prompt)
        return jsonString?.let { parseJson<ActivityInfo>(it) }
    }

    private inline fun <reified T> parseJson(jsonString: String): T? {
        return try {
            jsonParser.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildFoodPrompt(foodName: String, description: String): String {
        return """
            You are a nutrition analysis assistant. Respond ONLY with a valid JSON object.
            The JSON object must have this exact structure:
            {"name": "string", "calories": integer, "protein": double, "carbs": double, "fat": double}
            
            RULES:
            1. NEVER respond with anything other than the JSON object. Do not add text like "Here is the JSON:".
            2. If the input is not a food, return a JSON object with all values set to 0, e.g., {"name": "Unknown", "calories": 0, "protein": 0.0, "carbs": 0.0, "fat": 0.0}.
            3. Capitalize the name of the food in the 'name' field.

            USER INPUT:
            Food Name: "$foodName"
            Description: "$description"
        """.trimIndent()
    }



    private fun buildActivityPrompt(activityName: String): String {
        return """
            You are a fitness analysis assistant. Your task is to estimate the calories burned for a given activity. Respond ONLY with a valid JSON object.
            Assume the activity is performed by an average person.
            
            The JSON object must have this exact structure:
            {"name": "string", "calories_burned": integer}

            EXAMPLE:
            User Input: "30 minute run"
            Your Response:
            {"name": "30 Minute Run", "calories_burned": 300}

            RULES:
            1. NEVER respond with anything other than the JSON object.
            2. If the input is not a recognizable activity, return a JSON object with calories_burned set to 0. e.g. {"name": "Unknown Activity", "calories_burned": 0}.
            3. Capitalize the name of the activity in the 'name' field.

            USER INPUT:
            Activity: "$activityName"
        """.trimIndent()
    }
}