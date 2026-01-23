package com.example.kokkibotti

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * AddRecipeActivity on aktiviteetti uuden reseptin luomista varten.
 * Se sisältää kentät reseptin nimelle, ainesosille ja valmistusohjeille.
 */
class AddRecipeActivity : AppCompatActivity() {

    // Tähän listaan kerätään käyttäjän syöttämät ainesosat ennen tallennusta.
    private val ingredients = mutableListOf<Ingredient>()

    // RecyclerView'n adapteri, joka vastaa ainesosien näyttämisestä listassa.
    private lateinit var ingredientAdapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Alustetaan käyttöliittymän komponentit
        setupUnitSpinner()
        setupRecyclerView()
        setupClickListeners()
    }

    /**
     * Alustaa yksikkövalikon (Spinner) ja täyttää sen arvoilla (dl, g, kpl, jne.)
     * arrays.xml-resurssitiedostosta.
     */
    private fun setupUnitSpinner() {
        val spinner: Spinner = findViewById(R.id.spinner_ingredient_unit)
        ArrayAdapter.createFromResource(
            this,
            R.array.units_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    /**
     * Alustaa RecyclerView'n ainesosien listan näyttämistä varten.
     */
    private fun setupRecyclerView() {
        val ingredientRecyclerView: RecyclerView = findViewById(R.id.recycler_view_ingredients)
        ingredientRecyclerView.layoutManager = LinearLayoutManager(this)
        ingredientAdapter = IngredientAdapter(ingredients)
        ingredientRecyclerView.adapter = ingredientAdapter
    }

    /**
     * Asettaa kuuntelijat "Lisää ainesosa"- ja "Tallenna resepti" -painikkeille.
     */
    private fun setupClickListeners() {
        val addIngredientButton: Button = findViewById(R.id.button_add_ingredient)
        addIngredientButton.setOnClickListener {
            addIngredient()
        }

        val saveButton: Button = findViewById(R.id.button_save_recipe)
        saveButton.setOnClickListener {
            saveRecipe()
        }
    }

    /**
     * Lukee ainesosan tiedot syöttökentistä, validoi ne ja lisää ne `ingredients`-listaan.
     * Päivittää myös RecyclerView'n näyttämään uuden ainesosan.
     */
    private fun addIngredient() {
        val amountEditText: EditText = findViewById(R.id.edit_text_ingredient_amount)
        val unitSpinner: Spinner = findViewById(R.id.spinner_ingredient_unit)
        val nameEditText: EditText = findViewById(R.id.edit_text_ingredient_name)

        val amount = amountEditText.text.toString().toDoubleOrNull()
        val unit = unitSpinner.selectedItem.toString()
        val name = nameEditText.text.toString()

        // Varmistetaan, että määrä ja nimi eivät ole tyhjiä.
        if (amount != null && name.isNotBlank()) {
            ingredients.add(Ingredient(amount, unit, name))
            // Ilmoitetaan adapterille tehokkaasti vain yhden uuden kohteen lisäyksestä.
            ingredientAdapter.notifyItemInserted(ingredients.size - 1)

            // Tyhjennetään kentät seuraavaa lisäystä varten.
            amountEditText.text.clear()
            nameEditText.text.clear()
            amountEditText.requestFocus() // Asetetaan fokus määrä-kenttään
        }
    }

    /**
     * Lukee reseptin nimen ja ohjeet, luo Recipe-olion ja palauttaa sen
     * kutsuvalle aktiviteetille (RecipesFragment) tallennettavaksi tietokantaan.
     */
    private fun saveRecipe() {
        val recipeNameEditText: EditText = findViewById(R.id.edit_text_recipe_name)
        val instructionsEditText: EditText = findViewById(R.id.edit_text_instructions)

        val recipeName = recipeNameEditText.text.toString()
        val instructions = instructionsEditText.text.toString()

        // Varmistetaan, että reseptillä on nimi, ohjeet ja vähintään yksi ainesosa.
        if (recipeName.isNotBlank() && ingredients.isNotEmpty() && instructions.isNotBlank()) {
            // Luodaan uusi Recipe-olio. Käytetään nimettyjä argumentteja selkeyden ja
            // turvallisuuden vuoksi, koska Recipe-luokalla on nyt myös id-kenttä.
            val newRecipe = Recipe(
                name = recipeName,
                ingredients = ingredients,
                instructions = instructions
            )

            // Luodaan Intent, johon pakataan uusi resepti ja palautetaan se onnistumisen merkiksi.
            val resultIntent = Intent()
            resultIntent.putExtra("NEW_RECIPE", newRecipe)
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Suljetaan tämä aktiviteetti ja palataan edelliseen.
        }
    }
}

/**
 * Yksinkertainen RecyclerView Adapter, joka näyttää listan ainesosia tekstimuodossa.
 */
class IngredientAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        // Käytetään Androidin sisäänrakennettua yksinkertaista listanäkymää.
        val itemView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return IngredientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        // Muotoillaan ainesosan tiedot yhdeksi riviksi tekstiä.
        holder.textView.text = "${ingredient.amount} ${ingredient.unit} ${ingredient.name}"
    }

    override fun getItemCount() = ingredients.size

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Haetaan viittaus tekstikenttään, johon ainesosan tiedot kirjoitetaan.
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }
}