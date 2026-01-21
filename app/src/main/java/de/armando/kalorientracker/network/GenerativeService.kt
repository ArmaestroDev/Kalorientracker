package de.armando.kalorientracker.network

interface GenerativeService {
    suspend fun getApiResponse(prompt: String): String?
}
