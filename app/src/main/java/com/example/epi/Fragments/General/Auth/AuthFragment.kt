package com.example.epi.Fragments.General.Auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentAuthBinding
import kotlin.getValue

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val maxNumberLength = 4
    private val maxPasswordLength = 12

    private val viewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        buttons()
        // observeAuthResult()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textInputEditTextNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(maxNumberLength))
        binding.textInputEditTextPassword.filters = arrayOf(android.text.InputFilter.LengthFilter(maxPasswordLength))
    }

//    private fun observeAuthResult() {
//        viewModel.authResult.observe(viewLifecycleOwner) { result ->
//            when (result) {
//                is SharedViewModel.AuthResult.Success -> {
//                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
//                    binding.textInputEditTextNumber.text?.clear()
//                    binding.textInputEditTextPassword.text?.clear()
//                    findNavController().navigate(R.id.StartFragment)
//                }
//                is SharedViewModel.AuthResult.Error -> {
//                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
//                    if (result.message.contains("Неверный пароль")) {
//                        binding.textInputLayoutPassword.isErrorEnabled = true
//                        binding.textInputLayoutPassword.error = result.message
//                    } else {
//                        binding.textInputLayoutNumber.isErrorEnabled = true
//                        binding.textInputLayoutNumber.error = result.message
//                    }
//                }
//                is SharedViewModel.AuthResult.RegistrationSuccess,
//                is SharedViewModel.AuthResult.RegistrationError,
//                is SharedViewModel.AuthResult.Idle -> {
//                    // Игнорируем состояния, связанные с регистрацией или сбросом
//                }
//            }
//        }
//    }

    private fun buttons() {
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
//            if (validateInputs()) {
//                Log.d("Tagg", "Validation passed")
//                loginUser()
//            } else {
//                Log.d("Tagg", "Validation failed")
//            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.registrationFragment)
        }
    }

//    private fun validateInputs(): Boolean {
//        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
//        val password = binding.textInputEditTextPassword.text?.toString()?.trim()
//
//        val errors = viewModel.validateAuthInputs(number, password)
//        binding.textInputLayoutNumber.error = null
//        binding.textInputLayoutPassword.error = null
//
//        binding.textInputLayoutNumber.isErrorEnabled = !errors["number"].isNullOrBlank()
//        binding.textInputLayoutNumber.error = errors["number"]
//
//        binding.textInputLayoutPassword.isErrorEnabled = !errors["password"].isNullOrBlank()
//        binding.textInputLayoutPassword.error = errors["password"]
//
//        return errors.isEmpty()
//    }

//    private fun loginUser() {
//        val employeeNumber = binding.textInputEditTextNumber.text.toString().trim()
//        val password = binding.textInputEditTextPassword.text.toString().trim()
//        Log.d("Tagg", "Attempting login with employeeNumber: $employeeNumber")
//        viewModel.loginUser(employeeNumber, password)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}