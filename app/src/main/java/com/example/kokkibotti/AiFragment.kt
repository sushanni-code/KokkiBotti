package com.example.kokkibotti

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.kokkibotti.BuildConfig

class AiFragment : Fragment(R.layout.fragment_ai) {

    private lateinit var geminiService: GeminiService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize service using the key from BuildConfig
        geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

        val promptInput = view.findViewById<EditText>(R.id.edit_text_prompt)
        val generateButton = view.findViewById<Button>(R.id.button_generate)
        val responseText = view.findViewById<TextView>(R.id.text_ai_response)
        val spinner = view.findViewById<ProgressBar>(R.id.loading_spinner)

        generateButton.setOnClickListener {
            val userPrompt = promptInput.text.toString()
            if (userPrompt.isNotBlank()) {
                lifecycleScope.launch {
                    spinner.isVisible = true
                    try {
                        val recipe = geminiService.generateRecipe(userPrompt)
                        responseText.text = recipe.instructions
                    } catch (e: Exception) {
                        responseText.text = "Virhe: ${e.message}"
                    } finally {
                        spinner.isVisible = false
                    }
                }
            }
        }
    }
}