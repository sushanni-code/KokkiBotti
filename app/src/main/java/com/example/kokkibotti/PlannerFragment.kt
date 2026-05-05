package com.example.kokkibotti

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import java.util.Calendar

// Fragmentti, joka näyttää viikko- ja päiväkohtaiset ateriasuunnitelmat
class PlannerFragment : Fragment() {

    // Hakee RecipeViewModelin, jota käytetään ateriatietojen hallintaan
    private val recipeViewModel: RecipeViewModel by activityViewModels {
        RecipeViewModelFactory(requireActivity().application)
    }

    // Launcher reseptin tarkastelua ja mahdollista muokkausta varten
    private val viewRecipeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val editedRecipe = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("EDITED_RECIPE", Recipe::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Recipe>("EDITED_RECIPE")
            }
            if (editedRecipe != null) {
                recipeViewModel.updateRecipe(editedRecipe)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflatoi fragmentin layoutin
        return inflater.inflate(R.layout.fragment_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Viikonpäiväkonttien ja otsikoiden hakeminen layoutista
        val weekContainers = listOf(
            view.findViewById<LinearLayout>(R.id.week_1_days_container),
            view.findViewById<LinearLayout>(R.id.week_2_days_container),
            view.findViewById<LinearLayout>(R.id.week_3_days_container),
            view.findViewById<LinearLayout>(R.id.week_4_days_container)
        )

        val weekHeaders = listOf(
            view.findViewById<View>(R.id.week_1_header),
            view.findViewById<View>(R.id.week_2_header),
            view.findViewById<View>(R.id.week_3_header),
            view.findViewById<View>(R.id.week_4_header)
        )

        // Viikonpäivien nimet
        val weekDays = listOf("Maanantai", "Tiistai", "Keskiviikko", "Torstai", "Perjantai", "Lauantai", "Sunnuntai")
        val inflater = LayoutInflater.from(requireContext())

        // Haetaan nykyinen viikko ja viikonpäivä kalenterista
        val calendar = Calendar.getInstance()
        val currentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7 else calendar.get(Calendar.DAY_OF_WEEK) - 1

        for (i in weekContainers.indices) {
            val weekNumber = i + 1
            val daysContainer = weekContainers[i]
            val header = weekHeaders[i]

            // Asetetaan viikon otsikko
            header.findViewById<TextView>(R.id.week_title_text_view).text = "Viikko $weekNumber"
            daysContainer.removeAllViews() // Tyhjennetään vanhat näkymät

            // Nollaa-painikkeen toiminnallisuus
            val resetBtn = header.findViewById<Button>(R.id.reset_week_button)
            resetBtn.setOnClickListener {
                AlertDialog.Builder(requireContext(), R.style.Theme_App_Dialog_Alert)
                    .setTitle("Nollaa viikko")
                    .setMessage("Haluatko varmasti poistaa kaikki tämän viikon valinnat?")
                    .setPositiveButton("Kyllä") { _, _ -> recipeViewModel.resetWeek(weekNumber) }
                    .setNegativeButton("Ei", null)
                    .show()
            }

            // Luodaan näkymät jokaiselle viikonpäivälle
            for (j in weekDays.indices) {
                val dayOfWeek = j + 1
                val dayName = weekDays[j]
                val dayView = inflater.inflate(R.layout.planner_day_item, daysContainer, false)
                val recipeNameTextView = dayView.findViewById<TextView>(R.id.recipe_name_text_view)

                dayView.findViewById<TextView>(R.id.day_name_text_view).text = dayName
                recipeNameTextView.text = "Valitse resepti" // Alustusteksti

                // Kuunnellaan LiveDataa, jotta valittu resepti näkyy automaattisesti
                recipeViewModel.getRecipeForDay(weekNumber, dayOfWeek).observe(viewLifecycleOwner, Observer { recipe ->
                    recipeNameTextView.text = recipe?.name ?: "Valitse resepti"
                    
                    val btnViewRecipe = dayView.findViewById<ImageButton>(R.id.btn_view_recipe)
                    if (recipe != null && recipe.id != -1) {
                        btnViewRecipe.isVisible = true
                        btnViewRecipe.setOnClickListener {
                            val intent = Intent(requireContext(), RecipeDetailActivity::class.java).apply {
                                putExtra("RECIPE", recipe)
                            }
                            viewRecipeResultLauncher.launch(intent)
                        }
                    } else {
                        btnViewRecipe.isVisible = false
                    }
                })

                // Päivän näkymän klikkauksen käsittely reseptin valintaa varten
                dayView.setOnClickListener {
                    showRecipeSelectionDialog(weekNumber, dayOfWeek)
                }

                // Korostetaan nykyistä päivää
                if (currentWeekOfYear == calendar.get(Calendar.WEEK_OF_YEAR) && currentDayOfWeek == dayOfWeek) {
                    dayView.background = ContextCompat.getDrawable(requireContext(), R.drawable.current_day_background)
                }

                // Lisätään päivä näkymä konttiin
                daysContainer.addView(dayView)
            }

            // Viikon otsikon laajennus-/supistustoiminto
            val expandIcon = header.findViewById<ImageView>(R.id.expand_icon)
            header.setOnClickListener {
                daysContainer.isVisible = !daysContainer.isVisible
                rotateIcon(expandIcon, if (daysContainer.isVisible) 180f else 0f)
            }

            // Ensimmäinen viikko avattuna, muut suljettuina
            if (i > 0) {
                daysContainer.isVisible = false
                rotateIcon(expandIcon, 0f)
            } else {
                daysContainer.isVisible = true
                rotateIcon(expandIcon, 180f)
            }
        }
    }

    // Näyttää dialogin reseptin valintaa varten tietylle päivälle
    private fun showRecipeSelectionDialog(week: Int, day: Int) {
        val recipes = recipeViewModel.allRecipes.value ?: emptyList()
        val specialOptions = arrayOf("Tyhjä", "Syö ulkona")
        val recipeNames = recipes.map { it.name }.toTypedArray()
        val allOptions = specialOptions + recipeNames

        AlertDialog.Builder(requireContext(), R.style.Theme_App_Dialog_Selection)
            .setTitle("Valitse")
            .setItems(allOptions) { dialog, which ->
                when {
                    which == 0 -> { // Tyhjä
                        recipeViewModel.unplanMeal(week, day)
                    }
                    which == 1 -> { // Syö ulkona
                        // Luodaan placeholder-resepti "Syö ulkona"
                        val eatingOutRecipe = Recipe(id = -1, name = "Syö ulkona", ingredients = emptyList(), instructions = "")
                        recipeViewModel.planMeal(week, day, eatingOutRecipe)
                    }
                    else -> {
                        val selectedRecipe = recipes[which - specialOptions.size]
                        recipeViewModel.planMeal(week, day, selectedRecipe)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Peruuta") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Pyörittää laajennus-/supistuskuvaketta animaation avulla
    private fun rotateIcon(icon: ImageView, toAngle: Float) {
        ObjectAnimator.ofFloat(icon, "rotation", toAngle).setDuration(300).start()
    }
}
