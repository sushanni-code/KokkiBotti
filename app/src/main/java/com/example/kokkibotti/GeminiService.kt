package com.example.kokkibotti

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun generateRecipe(theme: String): Recipe = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Luo suomenkielinen resepti teemalla: $theme.
                Anna vastaus selkeässä muodossa:
                NIMI: [reseptin nimi]
                AINESOSAT:
                - [ainesosa 1]
                - [ainesosa 2]
                OHJEET:
                [vaiheittaiset ohjeet]
            """.trimIndent()
            
            val response = model.generateContent(prompt)
            val fullText = response.text ?: "Ei vastausta tekoälyltä."
            
            // Yritetään poimia nimi tekstistä
            val name = fullText.lineSequence()
                .firstOrNull { it.startsWith("NIMI:", ignoreCase = true) }
                ?.substringAfter(":")?.trim() ?: "AI Resepti: $theme"

            Recipe(
                name = name,
                instructions = fullText,
                ingredients = emptyList() // Ainesosien parsiminen listaksi vaatisi enemmän logiikkaa, jätetään se myöhemmäksi
            )
        } catch (e: Exception) {
            Recipe(name = "Virhe", instructions = "Reseptin luonti epäonnistui: ${e.message}", ingredients = emptyList())
        }
    }
}