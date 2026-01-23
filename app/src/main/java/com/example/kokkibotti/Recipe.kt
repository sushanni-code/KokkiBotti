package com.example.kokkibotti

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

// --- ENTITEETIT (TIETOKANTARAKENTEET) ---

/**
 * Recipe-luokka on sovelluksen datamallin ydin.
 * @Entity-annotaatio kertoo Room-tietokannalle, että tämä luokka on tietokantataulu nimeltä "recipes".
 * @TypeConverters-annotaatio kertoo Roomille, miten monimutkainen 'ingredients'-lista käsitellään.
 * @Parcelize-annotaatio mahdollistaa reseptiolioiden välittämisen aktiviteettien välillä (esim. listalta muokkausnäkymään).
 */
@Parcelize
@Entity(tableName = "recipes")
@TypeConverters(IngredientListConverter::class)
data class Recipe(
    // @PrimaryKey-annotaatio määrittelee tämän kentän taulun pääavaimeksi.
    // autoGenerate = true saa tietokannan luomaan automaattisesti yksilöllisen ID:n jokaiselle uudelle reseptille.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ingredients: List<Ingredient>,
    val instructions: String
) : Parcelable

/**
 * Ingredient-luokka ei ole oma tietokantataulunsa, vaan se tallennetaan osana Recipe-oliota.
 * @Parcelize mahdollistaa sen välittämisen aktiviteettien välillä.
 */
@Parcelize
data class Ingredient(
    val amount: Double,
    val unit: String,
    val name: String
) : Parcelable


// --- TYYPPIMUUNNIN (TYPE CONVERTER) ROOM-KIRJASTOLLE ---

/**
 * Room osaa tallentaa vain yksinkertaisia tietotyyppejä (tekstiä, numeroita). Se ei ymmärrä, miten List<Ingredient> tallennettaisiin.
 * Tämä apuluokka "opettaa" Roomille, miten ainesosalista muunnetaan tekstiksi (JSON) ja takaisin.
 */
class IngredientListConverter {
    /**
     * Muuntaa List<Ingredient>-olion JSON-muotoiseksi merkkijonoksi tallennusta varten.
     */
    @androidx.room.TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.toJson(value, type)
    }

    /**
     * Muuntaa tietokannasta luetun JSON-merkkijonon takaisin List<Ingredient>-olioksi.
     */
    @androidx.room.TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        val gson = Gson()
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson(value, type)
    }
}