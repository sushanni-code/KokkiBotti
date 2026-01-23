package com.example.kokkibotti

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView

// Adapteri, joka näyttää reseptit RecyclerViewissä
class RecipeAdapter(
    private val recipes: MutableList<Recipe>, // Lista resepteistä, joita näytetään
    private val editRecipeResultLauncher: ActivityResultLauncher<Intent> // Launcher uuden Activityn käynnistämiseen muokkausta varten
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // Luo uuden ViewHolderin ja inflatoi layoutin
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_list_item, parent, false)
        return RecipeViewHolder(view)
    }

    // Sidotaan data ViewHolderiin
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name

        // Klikkaustapahtuma: avaa RecipeDetailActivityn muokkausta varten
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailActivity::class.java).apply {
                putExtra("RECIPE", recipe) // Lähetetään valittu resepti
                putExtra("RECIPE_POSITION", position) // Lähetetään myös sijainti listassa
            }
            editRecipeResultLauncher.launch(intent)
        }
    }

    // Palauttaa listan koon
    override fun getItemCount() = recipes.size

    // Apufunktio tietyn reseptin hakemiseen indeksin perusteella
    fun getRecipeAt(position: Int): Recipe {
        return recipes[position]
    }

    // Sisäinen ViewHolder-luokka, joka pitää yllä yksittäisen reseptin näkymää
    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.recipe_name)
    }
}
