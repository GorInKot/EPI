package com.example.epi.Fragments.Transport

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.DataBase.ExtraDatabase.ExtraDatabaseHelper
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentTransportBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.contracts.contract

class TransportFragment : Fragment() {
    private var _binding: FragmentTransportBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository
        )
    }

    companion object {
        private const val TAG = "Tagg-TransportFragment"
    }

    private lateinit var contractCustomerTextWatcher: TextWatcher
    private lateinit var executorNameTextWatcher: TextWatcher
    private lateinit var contractTransportTextWatcher: TextWatcher
    private lateinit var stateNumberTextWatcher: TextWatcher
    private lateinit var startDateTextWatcher: TextWatcher
    private lateinit var startTimeTextWatcher: TextWatcher
    private lateinit var endDateTextWatcher: TextWatcher
    private lateinit var endTimeTextWatcher: TextWatcher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
        loadTransportData()
    }

    private fun setupObservers() {
        sharedViewModel.isTransportAbsent.observe(viewLifecycleOwner) { isChecked ->
            binding.chBoxMCustomer.isChecked = isChecked
            setFieldsEnabled(!isChecked)
        }
        sharedViewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar
                .make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }
    }

    private fun setupInputListeners() {
        // Чекбокс
        binding.chBoxMCustomer.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setTransportAbsent(isChecked)
            if (isChecked) sharedViewModel.clearTransport()
        }

        // Дата начала и окончания поездки
        setupDateInput(binding.textInputEditTextStartDate)
        setupDateInput(binding.textInputEditTextEndDate)

        // Время начала и окончания
        setupTimeInput(binding.textInputEditTextStartDateHours, binding.textInputLayoutStartDateHours)
        setupTimeInput(binding.textInputEditTextEndDateHours, binding.textInputLayoutEndDateHours)

        // Госномер
        setupStateNumberInput(binding.textInputEditTextStateNumber, binding.textInputLayoutStateNumber)

        // Прочие текстовые поля
        contractCustomerTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportContractCustomer(s.toString())
            }
        }

        executorNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportExecutorName(s.toString())
            }
        }
        binding.autoCompleteExecutor.doAfterTextChanged { sharedViewModel.setTransportExecutorName(it.toString()) }

        contractTransportTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportContractTransport(s.toString())
            }
        }
        binding.autoCompleteContractTransport.doAfterTextChanged { sharedViewModel.setTransportContractTransport(it.toString()) }

        stateNumberTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportStateNumber(s.toString())
            }
        }
        binding.textInputEditTextStateNumber.doAfterTextChanged { sharedViewModel.setTransportStateNumber(it.toString()) }

        startDateTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportStartDate(s.toString())
            }
        }
        binding.textInputEditTextStartDate.doAfterTextChanged { sharedViewModel.setTransportStartDate(it.toString()) }

        startTimeTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportStartTime(s.toString())
            }
        }
        binding.textInputEditTextStartDateHours.doAfterTextChanged { sharedViewModel.setTransportStartTime(it.toString()) }

        endDateTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportEndDate(s.toString())
            }
        }
        binding.textInputEditTextEndDate.doAfterTextChanged { sharedViewModel.setTransportEndDate(it.toString()) }

        endTimeTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setTransportEndTime(s.toString())
            }
        }
        binding.textInputEditTextEndDateHours.doAfterTextChanged { sharedViewModel.setTransportEndTime(it.toString()) }
    }

    private fun setupButtons() {
        // Далее
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                sharedViewModel.viewModelScope.launch {
                    try {
                        val reportId = sharedViewModel.updateTransportReport()
                        if (reportId > 0) {
                            val action = TransportFragmentDirections.actionTransportFragmentToControlFragment()
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Ошибка сохранения отчета: неверный ID",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(TAG, "Ошибка: reportId = $reportId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при сохранении отчета: ${e.message}")
                        Toast.makeText(
                            requireContext(),
                            "Ошибка сохранения отчета: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Log.d(TAG, "Валидация не прошла")
            }
        }

        // Назад
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_transportFragment_to_arrangementFragment)
        }

        // Выбор даты начала поездки
        binding.btnStartDate.setOnClickListener {
            showDatePickerDialog(binding.textInputEditTextStartDate) { date ->
                sharedViewModel.setTransportStartDate(date)
                binding.textInputEditTextStartDate.setText(date)
            }
        }

        // Выбор времени начала поездки
        binding.btnStartTime.setOnClickListener {
            Log.d(TAG, "Кнопка выбора времени начала поездки нажата")
            showMaterialTimePicker(binding.textInputEditTextStartDateHours) { time ->
                sharedViewModel.setTransportStartTime(time)
                binding.textInputEditTextStartDateHours.setText(time)
            }
        }

        // Выбор даты завершения поездки
        binding.btnEndDate.setOnClickListener {
            showDatePickerDialog(binding.textInputEditTextEndDate) { date ->
                sharedViewModel.setTransportEndDate(date)
                binding.textInputEditTextEndDate.setText(date)
            }
        }

        // Выбор времени завершения поездки
        binding.btnEndTime.setOnClickListener {
            Log.d(TAG, "Кнопка выбора времени завершения поездки нажата")
            showMaterialTimePicker(binding.textInputEditTextEndDateHours) { time ->
                sharedViewModel.setTransportEndTime(time)
                binding.textInputEditTextEndDateHours.setText(time)
            }
        }
    }

    private fun showDatePickerDialog(editText: AppCompatEditText, onDateSelected: (date: String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentText = editText.text.toString()
        if (currentText.isNotBlank() && isValidDate(currentText)) {
            try {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val date = sdf.parse(currentText)
                date?.let { calendar.time = it }
            } catch (e: Exception) {
                // Если не парсится, берём текущую дату
            }
        }
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.CustomPickerTheme)
            .setTitleText("Выберите дату")
            .setSelection(calendar.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Calendar.getInstance().apply { timeInMillis = selection }
            val formattedDate = String.format(
                Locale.getDefault(),
                "%02d.%02d.%04d",
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.YEAR)
            )
            onDateSelected(formattedDate)
        }
        datePicker.show(parentFragmentManager, "MaterialDatePicker")
    }

    private fun showMaterialTimePicker(editText: AppCompatEditText, onTimeSelected: (time: String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentText = editText.text.toString()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)
        if (currentText.isNotBlank() && isValidTimeFormat(currentText)) {
            try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val time = sdf.parse(currentText)
                time?.let {
                    calendar.time = it
                    hour = calendar.get(Calendar.HOUR_OF_DAY)
                    minute = calendar.get(Calendar.MINUTE)
                }
            } catch (e: Exception) {
                // Если время не парсится, используем текущее
                Log.e(TAG, "TimePickerError: Ошибка парсинга времени: ${e.message}")
            }
        }
        Log.d("TimePicker", "Creating TimePicker with hour=$hour, minute=$minute")
        val timePicker = MaterialTimePicker.Builder()
            .setTheme(R.style.CustomTimePicker)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Выберите время")
            .build()
        timePicker.addOnPositiveButtonClickListener {
            val formattedTime = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                timePicker.hour,
                timePicker.minute
            )
            onTimeSelected(formattedTime)
            Log.d(TAG,"TimePicker: Selected time: $formattedTime")
        }
        try {
            timePicker.show(parentFragmentManager, "CustomPickerTheme")
            Log.d(TAG, "TimePicker shown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отображении TimePicker: ${e.message}")
        }
    }

    private fun setupDateInput(editText: AppCompatEditText) {
        var isFormatting = false
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                val clean = s?.toString()?.replace("[^\\d]".toRegex(), "") ?: return
                val formatted = StringBuilder()
                var cursorPosition = 0
                if (clean.length > 8) return // Ограничиваем максимум до ддММгггг
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
                // Проверка валидности даты
                if (formatted.length == 10) {
                    if (!isValidDate(formatted.toString())) {
                        val parent = editText.parent?.parent
                        if (parent is TextInputLayout) {
                            parent.error = "Неверная дата"
                        }
                    } else {
                        val parent = editText.parent?.parent
                        if (parent is TextInputLayout) {
                            parent.error = null
                        }
                    }
                } else {
                    // Если дата неполная
                    val parent = editText.parent?.parent
                    if (parent is TextInputLayout) {
                        parent.error = null
                    }
                }
            }
        })
    }

    private fun setupTimeInput(editText: AppCompatEditText, inputLayout: TextInputLayout) {
        var previous = "" // Чтобы избежать бесконечного цикла обновлений
        editText.doAfterTextChanged {
            val input = it?.toString() ?: return@doAfterTextChanged
            val clean = input.replace("[^\\d]".toRegex(), "").trim()
            // Не реагируем на обновление, если значение не изменилось
            if (clean == previous.replace(":", "")) {
                return@doAfterTextChanged
            }
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
                inputLayout.error = "Неверный формат: чч:мм (00-23)"
            } else {
                inputLayout.error = null
            }
        }
    }

    private fun setupStateNumberInput(editText: AppCompatEditText, inputLayout: TextInputLayout) {
        var previous = "" // Чтобы избежать бесконечного цикла обновлений
        editText.doAfterTextChanged {
            val input = it?.toString()?.trim()?.uppercase(Locale.getDefault()) ?: return@doAfterTextChanged
            val clean = input.replace("[^АВЕКМНОРСТУХ\\d]".toRegex(), "")
            // Не реагируем на обновление, если значение не изменилось
            if (clean == previous.replace(" ", "")) {
                return@doAfterTextChanged
            }
            val formatted = StringBuilder()
            var cursorPosition = 0
            when {
                clean.length <= 1 -> {
                    formatted.append(clean)
                    cursorPosition = clean.length
                }
                clean.length <= 4 -> {
                    formatted.append(clean.substring(0, 1)).append(" ").append(clean.substring(1))
                    cursorPosition = clean.length + 1
                }
                clean.length <= 6 -> {
                    formatted.append(clean.substring(0, 1)).append(" ")
                        .append(clean.substring(1, 4)).append(" ")
                        .append(clean.substring(4))
                    cursorPosition = clean.length + 2
                }
                else -> {
                    formatted.append(clean.substring(0, 1)).append(" ")
                        .append(clean.substring(1, 4)).append(" ")
                        .append(clean.substring(4, 6)).append(" ")
                        .append(clean.substring(6))
                    cursorPosition = clean.length + 3
                }
            }
            previous = formatted.toString()
            // Устанавливаем текст только если отличается
            if (input != formatted.toString()) {
                editText.setText(formatted.toString())
                editText.setSelection(cursorPosition.coerceAtMost(formatted.length))
            }
            // Проверка формата и ошибки
            if (!sharedViewModel.isValidStateNumber(formatted.toString())) {
                inputLayout.error = "Неверный формат: А123БВ45 или А123БВ456"
            } else {
                inputLayout.error = null
            }
        }
    }

    private fun restoreInputs() {
        // Устанавливаем значения из SharedViewModel
        binding.autoCompleteExecutor.setText(sharedViewModel.transportExecutorName.value)
        binding.autoCompleteContractTransport.setText(sharedViewModel.transportContractTransport.value)
        binding.textInputEditTextStateNumber.setText(sharedViewModel.transportStateNumber.value)
        binding.textInputEditTextStartDate.setText(sharedViewModel.transportStartDate.value)
        binding.textInputEditTextStartDateHours.setText(sharedViewModel.transportStartTime.value)
        binding.textInputEditTextEndDate.setText(sharedViewModel.transportEndDate.value)
        binding.textInputEditTextEndDateHours.setText(sharedViewModel.transportEndTime.value)
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidTimeFormat(time: String): Boolean {
        return time.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))
    }

    private fun validateInputs(): Boolean {
        val isTransportAbsent = binding.chBoxMCustomer.isChecked
        val executor = binding.autoCompleteExecutor.text?.toString()?.trim()
        val contractTransport = binding.autoCompleteContractTransport.text?.toString()?.trim()
        val dateStart = binding.textInputEditTextStartDate.text?.toString()?.trim()
        val timeStart = binding.textInputEditTextStartDateHours.text?.toString()?.trim()
        val number = binding.textInputEditTextStateNumber.text?.toString()?.trim()
        val dateEnd = binding.textInputEditTextEndDate.text?.toString()?.trim()
        val endTime = binding.textInputEditTextEndDateHours.text?.toString()?.trim()

        val errors = sharedViewModel.validateTransportInputs(
            isTransportAbsent = isTransportAbsent,
            executorName = executor,
            contractTransport = contractTransport,
            stateNumber = number,
            startDate = dateStart,
            startTime = timeStart,
            endDate = dateEnd,
            endTime = endTime,
        )

        // Показать Snackbar при наличии ошибок
        if (errors.isNotEmpty()) {
            Snackbar
                .make(binding.root, "Не все поля заполнены или содержат ошибки", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }

        // Очистка ошибок
        clearErrors(
            binding.textInputLayoutExecutor,
            binding.textInputLayoutContractTransport,
            binding.textInputLayoutStartDate,
            binding.textInputLayoutStartDateHours,
            binding.textInputLayoutStateNumber,
            binding.textInputLayoutEndDate,
            binding.textInputLayoutEndDateHours
        )

        // Установка ошибок
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
        layouts.forEach { it.isErrorEnabled = false; it.error = null }
    }

    private fun setError(layout: TextInputLayout, errorMessage: String?) {
        layout.isErrorEnabled = !errorMessage.isNullOrBlank()
        layout.error = errorMessage
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.autoCompleteExecutor.isEnabled = enabled
        binding.autoCompleteContractTransport.isEnabled = enabled
        binding.textInputEditTextStateNumber.isEnabled = enabled
        binding.btnStartDate.isEnabled = enabled
        binding.btnStartTime.isEnabled = enabled
        binding.btnEndDate.isEnabled = enabled
        binding.btnEndTime.isEnabled = enabled
        binding.textInputEditTextStartDate.isEnabled = false
        binding.textInputEditTextEndDate.isEnabled = false
        binding.textInputEditTextStartDateHours.isEnabled = false
        binding.textInputEditTextEndDateHours.isEnabled = false
    }

    private fun loadTransportData() {
        sharedViewModel.selectedContract.observe(viewLifecycleOwner) { contract ->
            Log.d("Tagg-Transport", "Selected contract: $contract")
            if (contract != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val dbHelper = ExtraDatabaseHelper(requireContext())
                    val contractId = dbHelper.getContractIdByName(contract) ?: contract // Fallback if mapping fails
                    Log.d("Tagg-Transport", "Mapped contract ID: $contractId")
                    val executors = withContext(Dispatchers.IO) {
                        dbHelper.getTransportContractExecutorsByContract(contractId)
                    }
                    val names = withContext(Dispatchers.IO) {
                        dbHelper.getTransportContractNamesByContract(contractId)
                    }
                    Log.d("Tagg-Transport", "Executors loaded: $executors")
                    Log.d("Tagg-Transport", "Names loaded: $names")

                    if (executors.isEmpty()) {
                        Toast.makeText(requireContext(), "Список исполнителей пуст", Toast.LENGTH_SHORT).show()
                        Log.d("Tagg-Transport", "Список исполнителей пуст для контракта: $contract")
                    }
                    val executorAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        executors
                    )
                    binding.autoCompleteExecutor.setAdapter(executorAdapter)
                    binding.autoCompleteExecutor.inputType = InputType.TYPE_NULL
                    binding.autoCompleteExecutor.keyListener = null
                    binding.autoCompleteExecutor.setOnTouchListener { _, _ ->
                        if (binding.autoCompleteExecutor.isEnabled) {
                            binding.autoCompleteExecutor.showDropDown()
                        }
                        false
                    }
                    binding.autoCompleteExecutor.setOnItemClickListener { parent, _, position, _ ->
                        val selectedExecutor = parent.getItemAtPosition(position).toString()
                        sharedViewModel.setTransportExecutorName(selectedExecutor)
                        Log.d("Tagg-Transport", "Selected executor: $selectedExecutor")
                    }

                    if (names.isEmpty()) {
                        Toast.makeText(requireContext(), "Список контрактов пуст", Toast.LENGTH_SHORT).show()
                        Log.d("Tagg-Transport", "Список контрактов пуст для контракта: $contract")
                    }
                    val nameAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                    )
                    binding.autoCompleteContractTransport.setAdapter(nameAdapter)
                    binding.autoCompleteContractTransport.inputType = InputType.TYPE_NULL
                    binding.autoCompleteContractTransport.keyListener = null
                    binding.autoCompleteContractTransport.setOnTouchListener { _, _ ->
                        if (binding.autoCompleteContractTransport.isEnabled) {
                            binding.autoCompleteContractTransport.showDropDown()
                        }
                        false
                    }
                    binding.autoCompleteContractTransport.setOnItemClickListener { parent, _, position, _ ->
                        val selectedName = parent.getItemAtPosition(position).toString()
                        sharedViewModel.setTransportContractTransport(selectedName)
                        Log.d("Tagg-Transport", "Selected contract transport: $selectedName")
                    }
                }
            } else {
                Log.d("Tagg-Transport", "Нет выбранного контракта")
                binding.autoCompleteExecutor.setAdapter(null)
                binding.autoCompleteContractTransport.setAdapter(null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.autoCompleteExecutor.removeTextChangedListener(executorNameTextWatcher)
        binding.autoCompleteContractTransport.removeTextChangedListener(contractTransportTextWatcher)
        binding.textInputEditTextStateNumber.removeTextChangedListener(stateNumberTextWatcher)
        binding.textInputEditTextStartDate.removeTextChangedListener(startDateTextWatcher)
        binding.textInputEditTextStartDateHours.removeTextChangedListener(startTimeTextWatcher)
        binding.textInputEditTextEndDate.removeTextChangedListener(endDateTextWatcher)
        binding.textInputEditTextEndDateHours.removeTextChangedListener(endTimeTextWatcher)
        _binding = null
    }
}