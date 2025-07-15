package com.example.epi.Fragments.Transport

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.ViewModel.SharedViewModel
import com.example.epi.databinding.FragmentTransportBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TransportFragment : Fragment() {

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransportVIewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupInputListeners()
        setupButtons()
        restoreInputs()
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
            viewModel.clearTransport()


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
        binding.textInputEditTextContract.doAfterTextChanged {
            viewModel.contractCustomer.value = it.toString()
        }
        binding.textInputEditTextExecutor.doAfterTextChanged {
            viewModel.executorName.value = it.toString()
        }
        binding.textInputEditTextContractTransport.doAfterTextChanged {
            viewModel.contractTransport.value = it.toString()
        }
        binding.textInputEditTextStateNumber.doAfterTextChanged {
            viewModel.stateNumber.value = it.toString()
        }
        binding.textInputEditTextStartDate.doAfterTextChanged {
            viewModel.startDate.value = it.toString()
        }
        binding.textInputEditTextStartDateHours.doAfterTextChanged {
            viewModel.startTime.value = it.toString()
        }
        binding.textInputEditTextEndDate.doAfterTextChanged {
            viewModel.endDate.value = it.toString()
        }
        binding.textInputEditTextEndDateHours.doAfterTextChanged {
            viewModel.endTime.value = it.toString()
        }

    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                findNavController().navigate(R.id.controlFragment)
            }
        }
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.arrangementFragment)
        }
    }

    private fun setupDateInput(editText: androidx.appcompat.widget.AppCompatEditText) {
        var isFormatting = false
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isFormatting) return
                val clean = s?.toString()?.replace("[^\\d]".toRegex(), "") ?: return
                val formatted = StringBuilder()
                if (clean.length > 8) return  // Ограничиваем максимум до ддММгггг
                var cursorPosition = 0
                if (clean.length >= 2) {
                    formatted.append(clean.substring(0, 2)).append(".")
                    cursorPosition = 3
                } else {
                    formatted.append(clean)
                    cursorPosition = clean.length
                }
                if (clean.length >= 4) {
                    formatted.append(clean.substring(2, 4)).append(".")
                    cursorPosition = 6
                } else if (clean.length > 2) {
                    formatted.append(clean.substring(2))
                    cursorPosition = clean.length + 1
                }
                if (clean.length > 4) {
                    formatted.append(clean.substring(4))
                    cursorPosition = formatted.length
                }
                isFormatting = true
                editText.setText(formatted.toString())
                editText.setSelection(cursorPosition.coerceAtMost(formatted.length))
                isFormatting = false
                // Проверка Валидности даты
                if (formatted.length == 10) {
                    if (!isValidDate(formatted.toString())) {
                        val parent = editText.parent?.parent
                        if (parent is com.google.android.material.textfield.TextInputLayout) {
                            parent.error = "Неверная дата"
                        }
                    } else {
                        val parent = editText.parent?.parent
                        if (parent is com.google.android.material.textfield.TextInputLayout) {
                            parent.error = null
                        }
                    }
                } else {
                    // Если дата неполная
                    val parent = editText.parent?.parent
                    if (parent is com.google.android.material.textfield.TextInputLayout) {
                        parent.error = null
                    }
                }
            }
        })
    }

    private fun restoreInputs() {
        binding.textInputEditTextCustomer.setText(viewModel.customerName.value)
        binding.textInputEditTextContract.setText(viewModel.contractCustomer.value)
        binding.textInputEditTextExecutor.setText(viewModel.executorName.value)
        binding.textInputEditTextContractTransport.setText(viewModel.contractTransport.value)
        binding.textInputEditTextStateNumber.setText(viewModel.stateNumber.value)
        binding.textInputEditTextStartDate.setText(viewModel.startDate.value)
        binding.textInputEditTextStartDateHours.setText(viewModel.startTime.value)
        binding.textInputEditTextEndDate.setText(viewModel.endDate.value)
        binding.textInputEditTextEndDateHours.setText(viewModel.endTime.value)
    }


    private fun isValidDate(date: String):Boolean {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun setupTimeInput(
        editText: AppCompatEditText,
        inputLayout: TextInputLayout
    ) {
        var previous = "" // Чтобы избежать бесконечного цикла обновлений

        editText.doAfterTextChanged {
            val input = it?.toString() ?: return@doAfterTextChanged
            val clean = input.replace("[^\\d]".toRegex(), "")

            // Не реагируем на обновление, если значение не изменилось
            if (clean == previous.replace(":", "")) return@doAfterTextChanged

            val formatted = when {
                clean.length <= 2 -> clean
                clean.length <= 4 -> "${clean.substring(0, 2)}:${clean.substring(2)}"
                else -> "${clean.substring(0, 2)}:${clean.substring(2, 4)}"
            }

            previous = formatted

            // Устанавливаем текст только если отличается
            if (input != formatted) {
                editText.setText(formatted)
                editText.setSelection(formatted.length.coerceAtMost(editText.text?.length ?: 0))
            }

            // Проверка формата и ошибки
            if (!isValidTimeFormat(formatted)) {
                inputLayout.error = "Неверный формат: чч:мм"
            } else {
                inputLayout.error = null
            }
        }
    }

    private fun validateInputs(): Boolean {

        val isTransportAbsent = binding.chBoxMCustomer.isChecked

        val customer = binding.textInputEditTextCustomer.text?.toString()?.trim()
        val contract = binding.textInputEditTextContract.text?.toString()?.trim()
        val executor = binding.textInputEditTextExecutor.text?.toString()?.trim()
        val contractTransport = binding.textInputEditTextContractTransport.text?.toString()?.trim()
        val dateStart = binding.textInputEditTextStartDate.text?.toString()?.trim()
        val timeStart = binding.textInputEditTextStartDateHours.text?.toString()?.trim()
        val number = binding.textInputEditTextStateNumber.text?.toString()?.trim()
        val dateEnd = binding.textInputEditTextEndDate.text?.toString()?.trim()
        val endTime = binding.textInputEditTextEndDateHours.text?.toString()?.trim()

        val errors = viewModel.validateTransportInputs(
            _isTransportAbsent=isTransportAbsent,
            customerName=customer,
            contractCustomer=contract,
            executorName=executor,
            contractTransport=contractTransport,
            stateNumber=number,
            startDate=dateStart,
            startTime=timeStart,
            endDate=dateEnd,
            endTime=endTime
        )

        // Показать Snackbar при наличии ошибок
        if (errors.isNotEmpty()) {
            Snackbar
                .make(binding.root, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }

        // ---------- Очистка ошибок ----------
        clearErrors(
            binding.textInputLayoutCustomer,
            binding.textInputLayoutContract,
            binding.textInputLayoutExecutor,
            binding.textInputLayoutContractTransport,
            binding.textInputLayoutStartDate,
            binding.textInputLayoutStartDateHours,
            binding.textInputLayoutStateNumber,
            binding.textInputLayoutEndDate,
            binding.textInputLayoutEndDateHours
        )

        // ---------- Установка ошибок ----------
        setError(binding.textInputLayoutCustomer, errors["customerName"])
        setError(binding.textInputLayoutContract, errors["contractCustomer"])
        setError(binding.textInputLayoutExecutor, errors["executorName"])
        setError(binding.textInputLayoutContractTransport, errors["contractTransport"])

        setError(binding.textInputLayoutStartDate, errors["startDate"])
        setError(binding.textInputLayoutStartDateHours, errors["startTime"])
        setError(binding.textInputLayoutStateNumber, errors["stateNumber"])
        setError(binding.textInputLayoutEndDate, errors["endDate"])
        setError(binding.textInputLayoutEndDateHours, errors["endTime"])

        return errors.isEmpty()
    }

    private fun clearErrors(vararg layouts: TextInputLayout) {
        layouts.forEach {
            it.isErrorEnabled = false
            it.error = null
        }
    }

    private fun setError(layout: TextInputLayout, errorMessage: String?) {
        layout.isErrorEnabled = !errorMessage.isNullOrBlank()
        layout.error = errorMessage
    }

    private fun isValidTimeFormat(time: String): Boolean {
        return time.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d\$"))
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.textInputEditTextCustomer.isEnabled = enabled
        binding.textInputEditTextContract.isEnabled = enabled
        binding.textInputEditTextExecutor.isEnabled = enabled
        binding.textInputEditTextContractTransport.isEnabled = enabled
        binding.textInputEditTextStateNumber.isEnabled = enabled
        binding.textInputEditTextStartDateHours.isEnabled = enabled
        binding.textInputEditTextEndDateHours.isEnabled = enabled
        binding.textInputEditTextStartDate.isEnabled = enabled
        binding.textInputEditTextEndDate.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
