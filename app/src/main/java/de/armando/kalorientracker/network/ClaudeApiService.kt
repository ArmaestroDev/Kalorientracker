package de.armando.kalorientracker.network

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class ClaudeApiService(private val apiKey: String) : GenerativeService {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.anthropic.com/")
        // Removed MoshiConverterFactory to do manual parsing
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        // Content-Type is set by RequestBody
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .build()

    private val service = retrofit.create(ClaudeApi::class.java)

    override suspend fun getApiResponse(prompt: String): String? {
        return try {
            val requestBodyObj = ClaudeRequest(
                messages = listOf(Message("user", prompt))
            )
            // Manual serialization
            val jsonBody = jsonParser.encodeToString(ClaudeRequest.serializer(), requestBodyObj)
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)

            val responseBody = service.sendMessage(requestBody)
            
            // Manual deserialization
            val responseString = responseBody.string()
            Log.d("ClaudeResponse", "Raw response: $responseString")
            
            val responseObj = jsonParser.decodeFromString<ClaudeResponse>(responseString)
            val content = responseObj.content.firstOrNull()?.text
            content
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("ClaudeError", "HTTP Error: ${e.code()} - $errorBody", e)
            // Return null or throw custom exception if needed, but for now log it clearly
            // We can't easily return the error message to the UI from here without changing the interface return type
            // But the log will help. To help the user, we throw a RuntimeException that MainViewModel catches
            throw RuntimeException("Claude API Error: ${e.code()} - $errorBody")
        } catch (e: Exception) {
            Log.e("ClaudeError", "Error fetching data from Claude API", e)
            throw e // Re-throw to let ViewModel show the error
        }
    }
}

interface ClaudeApi {
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: RequestBody): ResponseBody
}

@Serializable
data class ClaudeRequest(
    val model: String = "claude-4-5-sonnet",
    val max_tokens: Int = 1024,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeResponse(
    val content: List<Content>
)

@Serializable
data class Content(
    val text: String
)
