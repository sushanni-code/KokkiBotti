package com.example.kokkibotti

import androidx.room.Entity

@Entity(tableName = "planned_meals", primaryKeys = ["weekNumber", "dayOfWeek"])
data class PlannedMeal(
    val weekNumber: Int,    // Esim. 1, 2, 3, 4
    val dayOfWeek: Int,     // Esim. 1 (Maanantai), 2 (Tiistai), ...
    val recipeId: Int       // Viittaus Recipe-taulun id:hen
)