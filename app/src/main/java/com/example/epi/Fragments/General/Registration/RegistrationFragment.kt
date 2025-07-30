package com.example.epi.Fragments.General.Registration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.DataBase.User.User
import com.example.epi.R
import com.example.epi.ViewModel.GeneralViewModel
import com.example.epi.databinding.FragmentRegistrationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GeneralViewModel by viewModels()
    private val userRepository by lazy { (requireContext().applicationContext as App).userRepository }
    private val countryCodePrefix = "+7"
    private val maxPhoneLength = 18
    private val maxNumberLength = 4

    private val branchesWithPu = mapOf(
        "Тюмень" to listOf("ПУ ЮНГ", "ПУ Нижневартовск", "ПУ Тюмень", "ПУ Новый Уренгой", "ПУ Губкинский"),
        "Красноярск" to listOf(
            "ПУ Восток",
            "ПУ Томск",
            "ПУ Ачинск",
            "ПУ Ванкор",
            "ПУ Славнефть",
            "ПУ Иркутск",
            "ПУ Таас-Юрях",
            "ПУ Ангарск",
            "ПУ Комсомольск-на-Амуре"
        ),
        "Уфа" to listOf(
            "ПУ Рязань",
            "ПУ Краснодар",
            "ПУ Туапсе",
            "ПУ Новокуйбышевск",
            "ПУ Самара",
            "ПУ Сызрань",
            "ПУ Саратов",
            "ПУ Бузулук",
            "ПУ Уфа",
            "ПУ БН-Добыча",
            "ПУ Ижевск"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        buttons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Табельный номер
        binding.textInputEditTextNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(maxNumberLength))

        // Телефон
        val phoneEditText = binding.textInputEditTextPhone
        if (phoneEditText.text.isNullOrEmpty()) {
            phoneEditText.setText(countryCodePrefix)
            phoneEditText.setSelection(phoneEditText.text!!.length)
        }
        phoneEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && phoneEditText.text.isNullOrEmpty()) {
                phoneEditText.setText(countryCodePrefix)
                phoneEditText.setSelection(phoneEditText.text!!.length)
            }
        }
        phoneEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                if (s == null) {
                    isFormatting = false
                    return
                }
                val text = s.toString()
                val digitsAll = text.filter { it.isDigit() }
                val digits = if (digitsAll.startsWith("7")) digitsAll.drop(1) else digitsAll
                val formatted = StringBuilder()
                var index = 0
                formatted.append("+7 (")
                for (i in 0 until 3) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }
                formatted.append(") ")
                for (i in 0 until 3) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }
                formatted.append("-")
                for (i in 0 until 2) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }
                formatted.append("-")
                for (i in 0 until 2) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }
                if (formatted.length > maxPhoneLength) {
                    formatted.setLength(maxPhoneLength)
                }
                val newText = formatted.toString()
                if (newText != text) {
                    phoneEditText.removeTextChangedListener(this)
                    phoneEditText.setText(newText)
                    phoneEditText.setSelection(minOf(newText.length, phoneEditText.text!!.length))
                    phoneEditText.addTextChangedListener(this)
                }
                isFormatting = false
            }
        })

        // Филиал и ПУ
        val autoCompleteBranch = binding.autoCompleteTextViewBranch
        val autoCompletePu = binding.autoCompleteTextViewPU
        val branches = branchesWithPu.keys.toList()
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, branches)
        autoCompleteBranch.setAdapter(branchAdapter)
        autoCompleteBranch.setOnItemClickListener { parent, view, position, id ->
            val selectedBranch = branches[position]
            val cars = branchesWithPu[selectedBranch] ?: emptyList()
            val puAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cars)
            autoCompletePu.setAdapter(puAdapter)
            autoCompletePu.text?.clear()
        }
    }

    private fun buttons() {
        binding.btnMainMenu.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
        binding.btnAuth.setOnClickListener {
            findNavController().navigate(R.id.authFragment)
        }
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val secondName = binding.textInputEditTextSecondName.text?.toString()?.trim()
        val firstName = binding.textInputEditTextFirstName.text?.toString()?.trim()
        val thirdName = binding.textInputEditTextThirdName.text?.toString()?.trim()
        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
        val phone = binding.textInputEditTextPhone.text?.toString()?.trim()
        val branch = binding.autoCompleteTextViewBranch.text?.toString()?.trim()
        val pu = binding.autoCompleteTextViewPU.text?.toString()?.trim()
        val password = binding.textInputEditTextPassword.text?.toString()?.trim()
        val confirmPassword = binding.textInputEditTextConfirmPassword.text?.toString()?.trim()

        // Валидация через ViewModel
        val errors = viewModel.validateRegistrationInputs(
            secondName, firstName, thirdName, number, phone, branch, pu, password, confirmPassword
        )

        binding.textInputLayoutSecondName.isErrorEnabled = !errors["secondName"].isNullOrBlank()
        binding.textInputLayoutSecondName.error = errors["secondName"]
        binding.textInputLayoutFirstName.isErrorEnabled = !errors["firstName"].isNullOrBlank()
        binding.textInputLayoutFirstName.error = errors["firstName"]
        binding.textInputLayoutThirdName.isErrorEnabled = !errors["thirdName"].isNullOrBlank()
        binding.textInputLayoutThirdName.error = errors["thirdName"]
        binding.textInputLayoutNumber.isErrorEnabled = !errors["number"].isNullOrBlank()
        binding.textInputLayoutNumber.error = errors["number"]
        binding.textInputLayoutPhone.isErrorEnabled = !errors["phone"].isNullOrBlank()
        binding.textInputLayoutPhone.error = errors["phone"]
        binding.textInputLayoutBranch.isErrorEnabled = !errors["branch"].isNullOrBlank()
        binding.textInputLayoutBranch.error = errors["branch"]
        binding.textInputLayoutPU.isErrorEnabled = !errors["pu"].isNullOrBlank()
        binding.textInputLayoutPU.error = errors["pu"]
        binding.textInputLayoutPassword.isErrorEnabled = !errors["password"].isNullOrBlank()
        binding.textInputLayoutPassword.error = errors["password"]
        binding.textInputLayoutConfirmPassword.isErrorEnabled = !errors["confirmPassword"].isNullOrBlank()
        binding.textInputLayoutConfirmPassword.error = errors["confirmPassword"]

        return errors.isEmpty()
    }

    private fun registerUser() {
        val user = User(
            secondName = binding.textInputEditTextSecondName.text.toString().trim(),
            firstName = binding.textInputEditTextFirstName.text.toString().trim(),
            thirdName = binding.textInputEditTextThirdName.text.toString().trim().takeIf { it.isNotBlank() },
            employeeNumber = binding.textInputEditTextNumber.text.toString().trim(),
            phone = binding.textInputEditTextPhone.text.toString().trim(),
            branch = binding.autoCompleteTextViewBranch.text.toString().trim(),
            pu = binding.autoCompleteTextViewPU.text.toString().trim(),
            password = BCrypt.hashpw(binding.textInputEditTextPassword.text.toString().trim(), BCrypt.gensalt())
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (userRepository.isEmployeeNumberTaken(user.employeeNumber)) {
                    requireActivity().runOnUiThread {
                        binding.textInputLayoutNumber.isErrorEnabled = true
                        binding.textInputLayoutNumber.error = "Табельный номер уже занят"
                    }
                } else {
                    userRepository.insertUser(user)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Регистрация успешна", Toast.LENGTH_SHORT).show()
                        // Очистка полей после успешной регистрации
                        binding.textInputEditTextSecondName.text?.clear()
                        binding.textInputEditTextFirstName.text?.clear()
                        binding.textInputEditTextThirdName.text?.clear()
                        binding.textInputEditTextNumber.text?.clear()
                        binding.textInputEditTextPhone.text?.clear()
                        binding.autoCompleteTextViewBranch.text?.clear()
                        binding.autoCompleteTextViewPU.text?.clear()
                        binding.textInputEditTextPassword.text?.clear()
                        binding.textInputEditTextConfirmPassword.text?.clear()
                        findNavController().navigate(R.id.authFragment)
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}