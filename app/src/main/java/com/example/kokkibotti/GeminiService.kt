package com.example.kokkibotti

import android.graphics.Bitmap
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GeminiService(apiKey: String) {

    private val client = Client.builder()
        .apiKey(apiKey)
        .build()

    /**
     * Luodaan resepti tekstipromptin perusteella.
     */
    suspend fun generateRecipe(theme: String): Recipe = withContext(Dispatchers.IO) {
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
        
        callGemini(listOf(Part.fromText(prompt)), theme)
    }

    /**
     * Tunnistaa ainesosat kuvasta ja luo niistä reseptin.
     */
    suspend fun generateRecipeFromImage(bitmap: Bitmap): Recipe = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()

            val prompt = """
                Tunnista kaikki ainesosat tästä kuvasta ja luo niistä herkullinen suomenkielinen resepti.
                Anna vastaus selkeässä muodossa:
                NIMI: [reseptin nimi]
                AINESOSAT:
                - [ainesosa 1]
                - [ainesosa 2]
                OHJEET:
                [vaiheittaiset ohjeet]
            """.trimIndent()

            val parts = listOf(
                Part.fromText(prompt),
                Part.fromBytes(imageBytes, "image/jpeg")
            )

            callGemini(parts, "Kuva-analyysi")
        } catch (e: Exception) {
            Recipe(name = "Virhe", instructions = "Kuvan käsittely epäonnistui: ${e.message}", ingredients = emptyList())
        }
    }

    /**
     * Yhteinen apumetodi Gemini-kutsun tekemiseen.
     */
    private suspend fun callGemini(parts: List<Part>, fallbackTheme: String): Recipe {
        return try {
            val content = Content.builder()
                .parts(parts)
                .build()

            // Käytetään vuoden 2026 vakaata mallia (preview-tila vaatii usein -preview päätteen)
            val response = client.models.generateContent("gemini-3-flash-preview", content, null)
            val fullText = response.text() ?: "Ei vastausta tekoälyltä."
            
            // Yritetään poimia nimi tekstistä
            val name = fullText.lineSequence()
                .firstOrNull { it.startsWith("NIMI:", ignoreCase = true) }
                ?.substringAfter(":")?.trim() ?: "AI Resepti: $fallbackTheme"

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
