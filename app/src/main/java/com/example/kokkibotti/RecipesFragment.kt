package com.example.kokkibotti

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Fragmentti, joka näyttää listan kaikista resepteistä ja mahdollistaa niiden lisäämisen, muokkaamisen ja poistamisen
class RecipesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private var allRecipesList = emptyList<Recipe>()
    private var currentQuery = ""

    // ViewModel reseptien hallintaan
    private val recipeViewModel: RecipeViewModel by activityViewModels {
        RecipeViewModelFactory(requireActivity().application)
    }

    // Launcher uuden reseptin lisäämistä varten AddRecipeActivitysta
    private val addRecipeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newRecipe = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("NEW_RECIPE", Recipe::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Recipe>("NEW_RECIPE")
            }
            newRecipe?.let {
                recipeViewModel.addRecipe(it) // Lisää uusi resepti ViewModeliin
            }
        }
    }

    // Launcher olemassa olevan reseptin muokkaamista varten RecipeDetailActivitysta
    private val editRecipeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val editedRecipe = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("EDITED_RECIPE", Recipe::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Recipe>("EDITED_RECIPE")
            }
            if (editedRecipe != null) {
                recipeViewModel.updateRecipe(editedRecipe) // Päivittää reseptin ViewModelissa
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflatoi fragmentin layoutin
        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hakee RecyclerViewin ja tyhjän näkymän
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.empty_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Hakutoiminnallisuuden alustus
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                updateFilteredList()
                return true
            }
        })

        // Kuunnellaan reseptilistaa ViewModelista
        recipeViewModel.allRecipes.observe(viewLifecycleOwner, Observer { recipes ->
            allRecipesList = recipes ?: emptyList()
            updateFilteredList()
        })

        // Liitetään pyyhkäisytoiminto reseptien poistamiseen
        val itemTouchHelper = ItemTouchHelper(setupSwipeToDelete())
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // FloatingActionButton uuden reseptin lisäämiseen
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_recipe)
        fab.setOnClickListener {
            val intent = Intent(requireContext(), AddRecipeActivity::class.java)
            addRecipeResultLauncher.launch(intent)
        }
    }

    // Suodattaa reseptit haun perusteella ja päivittää listan
    private fun updateFilteredList() {
        val filtered = if (currentQuery.isEmpty()) {
            allRecipesList
        } else {
            allRecipesList.filter { recipe ->
                recipe.name.contains(currentQuery, ignoreCase = true) ||
                recipe.ingredients.any { it.name.contains(currentQuery, ignoreCase = true) }
            }
        }

        emptyView.isVisible = filtered.isEmpty()
        recyclerView.isVisible = filtered.isNotEmpty()

        // Luodaan uusi adapteri suodatetulla listalla
        val adapter = RecipeAdapter(filtered.toMutableList(), editRecipeResultLauncher)
        recyclerView.adapter = adapter
    }

    // Asettaa pyyhkäisytoiminnon RecyclerViewille reseptin poistamiseksi
    private fun setupSwipeToDelete(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Siirtämistä ei tueta
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val recipeToDelete = (recyclerView.adapter as RecipeAdapter).getRecipeAt(position)

                // Näytetään varmistusdialogi ennen poistamista
                AlertDialog.Builder(requireContext(), R.style.Theme_App_Dialog_Alert)
                    .setTitle("Poista resepti")
                    .setMessage("Haluatko varmasti poistaa reseptin '${recipeToDelete.name}'?")
                    .setPositiveButton("Poista") { _, _ ->
                        recipeViewModel.deleteRecipe(recipeToDelete) // Poistetaan resepti ViewModelista
                    }
                    .setNegativeButton("Peruuta") { dialog, _ ->
                        // Palautetaan pyyhkäisty item, jos käyttäjä peruuttaa
                        (recyclerView.adapter as RecipeAdapter).notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
}
