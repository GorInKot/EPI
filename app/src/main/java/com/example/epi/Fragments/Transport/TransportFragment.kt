package com.example.epi.Fragments.Transport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentTransportBinding
import java.text.SimpleDateFormat
import java.util.*

class TransportFragment : Fragment() {

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransportViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransportViewModel::class.java]

        setupObservers()
        setupInputListeners()
        setupButtons()
    }

    private fun setupObservers() {
        viewModel.isTransportAbsent.observe(viewLifecycleOwner) {
            binding.chBoxMCustomer.isChecked = it
            setFieldsEnabled(!it)
        }
    }

    private fun setupInputListeners() {
        // Чекбокс
        binding.chBoxMCustomer.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTransportAbsent(isChecked)
        }

        // Дата начала поездки
        setupDateInput(binding.textInputEditTextStartDate)
        setupDateInput(binding.textInputEditTextEndDate)

        // Время начала и окончания
        setupTimeInput(binding.textInputEditTextStartDateHours, binding.textInputLayoutStartDateHours)
        setupTimeInput(binding.textInputEditTextEndDateHours, binding.textInputLayoutEndDateHours)

        // Прочие текстовые поля
        binding.textInputEditTextCustomer.doAfterTextChanged {
            viewModel.customerName.value = it.toString()
        }
        binding.TextInputEditTextContract.doAfterTextChanged {
            viewModel.contractCustomer.value = it.toString()
        }
        binding.TextInputEditTextExecutor.doAfterTextChanged {
            viewModel.executorName.value = it.toString()
        }
        binding.textInputEditTextContractTransport.doAfterTextChanged {
            viewModel.contractTransport.value = it.toString()
        }
        binding.textInputEditTextStateNumber.doAfterTextChanged {
            viewModel.stateNumber.value = it.toString()
        }
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            if (viewModel.validateInputs()) {
                findNavController().navigate(R.id.controlFragment)
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupDateInput(editText: androidx.appcompat.widget.AppCompatEditText) {
        editText.doAfterTextChanged {
            val input = it?.toString() ?: return@doAfterTextChanged
            val clean = input.replace("[^\\d]".toRegex(), "")
            val formatted = when {
                clean.length <= 2 -> clean
                clean.length <= 4 -> "${clean.substring(0, 2)}.${clean.substring(2)}"
                clean.length <= 8 -> "${clean.substring(0, 2)}.${clean.substring(2, 4)}.${clean.substring(4)}"
                else -> "${clean.substring(0, 2)}.${clean.substring(2, 4)}.${clean.substring(4, 8)}"
            }

            if (input != formatted) {
                editText.setText(formatted)
                editText.setSelection(formatted.length.coerceAtMost(editText.text?.length ?: 0))
            }
        }
    }

    private fun setupTimeInput(
        editText: androidx.appcompat.widget.AppCompatEditText,
        inputLayout: com.google.android.material.textfield.TextInputLayout
    ) {
        editText.doAfterTextChanged {
            val time = it?.toString() ?: return@doAfterTextChanged
            if (!isValidTimeFormat(time)) {
                inputLayout.error = "Неверный формат. Введите чч:мм"
            } else {
                inputLayout.error = null
            }
        }
    }

    private fun isValidTimeFormat(time: String): Boolean {
        return time.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d\$"))
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.textInputEditTextCustomer.isEnabled = enabled
        binding.TextInputEditTextContract.isEnabled = enabled
        binding.TextInputEditTextExecutor.isEnabled = enabled
        binding.textInputEditTextContractTransport.isEnabled = enabled
        binding.textInputEditTextStateNumber.isEnabled = enabled
        binding.textInputEditTextStartDateHours.isEnabled = enabled
        binding.textInputEditTextEndDateHours.isEnabled = enabled
        binding.textInputEditTextStartDate.isEnabled = enabled
        binding.textInputEditTextEndDate.isEnabled = enabled
    }

    private fun isValidDateFormat(date: String): Boolean {
        val regex = Regex("""^(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[0-2])\.(19|20)\d{2}$""")
        return regex.matches(date)
    }

    private fun updateStartDateUI(calendar: Calendar) {
        binding.textInputEditTextStartDate.setText(
            String.format(
                "%02d.%02d.%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
            )
        )
        binding.textInputEditTextStartDateHours.setText(
            String.format(
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        )
    }

    private fun updateEndDateUI(calendar: Calendar) {
        binding.textInputEditTextEndDate.setText(
            String.format(
                "%02d.%02d.%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
            )
        )
        binding.textInputEditTextEndDateHours.setText(
            String.format(
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
