package com.example.epi.Fragments.Arrangement

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.R
import com.example.epi.databinding.FragmentArrangmentBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ArrangementFragment : Fragment() {

    private var _binding: FragmentArrangmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArrangementViewModel by viewModels {
        ArrangementViewModelFactory((requireActivity().application as App).reportRepository)
    }

    private lateinit var plotTextWatcher: TextWatcher
    private lateinit var repSSKGpTextWatcher: TextWatcher
    private lateinit var subContractorTextWatcher: TextWatcher
    private lateinit var repSubContractorTextWatcher: TextWatcher
    private lateinit var repSSKSubTextWatcher: TextWatcher

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArrangmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDateTime()
        setupLeftBlock()
        setupRightBlock()
        setupButtons()
        setupViewModelObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateDateTime()
    }

    private fun setupDateTime() {
        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.tvDate.text = "Дата: $it"
        }
        viewModel.currentTime.observe(viewLifecycleOwner) {
            binding.tvTime.text = "Время: $it"
        }
    }

    private fun setupLeftBlock() {
        // Режим работы
        val workTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.workTypes
        )
        binding.autoCompleteWorkType.setAdapter(workTypeAdapter)
        binding.autoCompleteWorkType.inputType = InputType.TYPE_NULL
        binding.autoCompleteWorkType.keyListener = null
        binding.autoCompleteWorkType.setOnTouchListener { _, _ ->
            binding.autoCompleteWorkType.showDropDown()
            false
        }

        binding.autoCompleteWorkType.setOnItemClickListener { parent, _, position, _ ->
            val selectedWorkType = parent.getItemAtPosition(position).toString()
            viewModel.selectedWorkType.value = selectedWorkType
        }

        // Заказчик
        val customerListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.customers
        )
        binding.autoCompleteCustomer.setAdapter(customerListAdapter)
        binding.autoCompleteCustomer.inputType = InputType.TYPE_NULL
        binding.autoCompleteCustomer.keyListener = null
        binding.autoCompleteCustomer.setOnTouchListener { _, _ ->
            binding.autoCompleteCustomer.showDropDown()
            false
        }
        binding.autoCompleteCustomer.setOnItemClickListener { parent, _, position, _ ->
            val selectedCustomer = parent.getItemAtPosition(position).toString()
            viewModel.selectedCustomer.value = selectedCustomer
        }

        // Заказчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualCustomer,
            binding.textInputLayoutAutoCustomer,
            binding.hiddenTextInputLayoutManualCustomer,
            binding.hiddenTextInputEditTextManualCustomer
        )

        // Заказчик (ручной ввод)
        binding.hiddenTextInputEditTextManualCustomer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.manualCustomer.value = s.toString()
            }
        })

        // Объект
        val objectListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.objects
        )
        binding.autoCompleteObject.setAdapter(objectListAdapter)
        binding.autoCompleteObject.inputType = InputType.TYPE_NULL
        binding.autoCompleteObject.keyListener = null
        binding.autoCompleteObject.setOnTouchListener { _, _ ->
            binding.autoCompleteObject.showDropDown()
            false
        }
        binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
            val selectedObject = parent.getItemAtPosition(position).toString()
            viewModel.selectedObject.value = selectedObject
        }

        // Объект: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualObject,
            binding.textInputLayoutAutoObject,
            binding.hiddenTextInputLayoutManualObject,
            binding.hiddenTextInputEditTextManualObject
        )

        // Объект (ручной ввод)
        binding.hiddenTextInputEditTextManualObject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.manualObject.value = s.toString()
            }
        })

        // Участок
        plotTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onPlotChanged(s.toString())
            }
        }
        binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)

        // Генподрядчик
        val contractorListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.contractors
        )
        binding.autoCompleteContractor.setAdapter(contractorListAdapter)
        binding.autoCompleteContractor.inputType = InputType.TYPE_NULL
        binding.autoCompleteContractor.keyListener = null
        binding.autoCompleteContractor.setOnTouchListener { _, _ ->
            binding.autoCompleteContractor.showDropDown()
            false
        }
        binding.autoCompleteContractor.setOnItemClickListener { parent, _, position, _ ->
            val selectedContractor = parent.getItemAtPosition(position).toString()
            viewModel.selectedContractor.value = selectedContractor
        }

        // Генподрядчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualContractor,
            binding.textInputLayoutAutoContractor,
            binding.hiddenTextInputLayoutManualContractor,
            binding.hiddenTextInputEditTextManualContractor
        )

        // Генподрядчик (ручной ввод)
        binding.hiddenTextInputEditTextManualContractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.manualContractor.value = s.toString()
            }
        })
    }

    private fun setupRightBlock() {
        // Представитель Генподрядчика
        val subContractorListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.subContractors
        )
        binding.autoCompleteSubContractor.setAdapter(subContractorListAdapter)
        binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
        binding.autoCompleteSubContractor.keyListener = null
        binding.autoCompleteSubContractor.setOnTouchListener { _, _ ->
            binding.autoCompleteSubContractor.showDropDown()
            false
        }
        binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
            val selectedSubContractor = parent.getItemAtPosition(position).toString()
            viewModel.selectedSubContractor.value = selectedSubContractor
        }

        // Представитель Генподрядчика: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualSubContractor,
            binding.textInputLayoutAutoSubContractor,
            binding.hiddenTextInputLayoutManualSubContractor,
            binding.hiddenTextInputEditTextManualSubContractor
        )

        // Представитель ССК ПО (ГП)
        repSSKGpTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSSKGpChanged(s.toString())
            }
        }
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)

        // Субподрядчик
        subContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSubContractorChanged(s.toString())
            }
        }
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)

        // Представитель Генподрядчика (ручной ввод)
        binding.hiddenTextInputEditTextManualSubContractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.manualSubContractor.value = s.toString()
            }
        })

        // Представитель субподрядчика
        repSubContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSubcontractorChanged(s.toString())
            }
        }
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)

        // Представитель ССК ПО (Суб)
        repSSKSubTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSSKSubChanged(s.toString())
            }
        }
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
    }

    private fun setupButtons() {
        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                Log.d("Tagg", "Валидация прошла")
                // Логирование значений
                Log.d("Tagg", "WorkType: ${viewModel.selectedWorkType.value}")
                Log.d("Tagg", "Customer: ${viewModel.selectedCustomer.value}, ManualCustomer: ${viewModel.manualCustomer.value}")
                Log.d("Tagg", "Object: ${viewModel.selectedObject.value}, ManualObject: ${viewModel.manualObject.value}")
                Log.d("Tagg", "Plot: ${viewModel.plotText.value}")
                Log.d("Tagg", "Contractor: ${viewModel.selectedContractor.value}, ManualContractor: ${viewModel.manualContractor.value}")
                Log.d("Tagg", "SubContractor: ${viewModel.selectedSubContractor.value}, ManualSubContractor: ${viewModel.manualSubContractor.value}")
                Log.d("Tagg", "RepSSKGp: ${viewModel.repSSKGpText.value}")
                Log.d("Tagg", "SubContractorText: ${viewModel.subContractorText.value}")
                Log.d("Tagg", "RepSubContractor: ${viewModel.repSubcontractorText.value}")
                Log.d("Tagg", "RepSSKSub: ${viewModel.repSSKSubText.value}")

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val reportId = withContext(Dispatchers.IO) { viewModel.saveOrUpdateReport() }
                        Log.d("Tagg", "Report ID: $reportId")

                        if (reportId > 0) {
                            Toast.makeText(requireContext(), "Отчет сохранен", Toast.LENGTH_SHORT).show()

                            // Получаем objectID (из выпадающего списка или вручную)
                            val objectID = viewModel.selectedObject.value ?: viewModel.manualObject.value
                            if (objectID.isNullOrBlank()) {
                                Toast.makeText(requireContext(), "Объект не задан", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val customerID = viewModel.selectedCustomer.value ?: viewModel.manualCustomer.value
                            if (customerID.isNullOrBlank()) {
                                Toast.makeText(requireContext(), "Заказчик не задан", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            // Переход с передачей reportId и objectID
                            val action = ArrangementFragmentDirections
                                .actionArrangementFragmentToTransportFragment(
                                    reportId = reportId,
                                    objectId = objectID,
                                    customer = customerID
                                )
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(requireContext(), "Ошибка сохранения отчета", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("Tagg", "Ошибка при сохранении отчета: ${e.message}")
                        Toast.makeText(requireContext(), "Ошибка сохранения отчета", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("Tagg", "Валидация НЕ прошла")
            }
        }


        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_arrangementFragment_to_startFragment)
        }

        // Кнопка "Копия предыдущего отчета"
        binding.btnCopy.setOnClickListener {
            viewModel.loadPreviousReport()
            Toast.makeText(requireContext(), "Загружен предыдущий отчет", Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Очистить"
        binding.btnClear.setOnClickListener {
            viewModel.arrangementIsClearing.value = true

            viewModel.clearAll()

            removeAllTextWatchers()
            clearUiFields()
            addAllTextWatchers()
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.arrangementIsClearing.value = false
            }, 100)

            Toast.makeText(requireContext(), "Все поля очищены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        val workTypes = binding.autoCompleteWorkType.text?.toString()?.trim()

        val customers = binding.autoCompleteCustomer.text?.toString()?.trim()
        val manualCustomer = binding.hiddenTextInputEditTextManualCustomer.text?.toString()?.trim()

        val objects = binding.autoCompleteObject.text?.toString()?.trim()
        val manualObject = binding.hiddenTextInputEditTextManualObject.text?.toString()?.trim()

        val plotText = binding.textInputEditTextPlot.text?.toString()?.trim()

        val contractors = binding.autoCompleteContractor.text?.toString()?.trim()
        val manualContractor = binding.hiddenTextInputEditTextManualContractor.text?.toString()?.trim()

        val subContractors = binding.autoCompleteSubContractor.text?.toString()?.trim()
        val manualSubContractor = binding.hiddenTextInputEditTextManualSubContractor.text?.toString()?.trim()

        val repSSKGpText = binding.textInputEditTextRepSSKGp.text?.toString()?.trim()
        val subContractorText = binding.textInputEditTextSubcontractor.text?.toString()?.trim()
        val repSubcontractorText = binding.textInputEditTextRepSubContractor.text?.toString()?.trim()
        val repSSKSubText = binding.textInputEditTextRepSSKSub.text?.toString()?.trim()

        val errors = viewModel.validateArrangementInputs(
            workTypes,
            customers, manualCustomer,
            objects, manualObject,
            plotText,
            contractors, manualContractor,
            subContractors, manualSubContractor,
            repSSKGpText, subContractorText,
            repSubcontractorText, repSSKSubText
        )

        if (errors.isNotEmpty()) {
            Snackbar
                .make(binding.root, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }

        clearErrors(
            binding.textInputLayoutAutoWorkType,
            binding.textInputLayoutAutoCustomer,
            binding.hiddenTextInputLayoutManualCustomer,
            binding.textInputLayoutAutoObject,
            binding.hiddenTextInputLayoutManualObject,
            binding.textInputLayoutPlot,
            binding.textInputLayoutAutoContractor,
            binding.hiddenTextInputLayoutManualContractor,
            binding.textInputLayoutAutoSubContractor,
            binding.hiddenTextInputLayoutManualSubContractor,
            binding.textInputLayoutRepSSKGp,
            binding.textInputLayoutSubContractor,
            binding.textInputLayoutRepSubContractor,
            binding.textInputLayoutRepSSKSub
        )

        setError(binding.textInputLayoutAutoWorkType, errors["workTypes"])

        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteCustomer.isShown,
            autoLayout = binding.textInputLayoutAutoCustomer,
            manualLayout = binding.hiddenTextInputLayoutManualCustomer,
            errorMessage = errors["customers"]
        )

        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteObject.isShown,
            autoLayout = binding.textInputLayoutAutoObject,
            manualLayout = binding.hiddenTextInputLayoutManualObject,
            errorMessage = errors["objects"]
        )

        setError(binding.textInputLayoutPlot, errors["plotText"])

        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteContractor.isShown,
            autoLayout = binding.textInputLayoutAutoContractor,
            manualLayout = binding.hiddenTextInputLayoutManualContractor,
            errorMessage = errors["contractors"]
        )

        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteSubContractor.isShown,
            autoLayout = binding.textInputLayoutAutoSubContractor,
            manualLayout = binding.hiddenTextInputLayoutManualSubContractor,
            errorMessage = errors["subContractors"]
        )

        setError(binding.textInputLayoutRepSSKGp, errors["repSSKGpText"])
        setError(binding.textInputLayoutSubContractor, errors["subContractorText"])

        setConditionalDualError(
            autoFieldVisible = binding.textInputEditTextRepSubContractor.isShown,
            autoLayout = binding.textInputLayoutRepSubContractor,
            manualLayout = binding.hiddenTextInputLayoutManualSubContractor,
            errorMessage = errors["repSubcontractorText"]
        )

        setError(binding.textInputLayoutRepSSKSub, errors["repSSKSubText"])

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

    private fun setConditionalDualError(
        autoFieldVisible: Boolean,
        autoLayout: TextInputLayout,
        manualLayout: TextInputLayout,
        errorMessage: String?
    ) {
        if (autoFieldVisible) {
            setError(autoLayout, errorMessage)
        } else {
            setError(manualLayout, errorMessage)
        }
    }

    private fun removeAllTextWatchers() {
        binding.textInputEditTextPlot.removeTextChangedListener(plotTextWatcher)
        binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
        binding.textInputEditTextSubcontractor.removeTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)
    }

    private fun clearUiFields() {
        binding.textInputEditTextPlot.setText("")
        binding.textInputEditTextRepSSKGp.setText("")
        binding.textInputEditTextSubcontractor.setText("")
        binding.textInputEditTextRepSubContractor.setText("")
        binding.textInputEditTextRepSSKSub.setText("")

        binding.autoCompleteWorkType.setText("")
        binding.autoCompleteCustomer.setText("")
        binding.autoCompleteObject.setText("")
        binding.autoCompleteContractor.setText("")
        binding.autoCompleteSubContractor.setText("")

        binding.checkBoxManualCustomer.isChecked = false
        binding.checkBoxManualObject.isChecked = false
        binding.checkBoxManualContractor.isChecked = false
        binding.checkBoxManualSubContractor.isChecked = false
    }

    private fun addAllTextWatchers() {
        binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
    }

    private fun setupViewModelObservers() {
        viewModel.plotText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextPlot.text.toString() != text) {
                binding.textInputEditTextPlot.removeTextChangedListener(plotTextWatcher)
                binding.textInputEditTextPlot.setText(text)
                binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)
            }
        }

        viewModel.repSSKGpText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSSKGp.text.toString() != text) {
                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
                binding.textInputEditTextRepSSKGp.setText(text)
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
            }
        }

        viewModel.subContractorText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextSubcontractor.text.toString() != text) {
                binding.textInputEditTextSubcontractor.removeTextChangedListener(subContractorTextWatcher)
                binding.textInputEditTextSubcontractor.setText(text)
                binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
            }
        }

        viewModel.repSubcontractorText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSubContractor.text.toString() != text) {
                binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
                binding.textInputEditTextRepSubContractor.setText(text)
                binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
            }
        }

        viewModel.repSSKSubText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSSKSub.text.toString() != text) {
                binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)
                binding.textInputEditTextRepSSKSub.setText(text)
                binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
            }
        }

        viewModel.selectedWorkType.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteWorkType.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteWorkType.setText(it ?: "", false)
            }
        }

        viewModel.selectedCustomer.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteCustomer.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteCustomer.setText(it ?: "", false)
            }
        }

        viewModel.selectedObject.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteObject.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteObject.setText(it ?: "", false)
            }
        }

        viewModel.selectedContractor.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteContractor.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteContractor.setText(it ?: "", false)
            }
        }

        viewModel.selectedSubContractor.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteSubContractor.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteSubContractor.setText(it ?: "", false)
            }
        }

        viewModel.isManualCustomer.observe(viewLifecycleOwner) {
            binding.checkBoxManualCustomer.isChecked = it
        }
        viewModel.isManualObject.observe(viewLifecycleOwner) {
            binding.checkBoxManualObject.isChecked = it
        }
        viewModel.isManualContractor.observe(viewLifecycleOwner) {
            binding.checkBoxManualContractor.isChecked = it
        }
        viewModel.isManualSubContractor.observe(viewLifecycleOwner) {
            binding.checkBoxManualSubContractor.isChecked = it
        }

        viewModel.manualCustomer.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualCustomer.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualCustomer.setText(newValue)
            }
        }
        viewModel.manualObject.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualObject.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualObject.setText(newValue)
            }
        }
        viewModel.manualContractor.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualContractor.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualContractor.setText(newValue)
            }
        }
        viewModel.manualSubContractor.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualSubContractor.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualSubContractor.setText(newValue)
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar
                .make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }
    }

    private fun setupToggleCheckbox(
        checkbox: CheckBox,
        autoInputLayout: View,
        manualInputLayout: View,
        manualEditText: EditText
    ) {
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                autoInputLayout.visibility = View.GONE
                manualInputLayout.visibility = View.VISIBLE
                manualEditText.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(manualEditText, InputMethodManager.SHOW_IMPLICIT)
            } else {
                autoInputLayout.visibility = View.VISIBLE
                manualInputLayout.visibility = View.GONE
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(manualEditText.windowToken, 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeAllTextWatchers()
        _binding = null
    }
}