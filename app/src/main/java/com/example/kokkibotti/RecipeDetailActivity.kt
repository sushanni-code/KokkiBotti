package com.example.kokkibotti

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

// Activity, jossa voidaan tarkastella ja muokata yksittäistä reseptiä
class RecipeDetailActivity : AppCompatActivity() {

    // UI-elementit reseptin nimen ja ohjeiden muokkaamiseen
    private lateinit var recipeNameEditText: EditText
    private lateinit var instructionsEditText: EditText
    private lateinit var editButton: ImageButton
    private lateinit var saveButton: Button

    // Ainesosien lista ja RecyclerView
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: EditableIngredientAdapter
    private var currentIngredients = mutableListOf<Ingredient>()

    // Näkymät uuden ainesosan lisäämistä varten
    private lateinit var addIngredientLayout: LinearLayout
    private lateinit var amountEditText: EditText
    private lateinit var unitSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var addIngredientButton: Button

    private var originalRecipe: Recipe? = null // Alkuperäinen resepti intentistä
    private var recipePosition: Int = -1       // Reseptin sijainti listassa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Haetaan kaikki näkymät
        findViews()

        // Haetaan resepti intentistä
        originalRecipe = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("RECIPE", Recipe::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Recipe>("RECIPE")
        }
        recipePosition = intent.getIntExtra("RECIPE_POSITION", -1)

        // Asetetaan UI alkuperäisen reseptin tiedoilla
        originalRecipe?.let {
            recipeNameEditText.setText(it.name)
            instructionsEditText.setText(it.instructions)
            currentIngredients = it.ingredients.toMutableList()
            setupRecyclerView()
        }

        setupUnitSpinner()       // Spinner ainesosan yksiköille
        setupClickListeners()    // Klikkaukset muokkaus- ja tallenna-napeille
    }

    // Hakee kaikki näkymät layoutista
    private fun findViews() {
        recipeNameEditText = findViewById(R.id.text_view_recipe_name_detail)
        instructionsEditText = findViewById(R.id.text_view_instructions_detail)
        editButton = findViewById(R.id.btnMuokkaa)
        saveButton = findViewById(R.id.btnTallenna)
        ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view)

        // Ainesosan lisäämisen näkymät
        addIngredientLayout = findViewById(R.id.add_ingredient_layout)
        amountEditText = findViewById(R.id.edit_text_ingredient_amount)
        unitSpinner = findViewById(R.id.spinner_ingredient_unit)
        nameEditText = findViewById(R.id.edit_text_ingredient_name)
        addIngredientButton = findViewById(R.id.button_add_ingredient)
    }

    // Alustaa RecyclerViewin ja adapterin
    private fun setupRecyclerView() {
        ingredientAdapter = EditableIngredientAdapter(currentIngredients, false) // Alussa ei muokkaustilassa
        ingredientsRecyclerView.adapter = ingredientAdapter
    }

    // Alustaa Spinnerin yksiköille (g, kg, ml, jne.)
    private fun setupUnitSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.units_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unitSpinner.adapter = adapter
        }
    }

    // Asettaa klikku-kuuntelijat muokkaus- ja tallenna-napeille sekä ainesosan lisäykselle
    private fun setupClickListeners() {
        editButton.setOnClickListener { enterEditMode() }
        saveButton.setOnClickListener { saveChangesAndExitEditMode() }
        addIngredientButton.setOnClickListener { addIngredient() }
    }

    // Lisää uuden ainesosan listaan ja RecyclerViewiin
    private fun addIngredient() {
        val amount = amountEditText.text.toString().toDoubleOrNull()
        val unit = unitSpinner.selectedItem.toString()
        val name = nameEditText.text.toString()

        if (amount != null && name.isNotBlank()) {
            currentIngredients.add(Ingredient(amount, unit, name))
            ingredientAdapter.notifyItemInserted(currentIngredients.size - 1)
            amountEditText.text.clear()
            nameEditText.text.clear()
            amountEditText.requestFocus()
        }
    }

    // Siirtyy muokkaustilaan
    private fun enterEditMode() {
        recipeNameEditText.isEnabled = true
        instructionsEditText.isEnabled = true
        
        val editBgColor = androidx.core.content.ContextCompat.getColor(this, R.color.neutral_gray)
        recipeNameEditText.setBackgroundColor(editBgColor)
        instructionsEditText.setBackgroundColor(editBgColor)

        editButton.visibility = View.GONE
        saveButton.visibility = View.VISIBLE
        addIngredientLayout.visibility = View.VISIBLE

        ingredientAdapter.setEditMode(true) // Ainesosalistasta muokattava
    }

    // Tallentaa muutokset ja poistuu muokkaustilasta
    private fun saveChangesAndExitEditMode() {
        recipeNameEditText.isEnabled = false
        instructionsEditText.isEnabled = false
        recipeNameEditText.setBackgroundResource(android.R.color.transparent)
        instructionsEditText.setBackgroundResource(android.R.color.transparent)

        editButton.visibility = View.VISIBLE
        saveButton.visibility = View.GONE
        addIngredientLayout.visibility = View.GONE

        ingredientAdapter.setEditMode(false)

        // Luo kopion muokatusta reseptistä
        val editedRecipe = originalRecipe?.copy(
            name = recipeNameEditText.text.toString(),
            ingredients = currentIngredients,
            instructions = instructionsEditText.text.toString()
        )

        // Lähetetään muokattu resepti takaisin RecipeAdapterille
        val resultIntent = Intent()
        resultIntent.putExtra("EDITED_RECIPE", editedRecipe)
        resultIntent.putExtra("RECIPE_POSITION", recipePosition)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
