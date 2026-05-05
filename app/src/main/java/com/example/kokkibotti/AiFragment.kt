package com.example.kokkibotti

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.kokkibotti.BuildConfig

class AiFragment : Fragment(R.layout.fragment_ai) {

    private lateinit var geminiService: GeminiService
    private val recipeViewModel: RecipeViewModel by activityViewModels {
        RecipeViewModelFactory(requireActivity().application)
    }
    private var lastGeneratedRecipe: Recipe? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize service using the key from BuildConfig
        geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

        val promptInput = view.findViewById<EditText>(R.id.edit_text_prompt)
        val generateButton = view.findViewById<Button>(R.id.button_generate)
        val saveButton = view.findViewById<Button>(R.id.button_save)
        val responseText = view.findViewById<TextView>(R.id.text_ai_response)
        val spinner = view.findViewById<ProgressBar>(R.id.loading_spinner)

        generateButton.setOnClickListener {
            val userPrompt = promptInput.text.toString()
            if (userPrompt.isNotBlank()) {
                lifecycleScope.launch {
                    spinner.isVisible = true
                    saveButton.isVisible = false
                    try {
                        val recipe = geminiService.generateRecipe(userPrompt)
                        lastGeneratedRecipe = recipe
                        responseText.text = recipe.instructions
                        
                        // Näytetään tallennuspainike vain jos haku onnistui
                        if (recipe.name != "Virhe") {
                            saveButton.isVisible = true
                        }
                    } catch (e: Exception) {
                        responseText.text = "Virhe: ${e.message}"
                        lastGeneratedRecipe = null
                    } finally {
                        spinner.isVisible = false
                    }
                }
            }
        }

        saveButton.setOnClickListener {
            lastGeneratedRecipe?.let { recipe ->
                recipeViewModel.addRecipe(recipe)
                Toast.makeText(requireContext(), "Resepti '${recipe.name}' tallennettu!", Toast.LENGTH_SHORT).show()
                saveButton.isVisible = false // Piilotetaan tallennuksen jälkeen
            }
        }
    }
}