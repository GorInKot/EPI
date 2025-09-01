package com.example.epi.Fragments.General

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.epi.Fragments.General.ChangePlanValues.ChangePlanValuesFragmentDirections
import com.example.epi.R
import com.example.epi.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка текущей темы
        val sharedPreferences = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("theme", "system") ?: "system"
        when (currentTheme) {
            "light" -> binding.radioLight.isChecked = true
            "dark" -> binding.radioDark.isChecked = true
            else -> binding.radioSystem.isChecked = true
        }

        // Обработка выбора темы
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPreferences.edit()
            when (checkedId) {
                R.id.radioLight -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putString("theme", "light")
                }
                R.id.radioDark -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putString("theme", "dark")
                }
                R.id.radioSystem -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    editor.putString("theme", "system")
                }
            }
            editor.apply()
        }

        // Кнопка возврата
        binding.buttonBack.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingFragmentToStartFragment()
            findNavController().navigate(action)
        }

        binding.btnChangePlanValues.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingFragmentToChangePlanValuesFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}