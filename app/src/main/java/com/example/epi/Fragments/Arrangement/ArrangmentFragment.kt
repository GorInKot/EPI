package com.example.epi.Fragments.Arrangement

import android.content.Context
import android.os.Bundle
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
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
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory((requireActivity().application as App).reportRepository)
    }

    private lateinit var plotTextWatcher: TextWatcher
    private lateinit var repSSKGpTextWatcher: TextWatcher
    private lateinit var subContractorTextWatcher: TextWatcher
    private lateinit var repSubContractorTextWatcher: TextWatcher
    private lateinit var repSSKSubTextWatcher: TextWatcher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
        sharedViewModel.updateDateTime()
    }

    private fun setupDateTime() {
        sharedViewModel.currentDate.observe(viewLifecycleOwner) { date ->
            binding.tvDate.text = "Дата: $date"
        }
        sharedViewModel.currentTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = "Время: $time"
        }
    }

    private fun setupLeftBlock() {
        // Режим работы
        val workTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            SharedViewModel.workTypes
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
            sharedViewModel.setSelectedWorkType(selectedWorkType)
        }

        // Заказчик
        val customerListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            SharedViewModel.customers
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
            sharedViewModel.setSelectedCustomer(selectedCustomer)
            sharedViewModel.setIsManualCustomer(false)
        }

        // Заказчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualCustomer,
            binding.textInputLayoutAutoCustomer,
            binding.hiddenTextInputLayoutManualCustomer,
            binding.hiddenTextInputEditTextManualCustomer
        )
        binding.hiddenTextInputEditTextManualCustomer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setManualCustomer(s.toString())
                sharedViewModel.setIsManualCustomer(true)
            }
        })

        // Объект
        val objectListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            SharedViewModel.objects
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
            sharedViewModel.setSelectedObject(selectedObject)
            sharedViewModel.setIsManualObject(false)
        }

        // Объект: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualObject,
            binding.textInputLayoutAutoObject,
            binding.hiddenTextInputLayoutManualObject,
            binding.hiddenTextInputEditTextManualObject
        )
        binding.hiddenTextInputEditTextManualObject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setManualObject(s.toString())
                sharedViewModel.setIsManualObject(true)
            }
        })

        // Участок
        plotTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setPlotText(s.toString())
            }
        }
        binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)

        // Генподрядчик
        val contractorListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            SharedViewModel.contractors
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
            sharedViewModel.setSelectedContractor(selectedContractor)
            sharedViewModel.setIsManualContractor(false)
        }

        // Генподрядчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualContractor,
            binding.textInputLayoutAutoContractor,
            binding.hiddenTextInputLayoutManualContractor,
            binding.hiddenTextInputEditTextManualContractor
        )
        binding.hiddenTextInputEditTextManualContractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setManualContractor(s.toString())
                sharedViewModel.setIsManualContractor(true)
            }
        })
    }

    private fun setupRightBlock() {
        // Представитель Генподрядчика
        val subContractorListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            SharedViewModel.subContractors
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
            sharedViewModel.setSelectedSubContractor(selectedSubContractor)
            sharedViewModel.setIsManualSubContractor(false)
        }

        // Представитель Генподрядчика: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualSubContractor,
            binding.textInputLayoutAutoSubContractor,
            binding.hiddenTextInputLayoutManualSubContractor,
            binding.hiddenTextInputEditTextManualSubContractor
        )
        binding.hiddenTextInputEditTextManualSubContractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setManualSubContractor(s.toString())
                sharedViewModel.setIsManualSubContractor(true)
            }
        })

        // Представитель ССК ПО (ГП)
        repSSKGpTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSSKGpText(s.toString())
            }
        }
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)

        // Субподрядчик
        subContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setSubContractorText(s.toString())
            }
        }
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)

        // Представитель субподрядчика
        repSubContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSubcontractorText(s.toString())
            }
        }
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)

        // Представитель ССК ПО (Суб)
        repSSKSubTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSSKSubText(s.toString())
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
                Log.d("Tagg", "WorkType: ${sharedViewModel.selectedWorkType.value}")
                Log.d("Tagg", "Customer: ${sharedViewModel.selectedCustomer.value}, ManualCustomer: ${sharedViewModel.manualCustomer.value}")
                Log.d("Tagg", "Object: ${sharedViewModel.selectedObject.value}, ManualObject: ${sharedViewModel.manualObject.value}")
                Log.d("Tagg", "Plot: ${sharedViewModel.plotText.value}")
                Log.d("Tagg", "Contractor: ${sharedViewModel.selectedContractor.value}, ManualContractor: ${sharedViewModel.manualContractor.value}")
                Log.d("Tagg", "SubContractor: ${sharedViewModel.selectedSubContractor.value}, ManualSubContractor: ${sharedViewModel.manualSubContractor.value}")
                Log.d("Tagg", "RepSSKGp: ${sharedViewModel.repSSKGpText.value}")
                Log.d("Tagg", "SubContractorText: ${sharedViewModel.subContractorText.value}")
                Log.d("Tagg", "RepSubContractor: ${sharedViewModel.repSubcontractorText.value}")
                Log.d("Tagg", "RepSSKSub: ${sharedViewModel.repSSKSubText.value}")

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val reportId = withContext(Dispatchers.IO) {
                            sharedViewModel.saveOrUpdateReport()
                        }
                        Log.d("Tagg", "Report ID: $reportId")
                        if (reportId > 0) {
                            Toast.makeText(requireContext(), "Отчет сохранен", Toast.LENGTH_SHORT).show()
                            // Получаем objectID (из выпадающего списка или вручную)
                            val objectID = sharedViewModel.selectedObject.value ?: sharedViewModel.manualObject.value
                            if (objectID.isNullOrBlank()) {
                                Toast.makeText(requireContext(), "Объект не задан", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val customerID = sharedViewModel.selectedCustomer.value ?: sharedViewModel.manualCustomer.value
                            if (customerID.isNullOrBlank()) {
                                Toast.makeText(requireContext(), "Заказчик не задан", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            // Переход с передачей reportId и objectID
                            val action = ArrangementFragmentDirections
                                .actionArrangementFragmentToTransportFragment(
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
            CoroutineScope(Dispatchers.Main).launch {
                sharedViewModel.loadPreviousReport()
                Toast.makeText(requireContext(), "Загружен предыдущий отчет", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка "Очистить"
        binding.btnClear.setOnClickListener {
            sharedViewModel.clearAll()
            removeAllTextWatchers()
            clearUiFields()
            addAllTextWatchers()
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

        val errors = sharedViewModel.validateArrangementInputs(
            workTypes, customers, manualCustomer, objects, manualObject, plotText,
            contractors, manualContractor, subContractors, manualSubContractor,
            repSSKGpText, subContractorText, repSubcontractorText, repSSKSubText
        )

        if (errors.isNotEmpty()) {
            Snackbar
                .make(binding.root, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }

        clearErrors(
            binding.textInputLayoutAutoWorkType, binding.textInputLayoutAutoCustomer,
            binding.hiddenTextInputLayoutManualCustomer, binding.textInputLayoutAutoObject,
            binding.hiddenTextInputLayoutManualObject, binding.textInputLayoutPlot,
            binding.textInputLayoutAutoContractor, binding.hiddenTextInputLayoutManualContractor,
            binding.textInputLayoutAutoSubContractor, binding.hiddenTextInputLayoutManualSubContractor,
            binding.textInputLayoutRepSSKGp, binding.textInputLayoutSubContractor,
            binding.textInputLayoutRepSubContractor, binding.textInputLayoutRepSSKSub
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
        layouts.forEach { it.isErrorEnabled = false; it.error = null }
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
        sharedViewModel.plotText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextPlot.text.toString() != text) {
                binding.textInputEditTextPlot.removeTextChangedListener(plotTextWatcher)
                binding.textInputEditTextPlot.setText(text)
                binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)
            }
        }
        sharedViewModel.repSSKGpText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSSKGp.text.toString() != text) {
                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
                binding.textInputEditTextRepSSKGp.setText(text)
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
            }
        }
        sharedViewModel.subContractorText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextSubcontractor.text.toString() != text) {
                binding.textInputEditTextSubcontractor.removeTextChangedListener(subContractorTextWatcher)
                binding.textInputEditTextSubcontractor.setText(text)
                binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
            }
        }
        sharedViewModel.repSubcontractorText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSubContractor.text.toString() != text) {
                binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
                binding.textInputEditTextRepSubContractor.setText(text)
                binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
            }
        }
        sharedViewModel.repSSKSubText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSSKSub.text.toString() != text) {
                binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)
                binding.textInputEditTextRepSSKSub.setText(text)
                binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
            }
        }
        sharedViewModel.selectedWorkType.observe(viewLifecycleOwner) { workType ->
            val currentText = binding.autoCompleteWorkType.text?.toString() ?: ""
            if (currentText != workType) {
                binding.autoCompleteWorkType.setText(workType ?: "", false)
            }
        }
        sharedViewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            val currentText = binding.autoCompleteCustomer.text?.toString() ?: ""
            if (currentText != customer) {
                binding.autoCompleteCustomer.setText(customer ?: "", false)
            }
        }
        sharedViewModel.selectedObject.observe(viewLifecycleOwner) { objectId ->
            val currentText = binding.autoCompleteObject.text?.toString() ?: ""
            if (currentText != objectId) {
                binding.autoCompleteObject.setText(objectId ?: "", false)
            }
        }
        sharedViewModel.selectedContractor.observe(viewLifecycleOwner) { contractor ->
            val currentText = binding.autoCompleteContractor.text?.toString() ?: ""
            if (currentText != contractor) {
                binding.autoCompleteContractor.setText(contractor ?: "", false)
            }
        }
        sharedViewModel.selectedSubContractor.observe(viewLifecycleOwner) { subContractor ->
            val currentText = binding.autoCompleteSubContractor.text?.toString() ?: ""
            if (currentText != subContractor) {
                binding.autoCompleteSubContractor.setText(subContractor ?: "", false)
            }
        }
        sharedViewModel.isManualCustomer.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualCustomer.isChecked = isChecked
        }
        sharedViewModel.isManualObject.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualObject.isChecked = isChecked
        }
        sharedViewModel.isManualContractor.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualContractor.isChecked = isChecked
        }
        sharedViewModel.isManualSubContractor.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualSubContractor.isChecked = isChecked
        }
        sharedViewModel.manualCustomer.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualCustomer.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualCustomer.setText(newValue)
            }
        }
        sharedViewModel.manualObject.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualObject.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualObject.setText(newValue)
            }
        }
        sharedViewModel.manualContractor.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualContractor.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualContractor.setText(newValue)
            }
        }
        sharedViewModel.manualSubContractor.observe(viewLifecycleOwner) { newValue ->
            val current = binding.hiddenTextInputEditTextManualSubContractor.text?.toString() ?: ""
            if (current != newValue) {
                binding.hiddenTextInputEditTextManualSubContractor.setText(newValue)
            }
        }
        sharedViewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
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