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

        // Hakee uuden satunnaisen reseptin
        newRecipeButton.setOnClickListener {
            fetchRandomMeal()
        }

        // Tallentaa nykyisen reseptin tietokantaan
        saveRecipeButton.setOnClickListener {
            currentMeal?.let {
                val recipeToSave = convertMealToRecipe(it)
                recipeViewModel.addRecipe(recipeToSave)
                Toast.makeText(
                    requireContext(),
                    "Resepti '${it.name}' tallennettu!",
                    Toast.LENGTH_SHORT
                ).show()
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
                    updateUiWithMeal(meal)
                }
            } catch (e: Exception) {
                // Virhetilanteen ilmoitus käyttäjälle
                Toast.makeText(
                    requireContext(),
                    "Reseptin haku epäonnistui: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Piilotetaan latausspinneri
            loadingSpinner.isVisible = false
        }
    }

    // Päivittää käyttöliittymän aterian tiedoilla
    private fun updateUiWithMeal(meal: Meal) {

        // Ladataan kuva Coil-kirjastolla
        mealImage.load(meal.thumbnailUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_background)
            error(R.drawable.ic_launcher_background)
        }

        // Asetetaan tekstikenttien sisällöt
        mealName.text = meal.name
        mealInstructions.text = meal.instructions
        mealIngredients.text = formatIngredients(meal)

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

    // Muuntaa Meal-olion Recipe-olioksi tietokantaa varten
    private fun convertMealToRecipe(meal: Meal): Recipe {
        val ingredientsList = mutableListOf<Ingredient>()

        // Lista kaikista ainesosa–määrä -pareista
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

        // Rakennetaan Ingredient-oliot ei-tyhjistä arvoista
        for ((ingredient, measure) in properties) {
            if (!ingredient.isNullOrBlank()) {

                // Yritetään muuntaa määrä numeroksi
                val amount = measure?.toDoubleOrNull() ?: 1.0

                // Erotellaan yksikkö määrästä
                val unit = measure
                    ?.replace(amount.toString(), "")
                    ?.trim() ?: "kpl"

                ingredientsList.add(
                    Ingredient(amount, unit, ingredient)
                )
            }
        }

        // Luodaan Recipe-olio (ID syntyy vasta tietokantaan tallennettaessa)
        return Recipe(
            name = meal.name,
            ingredients = ingredientsList,
            instructions = meal.instructions
        )
    }
}
