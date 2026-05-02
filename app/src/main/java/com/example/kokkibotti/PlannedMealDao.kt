package com.example.kokkibotti

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Tämä rajapinta määrittelee tietokantaoperaatiot PlannedMeal-oliolle Room-kirjaston avulla
@Dao
interface PlannedMealDao {

    // Lisää uuden suunnitellun aterian tietokantaan
    // Jos sama ateria (sama primary key) on jo olemassa, se korvataan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(plannedMeal: PlannedMeal)

    // Hakee kaikki suunnitellut ateriat LiveData-muodossa, jotta UI voi kuunnella muutoksia
    @Query("SELECT * FROM planned_meals")
    fun getAllPlannedMeals(): LiveData<List<PlannedMeal>>

    // Hakee tietyn päivän reseptin viikon ja viikonpäivän perusteella
    // Käytetään LEFT JOINia, jotta "Syö ulkona" (id = -1) ja poistetut reseptit voidaan käsitellä
    @Query("""
        SELECT p.recipeId AS id, 
               CASE WHEN p.recipeId = -1 THEN 'Syö ulkona' ELSE r.name END AS name,
               COALESCE(r.ingredients, '[]') AS ingredients,
               COALESCE(r.instructions, '') AS instructions
        FROM planned_meals p
        LEFT JOIN recipes r ON p.recipeId = r.id
        WHERE p.weekNumber = :week AND p.dayOfWeek = :day
    """)
    fun getRecipeForDay(week: Int, day: Int): LiveData<Recipe?>

    // Poistaa kaikki suunnitelmat, joissa käytetään tiettyä reseptiä
    @Query("DELETE FROM planned_meals WHERE recipeId = :recipeId")
    suspend fun deleteAllPlansForRecipe(recipeId: Int)

    // Poistaa suunnitelman tietylle päivälle
    @Query("DELETE FROM planned_meals WHERE weekNumber = :week AND dayOfWeek = :day")
    suspend fun deletePlanForDay(week: Int, day: Int)

    // Poistaa kaikki suunnitelmat tietylle viikolle
    @Query("DELETE FROM planned_meals WHERE weekNumber = :week")
    suspend fun deletePlansForWeek(week: Int)
}
