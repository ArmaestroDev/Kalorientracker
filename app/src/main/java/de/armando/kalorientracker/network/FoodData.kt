package de.armando.kalorientracker.network

import com.squareup.moshi.Json

// Diese Klassen spiegeln die JSON-Struktur wider

data class ProductResponse(
    @Json(name = "status") val status: Int,
    @Json(name = "product") val product: Product?
)

data class Product(
    // Mappt das JSON-Feld "product_name" auf die Eigenschaft "productName"
    @Json(name = "product_name") val productName: String?,
    @Json(name = "nutriments") val nutriments: Nutriments?
)

data class Nutriments(
    // Mappt das JSON-Feld "energy-kcal_100g" auf die Eigenschaft "energyKcal100g"
    @Json(name = "energy-kcal_100g") val energyKcal100g: Double?,
    // Mappt das JSON-Feld "proteins_100g" auf die Eigenschaft "proteins100g"
    @Json(name = "proteins_100g") val proteins100g: Double?,
    // Mappt das JSON-Feld "carbohydrates_100g" auf die Eigenschaft "carbohydrates100g"
    @Json(name = "carbohydrates_100g") val carbohydrates100g: Double?,
    // Mappt das JSON-Feld "fat_100g" auf die Eigenschaft "fat100g"
    @Json(name = "fat_100g") val fat100g: Double?
)