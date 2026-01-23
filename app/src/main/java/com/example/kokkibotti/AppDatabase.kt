package com.example.kokkibotti

// Android Context tarvitaan tietokannan luomiseen
import android.content.Context

// Room-kirjaston annotaatiot ja perusluokat
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Määrittelee Room-tietokannan:
// - entities: tietokannan taulut (Recipe ja PlannedMeal)
// - version: tietokannan versio
// - exportSchema: ei tallenneta skeemaa tiedostoon
@Database(entities = [Recipe::class, PlannedMeal::class], version = 1, exportSchema = false)

// Määrittelee TypeConverterin, jota käytetään mm. Ingredient-listojen tallentamiseen
@TypeConverters(IngredientListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // DAO-rajapinta reseptien käsittelyyn
    abstract fun recipeDao(): RecipeDao

    // DAO-rajapinta ateriasuunnitelmien käsittelyyn
    abstract fun plannedMealDao(): PlannedMealDao

    companion object {

        // Volatile varmistaa, että INSTANCE-muuttujan arvo on sama kaikille säikeille
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Palauttaa tietokannan instanssin (Singleton-malli)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Rakennetaan tietokanta, jos sitä ei vielä ole olemassa
                val instance = Room.databaseBuilder(
                    context.applicationContext,   // Käytetään application contextia muistivuotojen estämiseksi
                    AppDatabase::class.java,      // Tietokannan luokka
                    "kokkibotti_database"         // Tietokannan nimi
                ).build()

                // Tallennetaan instanssi myöhempää käyttöä varten
                INSTANCE = instance
                instance
            }
        }
    }
}
