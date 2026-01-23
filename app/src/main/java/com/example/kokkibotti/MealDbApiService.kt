package com.example.kokkibotti

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Määritellään Retrofit-instanssi ja base URL
private val retrofit = Retrofit.Builder()
    .baseUrl("https://www.themealdb.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// 2. Määritellään rajapinta API-kutsuille
interface MealDbApiService {
    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): MealDbResponse
}

// 3. Luodaan ja tarjotaan julkisesti yksi ainoa instanssi palvelusta
val mealApiService: MealDbApiService = retrofit.create(MealDbApiService::class.java)