package com.example.epi.Fragments.General.Auth

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.DataBase.ExtraDatabase.ExtraDatabaseHelper
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentAuthBinding
import kotlin.getValue

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val minPasswordLength = 6
    private var isLoggingIn = false

    private val viewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext,
            (requireActivity().application as App).planValueRepository,
            (requireActivity().application as App).orderNumberRepository,
            (requireActivity().application as App).factValueRepository
        )
    }

    companion object {
        private val TAG = "Tagg-Auth"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        buttons()
        observeAuthResult()
        setupTypeOfWorkDropdown()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.textInputEditTextPassword.filters = arrayOf(android.text.InputFilter.LengthFilter(minPasswordLength))

        // Автозаполнение уникального номера (логина) и пароля
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.textInputEditTextNumber.setAutofillHints(View.AUTOFILL_HINT_USERNAME)
            binding.textInputEditTextPassword.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)
        }

    }

    private fun setupTypeOfWorkDropdown() {
        // Получение данных из таблицы TypeOfWork
        val dbHelper = ExtraDatabaseHelper(requireContext())
        val typeOfWorks = dbHelper.getTypeOfWorks()
        // Настройка адаптера для AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, // Используйте стандартный layout для выпадающего списка или создайте свой
            typeOfWorks
        )
        binding.autoCompleteWorkType.setAdapter(adapter)

        // Обработка выбора элемента
        binding.autoCompleteWorkType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = typeOfWorks[position]
            viewModel.setSelectedTypeOfWork(selectedType)
            Log.d(TAG, "Выбран тип работы: $selectedType")
        }
    }

    private fun observeAuthResult() {
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SharedViewModel.AuthResult.Success -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "result.message: ${result.message}")
                    binding.textInputEditTextNumber.text?.clear()
                    binding.textInputEditTextPassword.text?.clear()
                    findNavController().navigate(R.id.StartFragment)
                }
                is SharedViewModel.AuthResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    if (result.message.contains("Неверный пароль")) {
                        binding.textInputLayoutPassword.isErrorEnabled = true
                        binding.textInputLayoutPassword.error = result.message
                    } else {
                        binding.textInputLayoutNumber.isErrorEnabled = true
                        binding.textInputLayoutNumber.error = result.message
                    }
                }
                is SharedViewModel.AuthResult.RegistrationSuccess,
                is SharedViewModel.AuthResult.RegistrationError,
                is SharedViewModel.AuthResult.Idle -> {
                    // Игнорируем состояния, связанные с регистрацией или сбросом
                }
            }
            isLoggingIn = false
        }
    }

    private fun buttons() {
        binding.btnLogin.setOnClickListener {
            if(isLoggingIn) return@setOnClickListener
            if (validateInputs()) {
                Log.d(TAG, "Валидация пройдена")
                loginUser()
            } else {
                Log.d(TAG,
                    "Валидация НЕ пройдена: ${viewModel.validateAuthInputs(
                    binding.textInputEditTextNumber.text?.toString()?.trim(),
                    binding.textInputEditTextPassword.text?.toString()?.trim(),
                    binding.autoCompleteWorkType.text?.toString()?.trim()
                )}")
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_AuthFragment_to_RegistrationFragment)
        }
    }

    private fun validateInputs(): Boolean {
        binding.textInputLayoutNumber.error = null
        binding.textInputLayoutPassword.error = null

        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
        val password = binding.textInputEditTextPassword.text?.toString()?.trim()
        val typeOfWork = binding.autoCompleteWorkType.text?.toString()?.trim()

        val errors = viewModel.validateAuthInputs(number, password, typeOfWork)

        binding.textInputLayoutNumber.isErrorEnabled = !errors["number"].isNullOrBlank()
        binding.textInputLayoutNumber.error = errors["number"]

        binding.textInputLayoutPassword.isErrorEnabled = !errors["password"].isNullOrBlank()
        binding.textInputLayoutPassword.error = errors["password"]

        binding.textInputLayoutWorkType.isErrorEnabled = !errors["typeOfWork"].isNullOrBlank()
        binding.textInputLayoutWorkType.error = errors["typeOfWork"]

        return errors.isEmpty()

    }

    private fun loginUser() {
        val employeeNumber = binding.textInputEditTextNumber.text.toString().trim()
        val password = binding.textInputEditTextPassword.text.toString().trim()
        Log.d(TAG, "Попытка входа для сотрудника с номером: $employeeNumber")
        viewModel.loginUser(employeeNumber, password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}