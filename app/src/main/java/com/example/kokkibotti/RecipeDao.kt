package com.example.kokkibotti

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Data Access Object (DAO) resepteille Room-tietokannassa
@Dao
interface RecipeDao {

    // Lisää uuden reseptin tietokantaan tai päivittää sen, jos sama resepti on jo olemassa
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(recipe: Recipe)

    // Hakee kaikki reseptit aakkosjärjestyksessä nimen mukaan
    // Palauttaa LiveData-listan, jotta UI voi kuunnella muutoksia automaattisesti
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): LiveData<List<Recipe>>

    // Päivittää olemassa olevan reseptin tiedot
    @Update
    suspend fun updateRecipe(recipe: Recipe)

    // Poistaa tietyn reseptin tietokannasta
    @Delete
    suspend fun delete(recipe: Recipe)
}
