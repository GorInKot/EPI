package com.example.epi.Fragments.General.Auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.ViewModel.GeneralViewModel
import com.example.epi.databinding.FragmentAuthBinding


class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val maxNumberLength = 4
    private val maxPasswordLength = 12

    private val viewModel: GeneralViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAuthBinding.inflate(inflater, container, false)

        buttons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Табельный номер
        binding.textInputEditTextNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(maxNumberLength))

        // Пароль
        binding.textInputEditTextPassword.filters = arrayOf(android.text.InputFilter.LengthFilter(maxPasswordLength))

    }

    private fun buttons() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()){
                Log.d("Tagg", "123")
                Toast.makeText(requireContext(), "Здесь скоро будет авторизация", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.StartFragment)
            } else {
                Log.d("Tagg", "000")
            }

        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.registrationFragment)
        }

        binding.btnBackToMenu.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

    private fun validateInputs(): Boolean {

        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
        val password = binding.textInputEditTextPassword.text?.toString()?.trim()

        val errors = viewModel.validateAuthInputs(
            number,
            password
        )
        // Сброс старых ошибок
        binding.textInputLayoutNumber.error = null
        binding.textInputLayoutPassword.error = null

        // Установка новых ошибок
        binding.textInputLayoutNumber.isErrorEnabled = !errors["number"].isNullOrBlank()
        binding.textInputLayoutNumber.error = errors["number"]

        binding.textInputLayoutPassword.isErrorEnabled = !errors["password"].isNullOrBlank()
        binding.textInputLayoutPassword.error = errors["password"]

        return errors.isEmpty()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}