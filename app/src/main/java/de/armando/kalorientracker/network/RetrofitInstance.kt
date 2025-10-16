package de.armando.kalorientracker.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://world.openfoodfacts.org/"

    // Moshi-Instanz zum Parsen von JSON
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Logging Interceptor zum Debuggen von Anfragen
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp-Client mit dem Logger
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Retrofit-Instanz, die alles zusammenfügt
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Fügt den Client mit Logger hinzu
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Der API-Service, den wir im ViewModel verwenden werden
    val api: FoodApiService by lazy {
        retrofit.create(FoodApiService::class.java)
    }
}