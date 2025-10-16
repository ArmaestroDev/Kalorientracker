package de.armando.kalorientracker

import android.util.Log
import de.armando.kalorientracker.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.json.Json

class GeminiApiService(apiKey: String) {

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        safetySettings = this.safetySettings
    )

    val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun getApiResponse(prompt: String): String? {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.also {
                Log.d("GeminiResponse", "Raw JSON from API: $it")
            }
        } catch (e: Exception) {
            Log.e("GeminiError", "Error fetching data from API", e)
            null
        }
    }

    inline fun <reified T> parseJson(jsonString: String): T? {
        return try {
            jsonParser.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            Log.e("JsonParsingError", "Error parsing JSON: $jsonString", e)
            null
        }
    }
}