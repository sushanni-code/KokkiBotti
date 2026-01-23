package com.example.kokkibotti

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val darkModeSwitch: SwitchMaterial = view.findViewById(R.id.dark_mode_switch)

        // Asetetaan kytkin oikeaan tilaan fragmentin avautuessa
        darkModeSwitch.isChecked = ThemeManager.isDarkMode(requireContext())

        // Lisätään kuuntelija kytkimen tilan muutoksille
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ThemeManager.setDarkMode(requireContext())
            } else {
                ThemeManager.setLightMode(requireContext())
            }
        }
    }
}