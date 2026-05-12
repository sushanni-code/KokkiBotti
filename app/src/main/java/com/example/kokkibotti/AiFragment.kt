package com.example.kokkibotti

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
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

    // Kameran käynnistin
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val imagePreview = view?.findViewById<ImageView>(R.id.image_preview)
            imagePreview?.setImageBitmap(bitmap)
            imagePreview?.isVisible = true
            
            // Analysoidaan kuva nykyisellä kielellä
            analyzeImage(bitmap)
        }
    }

    // Luvan pyytäjä
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), getString(R.string.ai_permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize service using the key from BuildConfig
        geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

        val promptInput = view.findViewById<EditText>(R.id.edit_text_prompt)
        val generateButton = view.findViewById<Button>(R.id.button_generate)
        val cameraButton = view.findViewById<Button>(R.id.button_camera)
        val saveButton = view.findViewById<Button>(R.id.button_save)
        val responseText = view.findViewById<TextView>(R.id.text_ai_response)
        val spinner = view.findViewById<ProgressBar>(R.id.loading_spinner)
        val imagePreview = view.findViewById<ImageView>(R.id.image_preview)

        generateButton.setOnClickListener {
            val userPrompt = promptInput.text.toString()
            if (userPrompt.isNotBlank()) {
                imagePreview.isVisible = false
                lifecycleScope.launch {
                    spinner.isVisible = true
                    saveButton.isVisible = false
                    try {
                        val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "fi"
                        val recipe = geminiService.generateRecipe(userPrompt, currentLang)
                        handleRecipeResult(recipe, responseText, saveButton)
                    } catch (e: Exception) {
                        responseText.text = getString(R.string.ai_error_prefix, e.message)
                        lastGeneratedRecipe = null
                    } finally {
                        spinner.isVisible = false
                    }
                }
            }
        }

        cameraButton.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        saveButton.setOnClickListener {
            lastGeneratedRecipe?.let { recipe ->
                recipeViewModel.addRecipe(recipe)
                Toast.makeText(requireContext(), getString(R.string.ai_save_success, recipe.name), Toast.LENGTH_SHORT).show()
                saveButton.isVisible = false // Piilotetaan tallennuksen jälkeen
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePictureLauncher.launch(null)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        val responseText = view?.findViewById<TextView>(R.id.text_ai_response)
        val saveButton = view?.findViewById<Button>(R.id.button_save)
        val spinner = view?.findViewById<ProgressBar>(R.id.loading_spinner)

        lifecycleScope.launch {
            spinner?.isVisible = true
            saveButton?.isVisible = false
            try {
                val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "fi"
                val recipe = geminiService.generateRecipeFromImage(bitmap, currentLang)
                handleRecipeResult(recipe, responseText, saveButton)
            } catch (e: Exception) {
                responseText?.text = getString(R.string.ai_error_prefix, e.message)
                lastGeneratedRecipe = null
            } finally {
                spinner?.isVisible = false
            }
        }
    }

    private fun handleRecipeResult(recipe: Recipe, responseText: TextView?, saveButton: Button?) {
        lastGeneratedRecipe = recipe
        responseText?.text = recipe.instructions
        
        // Näytetään tallennuspainike vain jos haku onnistui
        if (recipe.name != "Virhe" && recipe.name != "Error") {
            saveButton?.isVisible = true
        }
    }
}