package com.example.kokkibotti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView Adapter, joka tukee ainesosien näyttämistä
 * ja niiden poistamista edit-tilassa.
 */
class EditableIngredientAdapter(

    // MutableList, koska adapter poistaa ainesosia listasta
    private val ingredients: MutableList<Ingredient>,

    // Edit-tila määrittää näkyykö poistopainike
    private var isEditMode: Boolean

) : RecyclerView.Adapter<EditableIngredientAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        // Inflatoidaan oma layout jokaiselle RecyclerView-riville
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.editable_ingredient_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Haetaan kyseinen ainesosa listasta
        val ingredient = ingredients[position]

        // Muodostetaan käyttäjälle selkeä tekstiesitys
        holder.ingredientText.text =
            "- ${ingredient.amount} ${ingredient.unit} ${ingredient.name}"

        // Poistopainike näkyy vain edit-tilassa
        holder.removeButton.visibility =
            if (isEditMode) View.VISIBLE else View.GONE

        // Poistopainikkeen klikkaus
        holder.removeButton.setOnClickListener {

            // Käytetään bindingAdapterPositionia,
            // jotta varmistetaan että positio on ajan tasalla
            val pos = holder.bindingAdapterPosition

            // RecyclerView.NO_POSITION tarkistus estää kaatumiset
            // esim. nopeissa klikkauksissa tai animaatioiden aikana
            if (pos != RecyclerView.NO_POSITION) {

                // Poistetaan ainesosa listasta
                ingredients.removeAt(pos)

                // Ilmoitetaan RecyclerView’lle vain yhden rivin poistosta
                // → tehokkaampi ja energiatehokkaampi kuin notifyDataSetChanged()
                notifyItemRemoved(pos)
            }
        }
    }

    override fun getItemCount(): Int =
        ingredients.size // RecyclerView kysyy montako riviä näytetään

    /**
     * Vaihtaa edit-tilan päälle tai pois
     * Tämä vaikuttaa kaikkiin riveihin, koska poistopainike
     * joko näkyy tai ei näy jokaisessa itemissä.
     */
    fun setEditMode(editMode: Boolean) {

        isEditMode = editMode

        // Tässä tapauksessa koko lista tarvitsee uudelleenbindauksen,
        // joten notifyDataSetChanged() on hyväksyttävä ja looginen ratkaisu
        notifyDataSetChanged()
    }

    /**
     * ViewHolder säilyttää viittaukset näkymiin
     * → findViewById kutsutaan vain kerran per item
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Ainesosan tekstikenttä
        val ingredientText: TextView =
            itemView.findViewById(R.id.ingredient_text)

        // Poistopainike
        val removeButton: ImageButton =
            itemView.findViewById(R.id.remove_ingredient_button)
    }
}
