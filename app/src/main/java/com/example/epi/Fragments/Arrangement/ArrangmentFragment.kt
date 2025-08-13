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
        // region Договор СК Старый вариант без связей
//        val contractListAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_dropdown_item,
//            SharedViewModel.selectedContract
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
//        sharedViewModel.selectedContract.observe(viewLifecycleOwner) { selectedContract ->
//            val currentText = binding.autoCompleteContract.text?.toString() ?: ""
//            if (currentText != selectedContract) {
//                binding.autoCompleteContract.setText(selectedContract ?: "", false)
//            }
//        }
        // endregion

        // region Договор СК Новый вариант
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contracts = withContext(Dispatchers.IO) {
                dbHelper.getContracts()
            }
            if (contracts.isEmpty()) {
                Toast.makeText(requireContext(), "Список договоров пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список договоров пуст")
            }
            val contractListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                contracts
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
                // Обновляем Object при выборе Contract
                updateObjectDropdown(selectedContract)
            }
        }
        // endregion

        // -------- Заказчик --------
        //region Заказчик (Вариант без связей)
//        CoroutineScope(Dispatchers.Main).launch {
//            val dbHelper = ExtraDatabaseHelper(requireContext())
//            val customer = withContext(Dispatchers.IO) {
//                dbHelper.getCustomers()
//            }
//
//            if (customer.isEmpty()) {
//                Toast.makeText(requireContext(), "Список Заказчиков пуст", Toast.LENGTH_SHORT).show()
//                Log.d("Tagg", "Список Заказчиков пуст")
//            }
//
//            val customerListAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_dropdown_item,
//                customer
//        )
//            binding.autoCompleteCustomer.setAdapter(customerListAdapter)
//            binding.autoCompleteCustomer.inputType = InputType.TYPE_NULL
//            binding.autoCompleteCustomer.keyListener = null
//            binding.autoCompleteCustomer.setOnTouchListener { _, _ ->
//                binding.autoCompleteCustomer.showDropDown()
//                false
//            }
//            binding.autoCompleteCustomer.setOnItemClickListener { parent, _, position, _ ->
//                val selectedCustomer = parent.getItemAtPosition(position).toString()
//                sharedViewModel.setSelectedCustomer(selectedCustomer)
//            }
//        }
        // endregion

        // region Заказчик Новый вариант
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val customersWithContracts = withContext(Dispatchers.IO) {
                dbHelper.getCustomersWithContracts() // Связь Customer -> Contract
            }
            if (customersWithContracts.isEmpty()) {
                Toast.makeText(requireContext(), "Список заказчиков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список заказчиков пуст")
            }
            val customerList = customersWithContracts.map { it.first }.distinct()
            val customerListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                customerList
            )
            binding.autoCompleteCustomer.setAdapter(customerListAdapter)
            binding.autoCompleteCustomer.inputType = InputType.TYPE_NULL
            binding.autoCompleteCustomer.keyListener = null
            binding.autoCompleteCustomer.setOnTouchListener { _, _ ->
                binding.autoCompleteCustomer.showDropDown()
                false
            }
            binding.autoCompleteCustomer.setOnItemClickListener { parent, _, position, _ ->
                val selectedCustomer = customerList[position]
                sharedViewModel.setSelectedCustomer(selectedCustomer)
                // Обновляем зависимые списки: Contractor, Contract, SubContractor
                updateContractorDropdown(selectedCustomer)
                updateContractDropdown(selectedCustomer)
                updateSubContractorDropdown(selectedCustomer)
            }
        }
        // endregion

        // -------- Объект --------
        // region Объект (Вариант без связей)
//        CoroutineScope(Dispatchers.Main).launch {
//            val dbHelper = ExtraDatabaseHelper(requireContext())
//            val obj = withContext(Dispatchers.IO) {
//                dbHelper.getObjects()
//            }
//
//            if (obj.isEmpty()) {
//                Toast.makeText(requireContext(), "Список объектов пуст", Toast.LENGTH_SHORT).show()
//                Log.d("Tagg", "Список объектов пуст")
//            }
//
//            val objListAdapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                obj
//            )
//
//            binding.autoCompleteObject.setAdapter(objListAdapter)
//            binding.autoCompleteObject.inputType = InputType.TYPE_NULL
//            binding.autoCompleteObject.keyListener = null
//            binding.autoCompleteObject.setOnTouchListener { _, _ ->
//                binding.autoCompleteObject.showDropDown()
//                false
//            }
//            binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
//                val selectedObject = parent.getItemAtPosition(position).toString()
//                sharedViewModel.setSelectedObject(selectedObject)
//
//            }
//        }
        // endregion

        // region Объект Новый вариант
        // Обновляется динамически из updateObjectDropdown
        // endregion

        // -------- Участок --------
        // region Участок
//        CoroutineScope(Dispatchers.Main).launch {
//            val dbHelper = ExtraDatabaseHelper(requireContext())
//            val plot = withContext(Dispatchers.IO) {
//                dbHelper.getPlots()
//            }
//            if (plot.isEmpty()) {
//                Toast.makeText(requireContext(), "Список участков пуст", Toast.LENGTH_SHORT).show()
//                Log.d("Tagg", "Список участков пуст")
//            }
//            val plotListAdapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                plot
//            )
//            binding.autoCompletePlot.setAdapter(plotListAdapter)
//            binding.autoCompletePlot.inputType = InputType.TYPE_NULL
//            binding.autoCompletePlot.keyListener = null
//            binding.autoCompletePlot.setOnTouchListener { _, _ ->
//                if (!sharedViewModel.isManualPlot.value!!) {
//                    binding.autoCompletePlot.showDropDown()
//                }
//                false
//            }
//            binding.autoCompletePlot.setOnItemClickListener { parent, _, position, _ ->
//                val selectedPlot = parent.getItemAtPosition(position).toString()
//                sharedViewModel.setPlotText(selectedPlot)
//                sharedViewModel.setIsManualPlot(false)
//            }
//        }
        // endregion

        // region Участок Новый вариант
        // Обновляется динамически из updatePlotDropdown (добавьте вызов при выборе Object)
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val plots = withContext(Dispatchers.IO) {
                dbHelper.getPlots()
            }
            if (plots.isEmpty()) {
                Toast.makeText(requireContext(), "Список участков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список участков пуст")
            }
            val plotListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                plots
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

    // region Вспомогательные функции для Left Code Block
    private fun updateContractorDropdown(selectedCustomer: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val customersWithContractors = withContext(Dispatchers.IO) {
                dbHelper.getCustomersWithContractors()
            }
            val contractorsForCustomer = customersWithContractors
                .filter { it.first == selectedCustomer }
                .mapNotNull { it.second }.distinct()
            val contractorListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                contractorsForCustomer
            )
            binding.autoCompleteContractor.setAdapter(contractorListAdapter)
            if (contractorsForCustomer.isNotEmpty()) {
                binding.autoCompleteContractor.setText(contractorsForCustomer[0], false)
                sharedViewModel.setSelectedContractor(contractorsForCustomer[0])
            } else {
                binding.autoCompleteContractor.setText("", false)
                sharedViewModel.setSelectedContractor(null)
            }
        }
    }

    private fun updateContractDropdown(selectedCustomer: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val customersWithContracts = withContext(Dispatchers.IO) {
                dbHelper.getCustomersWithContracts()
            }
            val contractsForCustomer = customersWithContracts
                .filter { it.first == selectedCustomer }
                .mapNotNull { it.second }.distinct()
            val contractListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                contractsForCustomer
            )
            binding.autoCompleteContract.setAdapter(contractListAdapter)
            if (contractsForCustomer.isNotEmpty()) {
                binding.autoCompleteContract.setText(contractsForCustomer[0], false)
                sharedViewModel.setSelectedContract(contractsForCustomer[0])
            } else {
                binding.autoCompleteContract.setText("", false)
                sharedViewModel.setSelectedContract(null)
            }
        }
    }

    private fun updateObjectDropdown(selectedContract: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contractsWithObjects = withContext(Dispatchers.IO) {
                dbHelper.getContractsWithObjects()
            }
            val objectsForContract = contractsWithObjects
                .filter { it.first == selectedContract }
                .mapNotNull { it.second }.distinct()
            val objectListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                objectsForContract
            )
            binding.autoCompleteObject.setAdapter(objectListAdapter)
            if (objectsForContract.isNotEmpty()) {
                binding.autoCompleteObject.setText(objectsForContract[0], false)
                sharedViewModel.setSelectedObject(objectsForContract[0])
                updatePlotDropdown(objectsForContract[0])
            } else {
                binding.autoCompleteObject.setText("", false)
                sharedViewModel.setSelectedObject(null)
            }
        }
    }

    private fun updatePlotDropdown(selectedObject: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val objectsWithPlots = withContext(Dispatchers.IO) {
                dbHelper.getObjectsWithPlots()
            }
            val plotsForObject = objectsWithPlots
                .filter { it.first == selectedObject }
                .mapNotNull { it.second }.distinct()
            val plotListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                plotsForObject
            )
            binding.autoCompletePlot.setAdapter(plotListAdapter)
            if (plotsForObject.isNotEmpty()) {
                binding.autoCompletePlot.setText(plotsForObject[0], false)
                sharedViewModel.setPlotText(plotsForObject[0])
            } else {
                binding.autoCompletePlot.setText("", false)
                sharedViewModel.setPlotText(null)
            }
        }
    }

    private fun updateSubContractorDropdown(selectedCustomer: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val customersWithSubContractors = withContext(Dispatchers.IO) {
                dbHelper.getCustomersWithSubContractors()
            }
            val subContractorsForCustomer = customersWithSubContractors
                .filter { it.first == selectedCustomer }
                .mapNotNull { it.second }.distinct()
            val subContractorListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                subContractorsForCustomer
            )
            binding.autoCompleteSubContractor.setAdapter(subContractorListAdapter)
            binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
            binding.autoCompleteSubContractor.keyListener = null
            binding.autoCompleteSubContractor.setOnTouchListener { _, _ ->
                binding.autoCompleteSubContractor.showDropDown()
                false
            }
            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedSubContractor = subContractorsForCustomer[position]
                sharedViewModel.setSelectedSubContractor(selectedSubContractor)
            }
            if (subContractorsForCustomer.isNotEmpty()) {
                binding.autoCompleteSubContractor.setText(subContractorsForCustomer[0], false)
                sharedViewModel.setSelectedSubContractor(subContractorsForCustomer[0])
            } else {
                binding.autoCompleteSubContractor.setText("", false)
                sharedViewModel.setSelectedSubContractor(null)
            }
        }
    }

    // endregion





    private fun setupRightBlock() {

        // -------- Генподрядчик --------
        // region Генподрядчик
//        CoroutineScope(Dispatchers.Main).launch {
//            val dbHelper = ExtraDatabaseHelper(requireContext())
//            val contractors = withContext(Dispatchers.IO) {
//                dbHelper.getContractors()
//            }
//            if (contractors.isEmpty()) {
//                Toast.makeText(requireContext(), "Список генподрядчиков пуст", Toast.LENGTH_SHORT).show()
//                Log.d("Tagg", "Список генподрядчиков пуст")
//            }
//            val contractorListAdapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                contractors
//            )
//            binding.autoCompleteContractor.setAdapter(contractorListAdapter)
//            binding.autoCompleteContractor.inputType = InputType.TYPE_NULL
//            binding.autoCompleteContractor.keyListener = null
//            binding.autoCompleteContractor.setOnTouchListener { _, _ ->
//                binding.autoCompleteContractor.showDropDown()
//                false
//            }
//            binding.autoCompleteContractor.setOnItemClickListener { parent, _, position, _ ->
//                val selectedContractor = parent.getItemAtPosition(position).toString()
//                sharedViewModel.setSelectedContractor(selectedContractor)
//            }
//        }
        // endregion

        // region Генподрядчик Новый вариант
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contractors = withContext(Dispatchers.IO) {
                dbHelper.getContractors()
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
//        CoroutineScope(Dispatchers.Main).launch {
//            val dbHelper = ExtraDatabaseHelper(requireContext())
//            val genContractors = withContext(Dispatchers.IO) {
//                dbHelper.getSubContractors()
//            }
//            if (genContractors.isEmpty()) {
//                Toast.makeText(requireContext(), "Список представителей генподрядчика пуст", Toast.LENGTH_SHORT).show()
//                Log.d("Tagg", "Список представителей генподрядчика пуст")
//            }
//            val repContractorListAdapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                genContractors
//            )
//            binding.autoCompleteSubContractor.setAdapter(repContractorListAdapter)
//            binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
//            binding.autoCompleteSubContractor.keyListener = null
//            binding.autoCompleteSubContractor.setOnTouchListener { _, _ ->
//                binding.autoCompleteSubContractor.showDropDown()
//                false
//            }
//            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
//                val selectedRepContractor = parent.getItemAtPosition(position).toString()
//                sharedViewModel.setSelectedRepContractor(selectedRepContractor)
//            }
//        }
        // endregion

        // region Представитель Генподрядчика Новый вариант
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val repSSKGps = withContext(Dispatchers.IO) {
                dbHelper.getRepSSKGp()
            }
            if (repSSKGps.isEmpty()) {
                Toast.makeText(requireContext(), "Список представителей генподрядчика пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg", "Список представителей генподрядчика пуст")
            }
            val repContractorListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                repSSKGps
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

    // Чекбокс для "Участок"
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
                Log.d("Tagg",
                    "Contract: ${sharedViewModel.contractText.value}\nCustomer: ${sharedViewModel.selectedCustomer.value}\n" +
                            "Object: ${sharedViewModel.selectedObject.value}\nPlot: ${sharedViewModel.plotText.value}\n" +
                            "Contractor: ${sharedViewModel.selectedContractor.value}\nSubContractor: ${sharedViewModel.selectedSubContractor.value}\n" +
                            "RepSSKGp: ${sharedViewModel.repSSKGpText.value}\nSubContractorText: ${sharedViewModel.subContractorText.value}\n" +
                            "RepSubContractor: ${sharedViewModel.repSubContractorText.value}\nRepSSKSub: ${sharedViewModel.repSSKSubText.value}")
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
                binding.textInputEditTextRepSSKGp.setSelection(cursorPosition.coerceAtMost(text!!.length))
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
                Log.d("LiveData", "Updated text: '$text', Cursor: $cursorPosition")
            }
        }

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

        sharedViewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar
                .make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeAllTextWatchers()
        _binding = null
    }
}