package com.example.kokkibotti

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * RecipeViewModel on sovelluksen "aivot". Se on vastuussa kaikesta datan käsittelystä
 * ja toimii siltana käyttöliittymän (Fragmentit) ja datalähteen (tietokanta) välillä.
 * Perimällä AndroidViewModel-luokan saamme käyttöömme application-kontekstin, jota tarvitaan
 * tietokantayhteyden luomiseen.
 */
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    // DAO (Data Access Object) -oliot, jotka tarjoavat metodit tietokantakyselyille.
    private val recipeDao: RecipeDao
    private val plannedMealDao: PlannedMealDao

    // LiveData-oliot, joita käyttöliittymä "tarkkailee" (observe).
    // Kun tietokannan data muuttuu, nämä oliot ilmoittavat siitä automaattisesti käyttöliittymälle.
    val allRecipes: LiveData<List<Recipe>>
    val allPlannedMeals: LiveData<List<PlannedMeal>>

    init {
        // Luodaan yhteys tietokantaan heti, kun ViewModel luodaan.
        val database = AppDatabase.getDatabase(application)
        // Haetaan viittaukset DAO-olioihin.
        recipeDao = database.recipeDao()
        plannedMealDao = database.plannedMealDao()

        // Alustetaan LiveData-oliot hakemalla data tietokannasta DAO-rajapintojen kautta.
        allRecipes = recipeDao.getAllRecipes()
        allPlannedMeals = plannedMealDao.getAllPlannedMeals()
    }

    // --- Reseptien käsittelyfunktiot ---

    /**
     * Lisää uuden reseptin tietokantaan. Toiminto suoritetaan taustasäikeessä
     * käyttämällä viewModelScope.launch-korutiinia, jotta se ei jumita käyttöliittymää.
     */
    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeDao.insertOrUpdate(recipe)
        }
    }

    /**
     * Päivittää olemassa olevan reseptin tiedot tietokantaan.
     */
    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeDao.updateRecipe(recipe)
        }
    }

    /**
     * Poistaa reseptin. Tärkeää on poistaa ensin kaikki ruokalistasuunnitelmat,
     * jotka viittaavat tähän reseptiin, jotta tietokanta pysyy eheänä.
     */
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            plannedMealDao.deleteAllPlansForRecipe(recipe.id)
            recipeDao.delete(recipe)
        }
    }

    // --- Ruokalistan käsittelyfunktiot ---

    /**
     * Tallentaa (tai päivittää) yhden aterian suunnitelman tietylle viikolle ja päivälle.
     */
    fun planMeal(week: Int, day: Int, recipe: Recipe) {
        val plannedMeal = PlannedMeal(weekNumber = week, dayOfWeek = day, recipeId = recipe.id)
        viewModelScope.launch {
            plannedMealDao.insertOrUpdate(plannedMeal)
        }
    }

    /**
     * Poistaa yksittäisen ateriasuunnitelman (asettaa päivän "tyhjäksi").
     */
    fun unplanMeal(week: Int, day: Int) {
        viewModelScope.launch {
            plannedMealDao.deletePlanForDay(week, day)
        }
    }

    /**
     * Nollaa koko viikon ruokalistan poistamalla kaikki sen suunnitelmat.
     */
    fun resetWeek(week: Int) {
        viewModelScope.launch {
            plannedMealDao.deletePlansForWeek(week)
        }
    }

    /**
     * Hakee tietylle päivälle suunnitellun reseptin LiveData-oliona.
     * Käyttöliittymä voi tarkkailla tätä ja päivittää itsensä automaattisesti.
     */
    fun getRecipeForDay(week: Int, day: Int): LiveData<Recipe?> {
        return plannedMealDao.getRecipeForDay(week, day)
    }
}