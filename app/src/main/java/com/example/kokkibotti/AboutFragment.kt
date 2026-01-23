package com.example.kokkibotti

// Androidin perusluokat fragmentin elinkaaren ja näkymien käsittelyyn
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// Fragment-luokka AndroidX-kirjastosta
import androidx.fragment.app.Fragment

// AboutFragment on Fragment, joka näyttää "Tietoja sovelluksesta" -näkymän
class AboutFragment : Fragment() {

    // Tätä metodia kutsutaan, kun fragmentin näkymä luodaan
    override fun onCreateView(
        inflater: LayoutInflater,          // Vastaa layoutin "inflateamisesta" XML:stä näkymäksi
        container: ViewGroup?,              // Parent view, johon fragmentti liitetään
        savedInstanceState: Bundle?         // Mahdollinen aiemmin tallennettu tila
    ): View? {
        // Inflatoidaan fragment_about.xml ja palautetaan se fragmentin näkymäksi
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}
