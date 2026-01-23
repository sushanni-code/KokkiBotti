package com.example.kokkibotti

// Androidin perusluokat fragmentin elinkaaren ja näkymien käsittelyyn
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// AndroidX Fragment -luokka
import androidx.fragment.app.Fragment

// Yksinkertainen placeholder-fragmentti,
// jota voidaan käyttää väliaikaisena tai tyhjänä näkymänä
class PlaceholderFragment : Fragment() {

    // Luodaan fragmentin näkymä
    override fun onCreateView(
        inflater: LayoutInflater,      // Vastaa layoutin muuttamisesta View-olioksi
        container: ViewGroup?,          // Parent view, johon fragmentti liitetään
        savedInstanceState: Bundle?     // Mahdollinen aiemmin tallennettu tila
    ): View? {

        // Inflatoidaan fragment_placeholder.xml ja palautetaan se fragmentin näkymäksi
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }
}
