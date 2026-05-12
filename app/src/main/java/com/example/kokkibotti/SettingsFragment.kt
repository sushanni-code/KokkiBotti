package com.example.kokkibotti

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkModeSwitch: SwitchMaterial = view.findViewById(R.id.dark_mode_switch)
        val languageAutoComplete: AutoCompleteTextView = view.findViewById(R.id.language_autocomplete)

        // --- Dark Mode ---
        darkModeSwitch.isChecked = ThemeManager.isDarkMode(requireContext())
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ThemeManager.setDarkMode(requireContext())
            } else {
                ThemeManager.setLightMode(requireContext())
            }
        }

        // --- Language Selection ---
        val languages = listOf("fi", "en")
        val displayNames = listOf(getString(R.string.language_fi), getString(R.string.language_en))

        // Käytä tavallista ArrayAdapteria
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            displayNames
        )
        languageAutoComplete.setAdapter(adapter)

        // Haetaan nykyinen kieli
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        val currentLang = if (!currentAppLocales.isEmpty) {
            currentAppLocales.get(0)?.language ?: "fi"
        } else {
            "fi"
        }

        // Asetetaan nykyinen teksti ilman suodatusta (filter = false)
        val currentText = if (currentLang.startsWith("en")) {
            getString(R.string.language_en)
        } else {
            getString(R.string.language_fi)
        }

        languageAutoComplete.setText(currentText, false)

        languageAutoComplete.setOnItemClickListener { _, _, position, _ ->
            // Haetaan valittu kieli adapterin perusteella, jotta se täsmää näkymään
            val selectedDisplayName = adapter.getItem(position)
            val selectedLang = if (selectedDisplayName == getString(R.string.language_en)) "en" else "fi"

            val nowAppLocales = AppCompatDelegate.getApplicationLocales()
            val nowLang = if (!nowAppLocales.isEmpty) nowAppLocales.get(0)?.language ?: "" else ""

            if (!nowLang.startsWith(selectedLang)) {
                val appLocales = LocaleListCompat.forLanguageTags(selectedLang)
                AppCompatDelegate.setApplicationLocales(appLocales)
            }
        }
    }
}