package com.example.epi.Fragments.General.Registration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentRegistrationBinding


class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RegistrationViewModel

    private val countryCodePrefix = "+7"
    private val maxPhoneLength = 18
    private val maxNumberLength = 4

    private val branchesWithPu = mapOf(
        "Тюмень" to listOf(
            "ПУ ЮНГ", "ПУ Нижневартовск",
            "ПУ Тюмень", "ПУ Новый Уренгой", "ПУ Губкинский"
        ),

        "Красноярск" to listOf(
            "ПУ Восток", "ПУ Томск", "ПУ Ачинск", "ПУ Ванкор", "ПУ Славнефть",
            "ПУ Иркутск", "ПУ Таас-Юрях", "ПУ Ангарск", "ПУ Комсомольск-на-Амуре"
        ),

        "Уфа" to listOf(
            "ПУ Рязань", "ПУ Краснодар", "ПУ Туапсе",
            "ПУ Новокуйбышевск", "ПУ Самара", "ПУ Сызрань",
            "ПУ Саратов", "ПУ Бузулук", "ПУ Уфа",
            "ПУ БН-Добыча", "ПУ Ижевск"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        buttons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Табельный номер
        binding.textInputEditTextNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(maxNumberLength))

        // Телефон
        val phoneEditText = binding.textInputEditTextPhone

        // Установим +7, если поле пустое
        if (phoneEditText.text.isNullOrEmpty()) {
            phoneEditText.setText(countryCodePrefix)
            phoneEditText.setSelection(phoneEditText.text!!.length)
        }

        // При фокусе, если поле пустое — добавим +7
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

                // Первая группа — 3 цифры или плейсхолдеры
                for (i in 0 until 3) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else { formatted.append("_") }
                }

                formatted.append(") ")

                // Вторая группа — 3 цифры или плейсхолдеры
                for (i in 0 until 3) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else { formatted.append("_") }
                }

                formatted.append("-")

                // Третья группа — 2 цифры или плейсхолдеры
                for (i in 0 until 2) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else { formatted.append("_") }
                }

                formatted.append("-")

                // Четвёртая группа — 2 цифры или плейсхолдеры
                for (i in 0 until 2) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else { formatted.append("_") }
                }

                if (formatted.length > maxPhoneLength) { formatted.setLength(maxPhoneLength) }
                val newText = formatted.toString()
                if (newText != text) {
                    phoneEditText.setText(newText)
                    phoneEditText.setSelection(minOf(newText.length, phoneEditText.text!!.length))
                }

                isFormatting = false
            }
        })

        // Филиал и ПУ
        val autoCompleteBranch = binding.autoCompleteTextViewBranch
        val autoCompletePu = binding.autoCompleteTextViewPU
        val branches = branchesWithPu.keys.toList()

        // Адаптер для филиала
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, branches)
        autoCompleteBranch.setAdapter(branchAdapter)

        // Обработка нажатия на Филиал
        autoCompleteBranch.setOnItemClickListener { parent, view, position, id ->
            val selectedBranch = branches[position]
            val cars = branchesWithPu[selectedBranch] ?: emptyList()

            // Адаптер для ПУ
            val puAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cars)
            autoCompletePu.setAdapter(puAdapter)
            // очистим выбор ПУ при смене филиала
            autoCompletePu.text?.clear()
        }

    }
    // Кнопки
    private fun buttons() {
        // Обработка кнопки "Главное меню"
        binding.btnMainMenu.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }

        // Обработка кнопки "Авторизоваться"
        binding.btnAuth.setOnClickListener {
            findNavController().navigate(R.id.authFragment)
        }

        // Обработка кнопки "Зарегистрироваться"
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                Toast.makeText(requireContext(), "Здесь скоро будет регистрация", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Валидация полей
    private fun validateInputs(): Boolean {

        var isValidate = true

        // Фамилия
        val secondName = binding.textInputEditTextSecondName.text?.toString()?.trim()
        if (secondName.isNullOrBlank()) {
            binding.textInputLayoutSecondName.error = "Введите фамилию"
            isValidate = false
        } else {
            binding.textInputLayoutSecondName.error = null
        }

        // Имя
        val firstName = binding.textInputEditTextFirstName.text?.toString()?.trim()
        if (firstName.isNullOrBlank()) {
            binding.textInputLayoutFirstName.error = "Введите имя"
            isValidate = false
        } else {
            binding.textInputLayoutFirstName.error = null
        }
        // Отчетство
        val thirdName = binding.textInputEditTextThirdName.text?.toString()?.trim()
        if (thirdName.isNullOrBlank()) {
            binding.textInputLayoutThirdName.error = "Введите отчество"
            isValidate = false
        } else {
            binding.textInputLayoutThirdName.error = null
        }

        // Табельный номер
        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
        if (number.isNullOrBlank()) {
            binding.textInputLayoutNumber.error = "Введите табельный номер"
            isValidate = false
        } else {
            binding.textInputLayoutNumber.error = null
        }

        // Телефон
        val phone = binding.textInputEditTextPhone.text?.toString()?.trim()
        val phoneDigits = phone?.filter { it.isDigit() }

        if (phoneDigits.isNullOrBlank() || phoneDigits.length != 11) {
            binding.textInputLayoutPhone.error = "Введите корректный номер телефона"
            isValidate = false
        } else {
            binding.textInputLayoutPhone.error = null
        }

        // Филиал
        val branch = binding.autoCompleteTextViewBranch.text?.toString()?.trim()
        if (branch.isNullOrBlank()) {
            binding.textInputLayoutBranch.error = "Выберите филиал"
            isValidate = false
        }

        // ПУ
        val pu = binding.autoCompleteTextViewPU.text?.toString()?.trim()
        if (pu.isNullOrBlank()) {
            binding.textInputLayoutPU.error = "Выберите ПУ"
            isValidate = false
        }

        return isValidate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}