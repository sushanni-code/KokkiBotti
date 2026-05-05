package com.example.kokkibotti

// Androidin perusluokat fragmentin elinkaareen ja näkymiin
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// UI-komponentit
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

// Apuluokkia ja AndroidX-kirjastoja
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope

// Coil-kirjasto kuvien lataamiseen
import coil.load

// Coroutines
import kotlinx.coroutines.launch

// Fragmentti, joka hakee ja näyttää satunnaisen reseptin inspiraatiota varten


class InspirationFragment : Fragment() {

    // Jaettu ViewModel activity-tasolla, jotta data säilyy fragmenttien välillä
    private val recipeViewModel: RecipeViewModel by activityViewModels {
        RecipeViewModelFactory(requireActivity().application)
    }

    private lateinit var geminiService: GeminiService
    private var lastTranslatedRecipe: Recipe? = null

    // Tällä hetkellä näytettävä ateria
    private var currentMeal: Meal? = null

    // UI-komponenttien muuttujat
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var newRecipeButton: Button
    private lateinit var saveRecipeButton: Button
    private lateinit var mealImage: ImageView
    private lateinit var mealName: TextView
    private lateinit var mealIngredients: TextView
    private lateinit var mealInstructions: TextView

    // Fragmentin näkymän luonti
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflatoidaan fragment_inspiration.xml
        val view = inflater.inflate(R.layout.fragment_inspiration, container, false)

        // Alustetaan UI-komponentit layoutista
        loadingSpinner = view.findViewById(R.id.loading_spinner)
        newRecipeButton = view.findViewById(R.id.new_recipe_button)
        saveRecipeButton = view.findViewById(R.id.save_recipe_button)
        mealImage = view.findViewById(R.id.meal_image)
        mealName = view.findViewById(R.id.meal_name)
        mealIngredients = view.findViewById(R.id.meal_ingredients)
        mealInstructions = view.findViewById(R.id.meal_instructions)

        return view
    }

    // Kutsutaan, kun näkymä on luotu
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Alustetaan Gemini-palvelu
        geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

        // Hakee uuden satunnaisen reseptin
        newRecipeButton.setOnClickListener {
            fetchRandomMeal()
        }

        // Tallentaa nykyisen reseptin tietokantaan
        saveRecipeButton.setOnClickListener {
            lastTranslatedRecipe?.let {
                recipeViewModel.addRecipe(it)
                Toast.makeText(
                    requireContext(),
                    "Resepti '${it.name}' tallennettu!",
                    Toast.LENGTH_SHORT
                ).show()
                saveRecipeButton.isVisible = false
            }
        }
    }

    // Hakee satunnaisen aterian API:sta
    private fun fetchRandomMeal() {
        lifecycleScope.launch {
            // Näytetään latausspinneri ja piilotetaan tallennusnappi
            loadingSpinner.isVisible = true
            saveRecipeButton.isVisible = false

            try {
                // API-kutsu satunnaisen reseptin hakemiseen
                val response = mealApiService.getRandomMeal()

                // Otetaan ensimmäinen ateria vastauksesta
                response.meals.firstOrNull()?.let { meal ->
                    currentMeal = meal
                    
                    // Käännetään ja muunnetaan resepti Geminin avulla
                    val ingredientsText = formatIngredients(meal)
                    val translatedRecipe = geminiService.translateAndConvertRecipe(
                        meal.name,
                        ingredientsText,
                        meal.instructions
                    )
                    
                    lastTranslatedRecipe = translatedRecipe
                    updateUiWithTranslatedRecipe(translatedRecipe, meal.thumbnailUrl)
                }
            } catch (e: Exception) {
                // Virhetilanteen ilmoitus käyttäjälle
                Toast.makeText(
                    requireContext(),
                    "Reseptin haku tai käännös epäonnistui: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Piilotetaan latausspinneri
            loadingSpinner.isVisible = false
        }
    }

    private fun updateUiWithTranslatedRecipe(recipe: Recipe, imageUrl: String) {
        // Ladataan kuva Coil-kirjastolla
        mealImage.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }

        // Asetetaan tekstikenttien sisällöt käännetyllä datalla
        mealName.text = recipe.name
        
        // Erotellaan ainesosat ja ohjeet jos mahdollista
        val fullText = recipe.instructions
        if (fullText.contains("OHJEET:", ignoreCase = true)) {
            val parts = fullText.split(Regex("OHJEET:", RegexOption.IGNORE_CASE))
            mealIngredients.text = parts[0].substringAfter("AINESOSAT:").trim()
            mealInstructions.text = parts[1].trim()
        } else {
            mealIngredients.text = ""
            mealInstructions.text = fullText
        }

        // Näytetään tallennuspainike
        saveRecipeButton.isVisible = true
    }

    // Muotoilee ainesosat näytettävään tekstimuotoon
    private fun formatIngredients(meal: Meal): String {
        val ingredients = mutableListOf<String>()

        // Lista kaikista mahdollisista ingredient–measure -pareista
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

        // Käydään läpi ainesosat ja lisätään vain ei-tyhjät arvot
        for ((ingredient, measure) in properties) {
            if (!ingredient.isNullOrBlank()) {
                ingredients.add(
                    "- ${measure?.trim() ?: ""} ${ingredient.trim()}".trim()
                )
            }
        }

        // Palautetaan rivinvaihdoilla eroteltu lista
        return ingredients.joinToString("\n")
    }
}
