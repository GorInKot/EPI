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
import com.example.epi.DataBase.ExtraDatabase.ExtraDatabaseHelper
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
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository
        )
    }

    // Text Watchers для полей с ручным вводом
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
        setupPlotCheckbox()
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

        // -------- Договор СК --------
//        val contractListAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_dropdown_item,
//            SharedViewModel.contractSK
//        )
//        binding.autoCompleteContract.setAdapter(contractListAdapter)
//        binding.autoCompleteContract.inputType = InputType.TYPE_NULL
//        binding.autoCompleteContract.keyListener = null
//        binding.autoCompleteContract.setOnTouchListener { _, _ ->
//            binding.autoCompleteContract.showDropDown()
//            false
//        }
//        binding.autoCompleteContract.setOnItemClickListener { parent, _, position, _ ->
//            val selectedContractSK = parent.getItemAtPosition(position).toString()
//            sharedViewModel.setSelectedContract(selectedContractSK)
//        }
//        sharedViewModel.selectedContractSK.observe(viewLifecycleOwner) { selectedContract ->
//            val currentText = binding.autoCompleteContract.text?.toString() ?: ""
//            if (currentText != selectedContract) {
//                binding.autoCompleteContract.setText(selectedContract ?: "", false)
//            }
//        }

        //region Договор СК
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contract = withContext(Dispatchers.IO) {
                dbHelper.getContract()
            }
            if (contract.isEmpty()) {
                Toast.makeText(requireContext(), "Список договоров пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список договоров пуст")
            }
            val contractListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                contract
            )
            binding.autoCompleteContract.setAdapter(contractListAdapter)
            binding.autoCompleteContract.inputType = InputType.TYPE_NULL
            binding.autoCompleteContract.keyListener = null
            binding.autoCompleteContract.setOnTouchListener { _, _ ->
                binding.autoCompleteContract.showDropDown()
                false
            }
            binding.autoCompleteContract.setOnItemClickListener { parent, _, position, _ ->
                val selectedContract = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedContract(selectedContract)

            }
        }
        // endregion

        // -------- Заказчик --------
        //region Заказчик
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val customer = withContext(Dispatchers.IO) {
                dbHelper.getCustomer()
            }

            if (customer.isEmpty()) {
                Toast.makeText(requireContext(), "Список Заказчиков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список Заказчиков пуст")
            }

            val customerListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
                customer
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
            }
        }
        // endregion

        // -------- Объект --------
        // region Объект
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val obj = withContext(Dispatchers.IO) {
                dbHelper.getObject()
            }

            if (obj.isEmpty()) {
                Toast.makeText(requireContext(), "Список объектов пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список объектов пуст")
            }

            val objListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                obj
            )

            binding.autoCompleteObject.setAdapter(objListAdapter)
            binding.autoCompleteObject.inputType = InputType.TYPE_NULL
            binding.autoCompleteObject.keyListener = null
            binding.autoCompleteObject.setOnTouchListener { _, _ ->
                binding.autoCompleteObject.showDropDown()
                false
            }
            binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
                val selectedObject = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedObject(selectedObject)

            }
        }
        // endregion

        // -------- Участок --------
        // region Участок
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val plot = withContext(Dispatchers.IO) {
                dbHelper.getPlot()
            }
            if (plot.isEmpty()) {
                Toast.makeText(requireContext(), "Список участков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список участков пуст")
            }
            val plotListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                plot
            )
            binding.autoCompletePlot.setAdapter(plotListAdapter)
            binding.autoCompletePlot.inputType = InputType.TYPE_NULL
            binding.autoCompletePlot.keyListener = null
            binding.autoCompletePlot.setOnTouchListener { _, _ ->
                if (!sharedViewModel.isManualPlot.value!!) {
                    binding.autoCompletePlot.showDropDown()
                }
                false
            }
            binding.autoCompletePlot.setOnItemClickListener { parent, _, position, _ ->
                val selectedPlot = parent.getItemAtPosition(position).toString()
                sharedViewModel.setPlotText(selectedPlot)
                sharedViewModel.setIsManualPlot(false)
            }
        }
        // endregion

    }

    private fun setupRightBlock() {

        // -------- Генподрядчик --------
        // region Генподрядчик
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contractors = withContext(Dispatchers.IO) {
                dbHelper.getContractor()
            }
            if (contractors.isEmpty()) {
                Toast.makeText(requireContext(), "Список генподрядчиков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список генподрядчиков пуст")
            }
            val contractorListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                contractors
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
            }
        }
        // endregion

        // -------- Представитель Генподрядчика --------
        // region Представитель Генподрядчика
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val genContractors = withContext(Dispatchers.IO) {
                dbHelper.getSubContractor()
            }
            if (genContractors.isEmpty()) {
                Toast.makeText(requireContext(), "Список представителей генподрядчика пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список представителей генподрядчика пуст")
            }
            val repContractorListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                genContractors
            )
            binding.autoCompleteSubContractor.setAdapter(repContractorListAdapter)
            binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
            binding.autoCompleteSubContractor.keyListener = null
            binding.autoCompleteSubContractor.setOnTouchListener { _, _ ->
                binding.autoCompleteSubContractor.showDropDown()
                false
            }
            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedRepContractor = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedRepContractor(selectedRepContractor)
            }
        }
        // endregion

        // -------- Представитель ССК ПО (ГП) --------
        // region Представитель ССК ПО (ГП)
        repSSKGpTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextWatcher", "Before: $s, Cursor: ${binding.textInputEditTextRepSSKGp.selectionStart}")
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextWatcher", "OnChanged: $s, Cursor: ${binding.textInputEditTextRepSSKGp.selectionStart}")
            }
            override fun afterTextChanged(s: Editable?) {
                Log.d("TextWatcher", "After: $s, Cursor: ${binding.textInputEditTextRepSSKGp.selectionStart}")
                val currentText = s.toString()
                if (currentText != sharedViewModel.repSSKGpText.value) {
                    sharedViewModel.setRepSSKGpText(currentText)
                }
            }
        }
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
        // endregion

        // -------- Субподрядчик --------
        // region Субподрядчик
        subContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextWatcher", "Before: $s, Cursor: ${binding.textInputEditTextSubcontractor.selectionStart}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextWatcher", "OnChanged: $s, Cursor: ${binding.textInputEditTextSubcontractor.selectionStart}")
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("TextWatcher", "After: $s, Cursor: ${binding.textInputEditTextSubcontractor.selectionStart}")
                sharedViewModel.setSubContractorText(s.toString())
            }
        }
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
        // endregion

        // -------- Представитель субподрядчика --------
        // region
        repSubContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextWatcher", "Before: $s, Cursor: ${binding.textInputEditTextRepSubContractor.selectionStart}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextWatcher", "OnChanged: $s, Cursor: ${binding.textInputEditTextRepSubContractor.selectionStart}")
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("TextWatcher", "After: $s, Cursor: ${binding.textInputEditTextRepSubContractor.selectionStart}")
                sharedViewModel.setRepSubContractorText(s.toString())
            }
        }
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
        // endregion

        // -------- Представитель ССК ПО (Суб) --------
        // region Представитель ССК ПО (Суб)
        repSSKSubTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextWatcher", "Before: $s, Cursor: ${binding.textInputEditTextRepSSKSub.selectionStart}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextWatcher", "OnChanged: $s, Cursor: ${binding.textInputEditTextRepSSKSub.selectionStart}")
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("TextWatcher", "After: $s, Cursor: ${binding.textInputEditTextRepSSKSub.selectionStart}")
                sharedViewModel.setRepSSKSubText(s.toString())
            }
        }
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
        // endregion
    }

    private fun setupPlotCheckbox() {
        binding.checkBoxManualPlot.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setIsManualPlot(isChecked)
            if (isChecked) {
                binding.autoCompletePlot.setText("Объект не делится на участки")
                binding.autoCompletePlot.isEnabled = false
                binding.autoCompletePlot.inputType = InputType.TYPE_NULL
                binding.autoCompletePlot.keyListener = null
            } else {
                binding.autoCompletePlot.setText("")
                binding.autoCompletePlot.isEnabled = true
                binding.autoCompletePlot.inputType = InputType.TYPE_NULL
                binding.autoCompletePlot.keyListener = null
                binding.autoCompletePlot.showDropDown()
            }
        }

        // Observe
        sharedViewModel.isManualPlot.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualPlot.isChecked = isChecked
            if (isChecked && binding.autoCompletePlot.text.toString() != "Объект не делится на участки") {
                binding.autoCompletePlot.setText("Объект не делится на участки")
                binding.autoCompletePlot.isEnabled = false
            } else if (!isChecked && binding.autoCompletePlot.text.toString() == "Объект не делится на участки") {
                binding.autoCompletePlot.setText("")
                binding.autoCompletePlot.isEnabled = true
            }
        }

    }

    private fun setupButtons() {
        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                Log.d("Tagg", "Валидация прошла")
                // Логирование значений
                Log.d("Tagg", "Contract: ${sharedViewModel.contractText.value}")
                Log.d("Tagg", "Customer: ${sharedViewModel.selectedCustomer.value}")
                Log.d("Tagg", "Object: ${sharedViewModel.selectedObject.value}")
                Log.d("Tagg", "Plot: ${sharedViewModel.plotText.value}")
                Log.d("Tagg", "Contractor: ${sharedViewModel.selectedContractor.value}")
                Log.d("Tagg", "SubContractor: ${sharedViewModel.selectedSubContractor.value}")
                Log.d("Tagg", "RepSSKGp: ${sharedViewModel.repSSKGpText.value}")
                Log.d("Tagg", "SubContractorText: ${sharedViewModel.subContractorText.value}")
                Log.d("Tagg", "RepSubContractor: ${sharedViewModel.repSubContractorText.value}")
                Log.d("Tagg", "RepSSKSub: ${sharedViewModel.repSSKSubText.value}")

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val reportId = withContext(Dispatchers.IO) {
                            sharedViewModel.saveArrangementData()
                        }
                        Log.d("Tagg", "Report ID: $reportId")
                        if (reportId > 0) {
//                            Toast.makeText(requireContext(), "Отчет сохранен", Toast.LENGTH_SHORT).show()
                            // Получаем objectID (из выпадающего списка или вручную)
//                            val objectID = sharedViewModel.selectedObject.value ?: sharedViewModel.manualObject.value
//                            if (objectID.isNullOrBlank()) {
//                                Toast.makeText(requireContext(), "Объект не задан", Toast.LENGTH_SHORT).show()
//                                return@launch
//                            }
//                            val customerID = sharedViewModel.selectedCustomer.value ?: sharedViewModel.manualCustomer.value
//                            if (customerID.isNullOrBlank()) {
//                                Toast.makeText(requireContext(), "Заказчик не задан", Toast.LENGTH_SHORT).show()
//                                return@launch
//                            }
                            // Переход с передачей reportId и objectID
                            val action = ArrangementFragmentDirections
                                .actionArrangementFragmentToTransportFragment()
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
            sharedViewModel.clearAllData()
            removeAllTextWatchers()
            clearUiFields()
            addAllTextWatchers()
            Toast.makeText(requireContext(), "Все поля очищены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        val contract = binding.autoCompleteContract.text?.toString()?.trim()
        val customers = binding.autoCompleteCustomer.text?.toString()?.trim()
        val objects = binding.autoCompleteObject.text?.toString()?.trim()
        val plotText = binding.autoCompletePlot.text?.toString()?.trim()
        val contractors = binding.autoCompleteContractor.text?.toString()?.trim()
        val subContractors = binding.autoCompleteSubContractor.text?.toString()?.trim()

        val repSSKGpText = binding.textInputEditTextRepSSKGp.text?.toString()?.trim()
        val subContractorText = binding.textInputEditTextSubcontractor.text?.toString()?.trim()
        val repSubcontractorText = binding.textInputEditTextRepSubContractor.text?.toString()?.trim()
        val repSSKSubText = binding.textInputEditTextRepSSKSub.text?.toString()?.trim()
        val isManualPlot = binding.checkBoxManualPlot.isChecked

        val errors = sharedViewModel.validateArrangementInputs(
            contract,
            customers,
            objects,
            plotText,
            contractors,
            subContractors,
            repSSKGpText,
            subContractorText,
            repSubcontractorText,
            repSSKSubText,
            isManualPlot
        )

        if (errors.isNotEmpty()) {
            Snackbar
                .make(binding.root, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }

        clearErrors(
            binding.textInputLayoutContract, binding.textInputLayoutAutoCustomer,
            binding.textInputLayoutAutoObject, binding.textInputLayoutPlot,
            binding.textInputLayoutAutoContractor, binding.textInputLayoutAutoSubContractor,
            binding.textInputLayoutRepSSKGp, binding.textInputLayoutSubContractor,
            binding.textInputLayoutRepSubContractor, binding.textInputLayoutRepSSKSub
        )

//        setError(binding.textInputLayoutAutoWorkType, errors["workTypes"])
        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteCustomer.isShown,
            autoLayout = binding.textInputLayoutAutoCustomer,
            errorMessage = errors["customers"]
        )
        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteObject.isShown,
            autoLayout = binding.textInputLayoutAutoObject,
            errorMessage = errors["objects"]
        )
        setError(binding.textInputLayoutPlot, errors["plotText"])
        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteContractor.isShown,
            autoLayout = binding.textInputLayoutAutoContractor,
            errorMessage = errors["contractors"]
        )
        setConditionalDualError(
            autoFieldVisible = binding.autoCompleteSubContractor.isShown,
            autoLayout = binding.textInputLayoutAutoSubContractor,
            errorMessage = errors["subContractors"]
        )
        setError(binding.textInputLayoutRepSSKGp, errors["repSSKGpText"])
        setError(binding.textInputLayoutSubContractor, errors["subContractorText"])
        setConditionalDualError(
            autoFieldVisible = binding.textInputEditTextRepSubContractor.isShown,
            autoLayout = binding.textInputLayoutRepSubContractor,
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
        errorMessage: String?
    ) {
        if (autoFieldVisible) {
            setError(autoLayout, errorMessage)
        }
    }

    private fun removeAllTextWatchers() {
        // Old code
//        binding.autoCompletePlot.removeTextChangedListener(plotTextWatcher)
        binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
        binding.textInputEditTextSubcontractor.removeTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)

        // New code
//        binding.autoCompleteCustomer.removeTextChangedListener(customerTextWatcher)
//        binding.autoCompleteContractor.removeTextChangedListener(contractorTextWatcher)
//        binding.autoCompleteObject.removeTextChangedListener(objectTextWatcher)
//        binding.autoCompletePlot.removeTextChangedListener(plotTextWatcher)
//        binding.autoCompleteSubContractor.removeTextChangedListener(subContractorTextWatcher)
    }

    private fun clearUiFields() {
        binding.autoCompleteContract.setText("")
        binding.autoCompletePlot.setText("")
        binding.textInputEditTextRepSSKGp.setText("")
        binding.textInputEditTextSubcontractor.setText("")
        binding.textInputEditTextRepSubContractor.setText("")
        binding.textInputEditTextRepSSKSub.setText("")
        binding.autoCompleteCustomer.setText("")
        binding.autoCompleteObject.setText("")
        binding.autoCompleteContractor.setText("")
        binding.autoCompleteSubContractor.setText("")
    }

    private fun addAllTextWatchers() {
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
    }

    private fun setupViewModelObservers() {
        sharedViewModel.plotText.observe(viewLifecycleOwner) { text ->
            if (binding.autoCompletePlot.text.toString() != text) {
//                binding.autoCompletePlot.removeTextChangedListener(plotTextWatcher)
                binding.autoCompletePlot.setText(text)
//                binding.autoCompletePlot.addTextChangedListener(plotTextWatcher)
            }
        }
        // Проверка обновлений из SharedViewModel (если используется LiveData)
        sharedViewModel.repSSKGpText.observe(viewLifecycleOwner) { text ->
            if (text != binding.textInputEditTextRepSSKGp.text.toString()) {
                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
                val cursorPosition = binding.textInputEditTextRepSSKGp.selectionStart
                binding.textInputEditTextRepSSKGp.setText(text)
                binding.textInputEditTextRepSSKGp.setSelection(cursorPosition.coerceAtMost(text.length))
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
                Log.d("LiveData", "Updated text: '$text', Cursor: $cursorPosition")
            }
        }
//        sharedViewModel.repSSKGpText.observe(viewLifecycleOwner) { text ->
//            if (binding.textInputEditTextRepSSKGp.text.toString() != text) {
//                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
//                binding.textInputEditTextRepSSKGp.setText(text)
//                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
//            }
//        }
        sharedViewModel.subContractorText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextSubcontractor.text.toString() != text) {
                binding.textInputEditTextSubcontractor.removeTextChangedListener(subContractorTextWatcher)
                binding.textInputEditTextSubcontractor.setText(text)
                binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
            }
        }
        sharedViewModel.repSubContractorText.observe(viewLifecycleOwner) { text ->
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
//        sharedViewModel.selectedWorkType.observe(viewLifecycleOwner) { workType ->
//            val currentText = binding.autoCompleteWorkType.text?.toString() ?: ""
//            if (currentText != workType) {
//                binding.autoCompleteWorkType.setText(workType ?: "", false)
//            }
//        }
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
////        sharedViewModel.manualCustomer.observe(viewLifecycleOwner) { newValue ->
////            val current = binding.hiddenTextInputEditTextManualCustomer.text?.toString() ?: ""
////            if (current != newValue) {
////                binding.hiddenTextInputEditTextManualCustomer.setText(newValue)
////            }
////        }
//        sharedViewModel.manualObject.observe(viewLifecycleOwner) { newValue ->
//            val current = binding.hiddenTextInputEditTextManualObject.text?.toString() ?: ""
//            if (current != newValue) {
//                binding.hiddenTextInputEditTextManualObject.setText(newValue)
//            }
//        }
//        sharedViewModel.manualContractor.observe(viewLifecycleOwner) { newValue ->
//            val current = binding.hiddenTextInputEditTextManualContractor.text?.toString() ?: ""
//            if (current != newValue) {
//                binding.hiddenTextInputEditTextManualContractor.setText(newValue)
//            }
//        }
//        sharedViewModel.manualSubContractor.observe(viewLifecycleOwner) { newValue ->
//            val current = binding.hiddenTextInputEditTextManualSubContractor.text?.toString() ?: ""
//            if (current != newValue) {
//                binding.hiddenTextInputEditTextManualSubContractor.setText(newValue)
//            }
//        }
        sharedViewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar
                .make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }
    }

    // New code
//    private fun setupViewModelObservers() {
//        sharedViewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
//            val currentText = binding.autoCompleteCustomer.text?.toString() ?: ""
//            if (currentText != customer) {
//                binding.autoCompleteCustomer.setText(customer ?: "", false)
//            }
//        }
//        sharedViewModel.selectedContractor.observe(viewLifecycleOwner) { contractor ->
//            val currentText = binding.autoCompleteContractor.text?.toString() ?: ""
//            if (currentText != contractor) {
//                binding.autoCompleteContractor.setText(contractor ?: "", false)
//            }
//        }
//        sharedViewModel.selectedObject.observe(viewLifecycleOwner) { objectName ->
//            val currentText = binding.autoCompleteObject.text?.toString() ?: ""
//            if (currentText != objectName) {
//                binding.autoCompleteObject.setText(objectName ?: "", false)
//            }
//        }
//        sharedViewModel.selectedPlot.observe(viewLifecycleOwner) { plot ->
//            val currentText = binding.autoCompletePlot.text?.toString() ?: ""
//            if (currentText != plot) {
//                binding.autoCompletePlot.setText(plot ?: "", false)
//            }
//        }
//        sharedViewModel.selectedSubContractor.observe(viewLifecycleOwner) { subContractor ->
//            val currentText = binding.autoCompleteSubContractor.text?.toString() ?: ""
//            if (currentText != subContractor) {
//                binding.autoCompleteSubContractor.setText(subContractor ?: "", false)
//            }
//        }
//        sharedViewModel.selectedWorkType.observe(viewLifecycleOwner) { workType ->
//            val currentText = binding.autoCompleteWorkType.text?.toString() ?: ""
//            if (currentText != workType) {
//                binding.autoCompleteWorkType.setText(workType ?: "", false)
//            }
//        }
//        sharedViewModel.selectedCustomerId.observe(viewLifecycleOwner) { customerId ->
//            if (customerId != null) {
//                updateDependentDropdowns(customerId, null, null)
//            } else {
//                updateDependentDropdowns(null, null, null)
//            }
//        }
//        sharedViewModel.selectedObjectId.observe(viewLifecycleOwner) { objectId ->
//            if (objectId != null) {
//                updateDependentDropdowns(null, objectId, null)
//            }
//        }
//        sharedViewModel.selectedContractorId.observe(viewLifecycleOwner) { contractorId ->
//            if (contractorId != null) {
//                updateDependentDropdowns(null, null, contractorId)
//            }
//        }
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        removeAllTextWatchers()
        _binding = null
    }
}