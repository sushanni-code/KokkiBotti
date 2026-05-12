package com.example.kokkibotti

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.launch

class InspirationFragment : Fragment() {

    private val recipeViewModel: RecipeViewModel by activityViewModels {
        RecipeViewModelFactory(requireActivity().application)
    }

    private lateinit var geminiService: GeminiService
    private var lastTranslatedRecipe: Recipe? = null

    private var currentMeal: Meal? = null

    private lateinit var loadingSpinner: ProgressBar
    private lateinit var newRecipeButton: Button
    private lateinit var saveRecipeButton: Button
    private lateinit var mealImage: ImageView
    private lateinit var mealName: TextView
    private lateinit var mealIngredients: TextView
    private lateinit var mealInstructions: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inspiration, container, false)

        loadingSpinner = view.findViewById(R.id.loading_spinner)
        newRecipeButton = view.findViewById(R.id.new_recipe_button)
        saveRecipeButton = view.findViewById(R.id.save_recipe_button)
        mealImage = view.findViewById(R.id.meal_image)
        mealName = view.findViewById(R.id.meal_name)
        mealIngredients = view.findViewById(R.id.meal_ingredients)
        mealInstructions = view.findViewById(R.id.meal_instructions)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

        newRecipeButton.setOnClickListener {
            fetchRandomMeal()
        }

        saveRecipeButton.setOnClickListener {
            lastTranslatedRecipe?.let {
                recipeViewModel.addRecipe(it)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.ai_save_success, it.name),
                    Toast.LENGTH_SHORT
                ).show()
                saveRecipeButton.isVisible = false
            }
        }
    }

    private fun fetchRandomMeal() {
        lifecycleScope.launch {
            loadingSpinner.isVisible = true
            saveRecipeButton.isVisible = false

            try {
                val response = mealApiService.getRandomMeal()

                response.meals.firstOrNull()?.let { meal ->
                    currentMeal = meal
                    
                    val ingredientsText = formatIngredients(meal)
                    val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "fi"
                    
                    val translatedRecipe = geminiService.translateAndConvertRecipe(
                        meal.name,
                        ingredientsText,
                        meal.instructions,
                        currentLang
                    )
                    
                    lastTranslatedRecipe = translatedRecipe
                    updateUiWithTranslatedRecipe(translatedRecipe, meal.thumbnailUrl, currentLang)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.insp_fetch_error, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }

            loadingSpinner.isVisible = false
        }
    }

    private fun updateUiWithTranslatedRecipe(recipe: Recipe, imageUrl: String, language: String) {
        mealImage.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }

        mealName.text = recipe.name
        
        val fullText = recipe.instructions
        val instrDelimiter = if (language == "en") "INSTRUCTIONS:" else "OHJEET:"
        val ingrDelimiter = if (language == "en") "INGREDIENTS:" else "AINESOSAT:"

        if (fullText.contains(instrDelimiter, ignoreCase = true)) {
            val parts = fullText.split(Regex(instrDelimiter, RegexOption.IGNORE_CASE))
            val topPart = parts[0]
            val ingredientsPart = if (topPart.contains(ingrDelimiter, ignoreCase = true)) {
                topPart.substringAfter(ingrDelimiter).trim()
            } else {
                topPart.trim()
            }
            
            mealIngredients.text = ingredientsPart
            mealInstructions.text = parts[1].trim()
        } else {
            mealIngredients.text = ""
            mealInstructions.text = fullText
        }

        saveRecipeButton.isVisible = true
    }

    private fun formatIngredients(meal: Meal): String {
        val ingredients = mutableListOf<String>()
        val properties = listOf(
            meal.ingredient1 to meal.measure1, meal.ingredient2 to meal.measure2,
            meal.ingredient3 to meal.measure3, meal.ingredient4 to meal.measure4,
            meal.ingredient5 to meal.measure5, meal.ingredient6 to meal.measure6,
            meal.ingredient7 to meal.measure7, meal.ingredient8 to meal.measure8,
            meal.ingredient9 to meal.measure9, meal.ingredient10 to meal.measure10,
            meal.ingredient11 to meal.measure11, meal.ingredient12 to meal.measure12,
            meal.ingredient13 to meal.measure13, meal.ingredient14 to meal.measure14,
            meal.ingredient15 to meal.measure15, meal.ingredient16 to meal.measure16,
            meal.ingredient17 to meal.measure17, meal.ingredient18 to meal.measure18,
            meal.ingredient19 to meal.measure19, meal.ingredient20 to meal.measure20
        )

        for ((ingredient, measure) in properties) {
            if (!ingredient.isNullOrBlank()) {
                ingredients.add("- ${measure?.trim() ?: ""} ${ingredient.trim()}".trim())
            }
        }
        return ingredients.joinToString("\n")
    }
}