package com.example.kokkibotti

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

// Singleton-objekti, joka hallitsee sovelluksen teemat (valo-/pimeätila)
object ThemeManager {
    private const val PREFS_NAME = "ThemePrefs"    // SharedPreferences-tiedoston nimi
    private const val THEME_KEY = "theme_mode"     // Avain tallennetulle teematilalle

    // Hakee SharedPreferences-instanssin
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Soveltaa tallennettua teematilaa sovellukseen
    fun applyTheme(context: Context) {
        when (getPrefs(context).getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // Asettaa valo-tilan ja tallentaa sen
    fun setLightMode(context: Context) {
        getPrefs(context).edit().putInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO).apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    // Asettaa pimeä-tilan ja tallentaa sen
    fun setDarkMode(context: Context) {
        getPrefs(context).edit().putInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_YES).apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    // Tarkistaa, onko nykyinen teema pimeä
    fun isDarkMode(context: Context): Boolean {
        return getPrefs(context).getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) == AppCompatDelegate.MODE_NIGHT_YES
    }
}
