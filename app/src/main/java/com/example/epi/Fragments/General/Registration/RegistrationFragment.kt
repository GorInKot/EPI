package com.example.epi.Fragments.General.Registration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentRegistrationBinding
import kotlin.math.max


class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel

    private val countryCodePrefix = "+7"
    private val maxLength = 18

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val autoCompleteBranch = binding.autoCompleteTextViewBranch
        val autoCompletePu = binding.autoCompleteTextViewPU

        val branches = branchesWithPu.keys.toList()

        // Адаптер для филиала
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, branches)
        autoCompleteBranch.setAdapter(branchAdapter)

        autoCompleteBranch.setOnItemClickListener { parent, view, position, id ->
            val selectedBranch = branches[position]
            val cars = branchesWithPu[selectedBranch] ?: emptyList()

            val puAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cars)
            autoCompletePu.setAdapter(puAdapter)
            autoCompletePu.text?.clear() // очистим выбор ПУ при смене филиала
        }


        binding.btnMainMenu.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }

        binding.btnAuth.setOnClickListener {
            findNavController().navigate(R.id.authFragment)
        }

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
                    } else {
                        formatted.append("_")
                    }
                }

                formatted.append(") ")

                // Вторая группа — 3 цифры или плейсхолдеры
                for (i in 0 until 3) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }

                formatted.append("-")

                // Третья группа — 2 цифры или плейсхолдеры
                for (i in 0 until 2) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }

                formatted.append("-")

                // Четвёртая группа — 2 цифры или плейсхолдеры
                for (i in 0 until 2) {
                    if (index < digits.length) {
                        formatted.append(digits[index])
                        index++
                    } else {
                        formatted.append("_")
                    }
                }

                if (formatted.length > maxLength) {
                    formatted.setLength(maxLength)
                }

                val newText = formatted.toString()

                if (newText != text) {
                    phoneEditText.setText(newText)
                    phoneEditText.setSelection(minOf(newText.length, phoneEditText.text!!.length))
                }

                isFormatting = false
            }



        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}