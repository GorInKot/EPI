package com.example.epi.Fragments.Transport

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentTransportBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TransportFragment : Fragment() {

    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransportViewModel

    private var isFromatting = false

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
            // Если транспорт отсутствует — просто переходим дальше без валидации
            if (viewModel.isTransportAbsent.value == true) {
                findNavController().navigate(R.id.controlFragment)
                return@setOnClickListener
            }
            // Валидация полей (если транспорт есть)
            val validationError = viewModel.validateInputs()
            if (validationError != null) {
                Snackbar.make(binding.root, validationError, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.RED)
                    .setTextColor(Color.WHITE)
                    .show()
                return@setOnClickListener
            }

            // Проверка даты и времени
            val startDate = binding.textInputEditTextStartDate.text?.toString()
            val endDate = binding.textInputEditTextEndDate.text?.toString()
            val startTime = binding.textInputEditTextStartDateHours.text?.toString()
            val endTime = binding.textInputEditTextEndDateHours.text?.toString()

            if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()
                && !startTime.isNullOrBlank() && !endTime.isNullOrBlank()
                ) {
                try {
                    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    val start = format.parse("$startDate $startTime")
                    val end = format.parse("$endDate $endTime")

                    if (start != null && end != null && start.after(end)) {
                        Snackbar.make(binding.root, "Время окончания не может быть раньше времени начала", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(Color.RED)
                            .setTextColor(Color.WHITE)
                            .show()
                        return@setOnClickListener
                    }
                } catch (e:Exception) {
                    Snackbar.make(binding.root, "Ошибка формата дата/время", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.RED)
                        .setTextColor(Color.WHITE)
                        .show()
                    return@setOnClickListener
                }
                // Если всё ок — переходим дальше
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
                if(formatted.length == 10) {
                    if(!isValidDate(formatted.toString())) {
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
                validateStartAndEndTime()
            }
        }
    }


    private fun isValidTimeFormat(time: String): Boolean {
        return time.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d\$"))
    }

    private fun validateStartAndEndTime() {
        val startDate = binding.textInputEditTextStartDate.text?.toString()
        val endDate = binding.textInputEditTextEndDate.text?.toString()
        val startTime = binding.textInputEditTextStartDateHours.text?.toString()
        val endTime = binding.textInputEditTextEndDateHours.text?.toString()

        if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()
            && !startTime.isNullOrBlank() && !endTime.isNullOrBlank()
        ) {
            try {
                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val start = format.parse("$startDate $startTime")
                val end = format.parse("$endDate $endTime")

                if (start != null && end != null && start.after(end)) {
                    binding.textInputLayoutEndDateHours.error = "Время окончания не может быть раньше начала"
                } else {
                    binding.textInputLayoutEndDateHours.error = null
                }
            } catch (e: Exception) {
                // ignore, формат неправильный
            }
        }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
