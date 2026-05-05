package com.example.kokkibotti

import com.google.genai.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(apiKey: String) {

    private val client = Client.builder()
        .apiKey(apiKey)
        .build()

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
            
            // Käytetään vuoden 2026 vakaata mallia (preview-tila vaatii usein -preview päätteen)
            val response = client.models.generateContent("gemini-3-flash-preview", prompt, null)
            val fullText = response.text() ?: "Ei vastausta tekoälyltä."
            
            // Yritetään poimia nimi tekstistä
            val name = fullText.lineSequence()
                .firstOrNull { it.startsWith("NIMI:", ignoreCase = true) }
                ?.substringAfter(":")?.trim() ?: "AI Resepti: $theme"

            Recipe(
                name = name,
                instructions = fullText,
                ingredients = emptyList()
            )
        } catch (e: Exception) {
            Recipe(name = "Virhe", instructions = "Reseptin luonti epäonnistui: ${e.message}", ingredients = emptyList())
        }
    }
}
