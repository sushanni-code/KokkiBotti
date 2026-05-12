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
     * Luodaan resepti tekstipromptin perusteella valitulla kielellä.
     */
    suspend fun generateRecipe(theme: String, language: String): Recipe = withContext(Dispatchers.IO) {
        val prompt = if (language == "en") {
            """
                Create an English recipe with the theme: $theme.
                Provide the answer in a clear format:
                NAME: [recipe name]
                INGREDIENTS:
                - [ingredient 1]
                - [ingredient 2]
                INSTRUCTIONS:
                [step-by-step instructions]
            """.trimIndent()
        } else {
            """
                Luo suomenkielinen resepti teemalla: $theme.
                Anna vastaus selkeässä muodossa:
                NIMI: [reseptin nimi]
                AINESOSAT:
                - [ainesosa 1]
                - [ainesosa 2]
                OHJEET:
                [vaiheittaiset ohjeet]
            """.trimIndent()
        }
        
        callGemini(listOf(Part.fromText(prompt)), theme, language)
    }

    /**
     * Tunnistaa ainesosat kuvasta ja luo niistä reseptin valitulla kielellä.
     */
    suspend fun generateRecipeFromImage(bitmap: Bitmap, language: String): Recipe = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()

            val prompt = if (language == "en") {
                """
                    Identify all ingredients from this image and create a delicious English recipe using them.
                    Provide the answer in a clear format:
                    NAME: [recipe name]
                    INGREDIENTS:
                    - [ingredient 1]
                    - [ingredient 2]
                    INSTRUCTIONS:
                    [step-by-step instructions]
                """.trimIndent()
            } else {
                """
                    Tunnista kaikki ainesosat tästä kuvasta ja luo niistä herkullinen suomenkielinen resepti.
                    Anna vastaus selkeässä muodossa:
                    NIMI: [reseptin nimi]
                    AINESOSAT:
                    - [ainesosa 1]
                    - [ainesosa 2]
                    OHJEET:
                    [vaiheittaiset ohjeet]
                """.trimIndent()
            }

            val parts = listOf(
                Part.fromText(prompt),
                Part.fromBytes(imageBytes, "image/jpeg")
            )

            callGemini(parts, "Image analysis", language)
        } catch (e: Exception) {
            val errorTitle = if (language == "en") "Error" else "Virhe"
            val errorMessage = if (language == "en") "Image processing failed: " else "Kuvan käsittely epäonnistui: "
            Recipe(name = errorTitle, instructions = "$errorMessage${e.message}", ingredients = emptyList())
        }
    }

    /**
     * Kääntää MealDB-reseptin (tai pitää englanninkielisenä) ja muuntaa yksiköt metrijärjestelmään.
     */
    suspend fun translateAndConvertRecipe(mealName: String, ingredients: String, instructions: String, language: String): Recipe = withContext(Dispatchers.IO) {
        val prompt = if (language == "en") {
            """
                Keep the following recipe in English but convert all measurements (like lbs, oz, cups, tsp, tbsp) to metric units (g, kg, ml, l).
                
                RECIPE NAME: $mealName
                INGREDIENTS:
                $ingredients
                INSTRUCTIONS:
                $instructions
                
                Return the answer in this format:
                NAME: [converted name]
                INGREDIENTS:
                - [ingredient 1 with metric measure]
                - [ingredient 2 with metric measure]
                INSTRUCTIONS:
                [instructions]
            """.trimIndent()
        } else {
            """
                Käännä seuraava resepti suomeksi ja muuntaa kaikki mitat (kuten lbs, oz, cups, tsp, tbsp) suomalaisiin metrijärjestelmän yksiköihin (g, kg, dl, l, tl, rkl).
                
                RESEPTIN NIMI: $mealName
                AINESOSAT:
                $ingredients
                OHJEET:
                $instructions
                
                Palauta vastaus tässä muodossa:
                NIMI: [käännetty nimi]
                AINESOSAT:
                - [ainesosa 1 mitalla]
                - [ainesosa 2 mitalla]
                OHJEET:
                [käännetyt ohjeet]
            """.trimIndent()
        }

        callGemini(listOf(Part.fromText(prompt)), mealName, language)
    }

    /**
     * Yhteinen apumetodi Gemini-kutsun tekemiseen.
     */
    private suspend fun callGemini(parts: List<Part>, fallbackTheme: String, language: String): Recipe {
        return try {
            val content = Content.builder()
                .parts(parts)
                .build()

            // Käytetään vuoden 2026 vakaata mallia (preview-tila vaatii usein -preview päätteen)
            val response = client.models.generateContent("gemini-3-flash-preview", content, null)
            val fullText = response.text() ?: (if (language == "en") "No response from AI." else "Ei vastausta tekoälyltä.")
            
            val namePrefix = if (language == "en") "NAME:" else "NIMI:"
            
            val name = fullText.lineSequence()
                .firstOrNull { it.startsWith(namePrefix, ignoreCase = true) }
                ?.substringAfter(":")?.trim() ?: "AI Recipe: $fallbackTheme"

            Recipe(
                name = name,
                instructions = fullText,
                ingredients = emptyList()
            )
        } catch (e: Exception) {
            val errorTitle = if (language == "en") "Error" else "Virhe"
            val errorMessage = if (language == "en") "Recipe generation failed: " else "Reseptin luonti epäonnistui: "
            Recipe(name = errorTitle, instructions = "$errorMessage${e.message}", ingredients = emptyList())
        }
    }
}