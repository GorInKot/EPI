package com.example.epi.Fragments.General

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbSettings.setOnClickListener {
            findNavController().navigate(R.id.SettingFragment)
        }

        binding.btnArrangement.setOnClickListener {
            findNavController().navigate(R.id.arrangementFragment)
        }

        binding.btnAuth.setOnClickListener {
            findNavController().navigate(R.id.authFragment)
        }

        binding.btnReports.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }

        binding.btnLogOut.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                // Очистка сессии при подтверждении
                val sharedPreferences = requireContext().getSharedPreferences("User_session", android.content.Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                findNavController().navigate(R.id.authFragment)
            }
            .setNegativeButton("Нет", null) // Ничего не делаем при отказе
            .setCancelable(true) // Позволяет закрыть диалог по кнопке "Назад"
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}