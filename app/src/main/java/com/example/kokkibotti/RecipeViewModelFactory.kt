package com.example.kokkibotti

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory-luokka, joka mahdollistaa RecipeViewModelin luomisen
// Tarvitaan, koska RecipeViewModel tarvitsee Application-parametrin konstruktorissaan
class RecipeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // Luo ViewModelin, tarkistaa että pyydetty luokka on RecipeViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(application) as T
        }
        // Heitetään poikkeus, jos yritetään luoda tuntematonta ViewModel-luokkaa
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
