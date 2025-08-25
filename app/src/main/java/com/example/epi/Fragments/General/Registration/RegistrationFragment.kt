package com.example.epi.Fragments.General.Registration

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentRegistrationBinding

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext,
            (requireActivity().application as App).planValueRepository,
        )
    }
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

    companion object {
        private val TAG = "Tagg-Registration"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        buttons()
//        observeAuthResult()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Уникальный номер сотрудника
        binding.textInputEditTextNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(maxNumberLength))

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
        binding.btnAuth.setOnClickListener {
            findNavController().navigate(R.id.action_RegistrationFragment_to_AuthFragment)
        }
        binding.btnRegister.setOnClickListener {
            Log.d(TAG, "Нажали кнопку регистрации")
            if (validateInputs()) {
                Log.d(TAG, "Успешная регистрация")
                registerUser()
                findNavController().navigate(R.id.action_RegistrationFragment_to_AuthFragment)
            } else {
                Log.d(TAG, "Ошибка регистрации")
            }
        }
    }

    private fun validateInputs(): Boolean {
        val secondName = binding.textInputEditTextSecondName.text?.toString()?.trim()
        val firstName = binding.textInputEditTextFirstName.text?.toString()?.trim()
        val thirdName = binding.textInputEditTextThirdName.text?.toString()?.trim()
        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
        val branch = binding.autoCompleteTextViewBranch.text?.toString()?.trim()
        val pu = binding.autoCompleteTextViewPU.text?.toString()?.trim()
        val password = binding.textInputEditTextPassword.text?.toString()?.trim()
        val confirmPassword = binding.textInputEditTextConfirmPassword.text?.toString()?.trim()

        // Валидация через ViewModel
        val errors = viewModel.validateRegistrationInputs(
            secondName, firstName, thirdName, number, branch, pu, password, confirmPassword
        )

        binding.textInputLayoutSecondName.isErrorEnabled = !errors["secondName"].isNullOrBlank()
        binding.textInputLayoutSecondName.error = errors["secondName"]
        binding.textInputLayoutFirstName.isErrorEnabled = !errors["firstName"].isNullOrBlank()
        binding.textInputLayoutFirstName.error = errors["firstName"]
        binding.textInputLayoutThirdName.isErrorEnabled = !errors["thirdName"].isNullOrBlank()
        binding.textInputLayoutThirdName.error = errors["thirdName"]
        binding.textInputLayoutNumber.isErrorEnabled = !errors["number"].isNullOrBlank()
        binding.textInputLayoutNumber.error = errors["number"]
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
        val secondName = binding.textInputEditTextSecondName.text.toString().trim()
        val firstName = binding.textInputEditTextFirstName.text.toString().trim()
        val thirdName = binding.textInputEditTextThirdName.text.toString().trim().takeIf { it.isNotBlank() }
        val number = binding.textInputEditTextNumber.text.toString().trim()

        val branch = binding.autoCompleteTextViewBranch.text.toString().trim()
        val pu = binding.autoCompleteTextViewPU.text.toString().trim()
        val password = binding.textInputEditTextPassword.text.toString().trim()

        Log.d(TAG, "Попытка регистрации для сотрудника с номером: $number")
        viewModel.registerUser(secondName, firstName, thirdName, number, branch, pu, password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}