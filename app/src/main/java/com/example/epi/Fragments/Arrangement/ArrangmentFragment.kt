package com.example.epi.Fragments.Arrangement

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
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext,
            (requireActivity().application as App).planValueRepository,
            (requireActivity().application as App).orderNumberRepository,
            (requireActivity().application as App).factValueRepository
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
        setupSubContractorCheckbox()
        setupScrollToError()
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
        // Заказчик
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val customersWithContracts = withContext(Dispatchers.IO) {
                dbHelper.getCustomersWithContracts()
            }
            if (customersWithContracts.isEmpty()) {
                Toast.makeText(requireContext(), "Список заказчиков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список заказчиков пуст")
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
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoCustomer.top)
                false
            }
            binding.autoCompleteCustomer.setOnItemClickListener { parent, _, position, _ ->
                val selectedCustomer = customerList[position]
                sharedViewModel.setSelectedCustomer(selectedCustomer)
                // Очистка всех зависимых полей и чекбоксов
                binding.autoCompleteContract.setText("", false)
                binding.autoCompleteObject.setText("", false)
                binding.autoCompletePlot.setText("", false)
                binding.autoCompleteContractor.setText("", false)
                binding.autoCompleteRepContractor.setText("", false)
                binding.textInputEditTextRepSSKGp.setText("")
                binding.autoCompleteSubContractor.setText("", false)
                binding.textInputEditTextRepSubContractor.setText("")
                binding.textInputEditTextRepSSKSub.setText("")
                binding.checkBoxManualPlot.isChecked = false
                binding.checkBoxManualSubContractor.isChecked = false
                // Очистка данных в SharedViewModel
                sharedViewModel.setSelectedContract(null)
                sharedViewModel.setSelectedObject(null)
                sharedViewModel.setPlotText(null)
                sharedViewModel.setSelectedContractor(null)
                sharedViewModel.setSelectedRepContractor(null)
                sharedViewModel.setRepSSKGpText("")
                sharedViewModel.setSelectedSubContractor(null)
                sharedViewModel.setRepSubContractorText("")
                sharedViewModel.setRepSSKSubText("")
                sharedViewModel.setIsManualPlot(false)
                sharedViewModel.setIsManualSubContractor(false)
                // Обновление зависимых списков
                updateContractDropdown(selectedCustomer)
                updateContractorDropdown(selectedCustomer)
                updateSubContractorDropdown(selectedCustomer)
                binding.textInputLayoutAutoCustomer.isErrorEnabled = false
                binding.textInputLayoutAutoCustomer.error = null
                Log.d("Tagg-Arrangement", "Customer changed to: $selectedCustomer, all dependent fields cleared")
            }
        }

        // Договор СК
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contracts = withContext(Dispatchers.IO) {
                dbHelper.getContracts()
            }
            if (contracts.isEmpty()) {
                Toast.makeText(requireContext(), "Список договоров пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список договоров пуст")
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
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutContract.top)
                false
            }
            binding.autoCompleteContract.setOnItemClickListener { parent, _, position, _ ->
                val selectedContract = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedContract(selectedContract)
                // Очистка зависимых полей
                binding.autoCompleteObject.setText("", false)
                binding.autoCompletePlot.setText("", false)
                binding.checkBoxManualPlot.isChecked = false
                // Очистка данных в SharedViewModel
                sharedViewModel.setSelectedObject(null)
                sharedViewModel.setPlotText(null)
                sharedViewModel.setIsManualPlot(false)
                updateObjectDropdown(selectedContract)
                binding.textInputLayoutContract.isErrorEnabled = false
                binding.textInputLayoutContract.error = null
            }
        }

        // Объект
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val objects = withContext(Dispatchers.IO) {
                dbHelper.getObjects()
            }
            if (objects.isEmpty()) {
                Toast.makeText(requireContext(), "Список объектов пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список объектов пуст")
            }
            val objectListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                objects
            )
            binding.autoCompleteObject.setAdapter(objectListAdapter)
            binding.autoCompleteObject.inputType = InputType.TYPE_NULL
            binding.autoCompleteObject.keyListener = null
            binding.autoCompleteObject.setOnTouchListener { _, _ ->
                binding.autoCompleteObject.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoObject.top)
                false
            }
            binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
                val selectedObject = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedObject(selectedObject)
                // Очистка зависимых полей
                binding.autoCompletePlot.setText("", false)
                binding.checkBoxManualPlot.isChecked = false
                // Очистка данных в SharedViewModel
                sharedViewModel.setPlotText(null)
                sharedViewModel.setIsManualPlot(false)
                updatePlotDropdown(selectedObject)
                binding.textInputLayoutAutoObject.isErrorEnabled = false
                binding.textInputLayoutAutoObject.error = null
            }
        }

        // Участок
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val plots = withContext(Dispatchers.IO) {
                dbHelper.getPlots()
            }
            if (plots.isEmpty()) {
                Toast.makeText(requireContext(), "Список участков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список участков пуст")
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
                    binding.scrollView.smoothScrollTo(0, binding.textInputLayoutPlot.top)
                }
                false
            }
            binding.autoCompletePlot.setOnItemClickListener { parent, _, position, _ ->
                val selectedPlot = parent.getItemAtPosition(position).toString()
                sharedViewModel.setPlotText(selectedPlot)
                sharedViewModel.setIsManualPlot(false)
                binding.textInputLayoutPlot.isErrorEnabled = false
                binding.textInputLayoutPlot.error = null
            }
        }

        // Генподрядчик
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contractors = withContext(Dispatchers.IO) {
                dbHelper.getContractors()
            }
            if (contractors.isEmpty()) {
                Toast.makeText(requireContext(), "Список генподрядчиков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список генподрядчиков пуст")
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
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoContractor.top)
                false
            }
            binding.autoCompleteContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedContractor = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedContractor(selectedContractor)
                // Очистка зависимого поля
                binding.autoCompleteRepContractor.setText("", false)
                // Очистка данных в SharedViewModel
                sharedViewModel.setSelectedRepContractor(null)
                updateRepSSKGpDropdown(selectedContractor)
                binding.textInputLayoutAutoContractor.isErrorEnabled = false
                binding.textInputLayoutAutoContractor.error = null
            }
        }
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
            binding.autoCompleteContractor.setOnTouchListener { _, _ ->
                binding.autoCompleteContractor.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoContractor.top)
                false
            }
            binding.autoCompleteContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedContractor = contractorsForCustomer[position]
                sharedViewModel.setSelectedContractor(selectedContractor)
                // Очистка зависимого поля
                binding.autoCompleteRepContractor.setText("", false)
                // Очистка данных в SharedViewModel
                sharedViewModel.setSelectedRepContractor(null)
                updateRepSSKGpDropdown(selectedContractor)
                binding.textInputLayoutAutoContractor.isErrorEnabled = false
                binding.textInputLayoutAutoContractor.error = null
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
            binding.autoCompleteContract.setOnTouchListener { _, _ ->
                binding.autoCompleteContract.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutContract.top)
                false
            }
            binding.autoCompleteContract.setOnItemClickListener { parent, _, position, _ ->
                val selectedContract = contractsForCustomer[position]
                sharedViewModel.setSelectedContract(selectedContract)
                // Очистка зависимых полей
                binding.autoCompleteObject.setText("", false)
                binding.autoCompletePlot.setText("", false)
                binding.checkBoxManualPlot.isChecked = false
                // Очистка данных в SharedViewModel
                sharedViewModel.setSelectedObject(null)
                sharedViewModel.setPlotText(null)
                sharedViewModel.setIsManualPlot(false)
                updateObjectDropdown(selectedContract)
                binding.textInputLayoutContract.isErrorEnabled = false
                binding.textInputLayoutContract.error = null
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
            binding.autoCompleteObject.setOnTouchListener { _, _ ->
                binding.autoCompleteObject.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoObject.top)
                false
            }
            binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
                val selectedObject = objectsForContract[position]
                sharedViewModel.setSelectedObject(selectedObject)
                // Очистка зависимых полей
                binding.autoCompletePlot.setText("", false)
                binding.checkBoxManualPlot.isChecked = false
                // Очистка данных в SharedViewModel
                sharedViewModel.setPlotText(null)
                sharedViewModel.setIsManualPlot(false)
                updatePlotDropdown(selectedObject)
                binding.textInputLayoutAutoObject.isErrorEnabled = false
                binding.textInputLayoutAutoObject.error = null
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
            binding.autoCompletePlot.setOnTouchListener { _, _ ->
                if (!sharedViewModel.isManualPlot.value!!) {
                    binding.autoCompletePlot.showDropDown()
                    binding.scrollView.smoothScrollTo(0, binding.textInputLayoutPlot.top)
                }
                false
            }
            binding.autoCompletePlot.setOnItemClickListener { parent, _, position, _ ->
                val selectedPlot = plotsForObject[position]
                sharedViewModel.setPlotText(selectedPlot)
                sharedViewModel.setIsManualPlot(false)
                binding.textInputLayoutPlot.isErrorEnabled = false
                binding.textInputLayoutPlot.error = null
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
            binding.autoCompleteSubContractor.setOnTouchListener { _, _ ->
                if (!sharedViewModel.isManualSubContractor.value!!) {
                    binding.autoCompleteSubContractor.showDropDown()
                    binding.scrollView.smoothScrollTo(0, binding.textInputLayoutSubContractor.top)
                }
                false
            }
            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedSubContractor = subContractorsForCustomer[position]
                sharedViewModel.setSelectedSubContractor(selectedSubContractor)
                binding.textInputLayoutSubContractor.isErrorEnabled = false
                binding.textInputLayoutSubContractor.error = null
            }
        }
    }

    private fun updateRepSSKGpDropdown(selectedContractor: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val contractorsWithRepSSKGp = withContext(Dispatchers.IO) {
                dbHelper.getContractorsWithRepSSKGp()
            }
            val repSSKGpsForContractor = contractorsWithRepSSKGp
                .filter { it.first == selectedContractor }
                .mapNotNull { it.second }.distinct()
            val repSSKGpListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                repSSKGpsForContractor
            )
            binding.autoCompleteRepContractor.setAdapter(repSSKGpListAdapter)
            binding.autoCompleteRepContractor.setOnTouchListener { _, _ ->
                binding.autoCompleteRepContractor.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoRepContractor.top)
                false
            }
            binding.autoCompleteRepContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedRepSSKGp = repSSKGpsForContractor[position]
                sharedViewModel.setSelectedRepContractor(selectedRepSSKGp)
                binding.textInputLayoutAutoRepContractor.isErrorEnabled = false
                binding.textInputLayoutAutoRepContractor.error = null
            }
        }
    }
    // endregion

    private fun setupRightBlock() {
        // Представитель Генподрядчика
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val repSSKGps = withContext(Dispatchers.IO) {
                dbHelper.getRepSSKGp()
            }
            if (repSSKGps.isEmpty()) {
                Toast.makeText(requireContext(), "Список представителей генподрядчика пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список представителей генподрядчика пуст")
            }
            val repSSKGpListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                repSSKGps
            )
            binding.autoCompleteRepContractor.setAdapter(repSSKGpListAdapter)
            binding.autoCompleteRepContractor.inputType = InputType.TYPE_NULL
            binding.autoCompleteRepContractor.keyListener = null
            binding.autoCompleteRepContractor.setOnTouchListener { _, _ ->
                Log.d("Tagg-Arrangement", "Touch on autoCompleteRepContractor, enabled: ${binding.autoCompleteRepContractor.isEnabled}")
                binding.autoCompleteRepContractor.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoRepContractor.top)
                false
            }
            binding.autoCompleteRepContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedRepSSKGp = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedRepContractor(selectedRepSSKGp)
                binding.textInputLayoutAutoRepContractor.isErrorEnabled = false
                binding.textInputLayoutAutoRepContractor.error = null
                Log.d("Tagg-Arrangement", "Selected repSSKGp: $selectedRepSSKGp")
            }
        }

        // Представитель ССК ПО (ГП)
        repSSKGpTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSSKGpText(s.toString())
                binding.textInputLayoutRepSSKGp.isErrorEnabled = false
                binding.textInputLayoutRepSSKGp.error = null
            }
        }
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
        binding.textInputEditTextRepSSKGp.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutRepSSKGp.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutRepSSKGp at ${binding.textInputLayoutRepSSKGp.top}")
            }
        }

        // Субподрядчик
        CoroutineScope(Dispatchers.Main).launch {
            val dbHelper = ExtraDatabaseHelper(requireContext())
            val subContractors = withContext(Dispatchers.IO) {
                dbHelper.getSubContractors()
            }
            if (subContractors.isEmpty()) {
                Toast.makeText(requireContext(), "Список субподрядчиков пуст", Toast.LENGTH_SHORT).show()
                Log.d("Tagg-Arrangement", "Список субподрядчиков пуст")
            }
            val subContractorListAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                subContractors
            )
            binding.autoCompleteSubContractor.setAdapter(subContractorListAdapter)
            binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
            binding.autoCompleteSubContractor.keyListener = null
            binding.autoCompleteSubContractor.setOnTouchListener { _, _ ->
                Log.d("Tagg-Arrangement", "Touch on autoCompleteSubContractor, enabled: ${binding.autoCompleteSubContractor.isEnabled}, " +
                        "manualSubContractor: ${sharedViewModel.isManualSubContractor.value}, " +
                        "text: ${binding.autoCompleteSubContractor.text}")
                if (!sharedViewModel.isManualSubContractor.value!! && binding.autoCompleteSubContractor.isEnabled) {
                    binding.autoCompleteSubContractor.showDropDown()
                    binding.scrollView.smoothScrollTo(0, binding.textInputLayoutSubContractor.top)
                }
                false
            }
            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedSubContractor = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedSubContractor(selectedSubContractor)
                binding.textInputLayoutSubContractor.isErrorEnabled = false
                binding.textInputLayoutSubContractor.error = null
                Log.d("Tagg-Arrangement", "Selected subContractor: $selectedSubContractor")
            }
        }

        // Субподрядчик (TextWatcher для выпадающего списка)
        subContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setSelectedSubContractor(s.toString())
                binding.textInputLayoutSubContractor.isErrorEnabled = false
                binding.textInputLayoutSubContractor.error = null
                Log.d("Tagg-Arrangement", "TextWatcher subContractor changed to: ${s.toString()}")
            }
        }
        binding.autoCompleteSubContractor.addTextChangedListener(subContractorTextWatcher)

        // Представитель Субподрядчика
        repSubContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSubContractorText(s.toString())
                binding.textInputLayoutRepSubContractor.isErrorEnabled = false
                binding.textInputLayoutRepSubContractor.error = null
            }
        }
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutRepSubContractor.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutRepSubContractor at ${binding.textInputLayoutRepSubContractor.top}")
            }
        }

        // Представитель ССК ПО (Суб)
        repSSKSubTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSSKSubText(s.toString())
                binding.textInputLayoutRepSSKSub.isErrorEnabled = false
                binding.textInputLayoutRepSSKSub.error = null
            }
        }
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
        binding.textInputEditTextRepSSKSub.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutRepSSKSub.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutRepSSKSub at ${binding.textInputLayoutRepSSKSub.top}")
            }
        }
    }

    private fun setupSubContractorCheckbox() {
        binding.checkBoxManualSubContractor.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setIsManualSubContractor(isChecked)
            if (isChecked) {
                binding.autoCompleteSubContractor.setText("Субподрядчик отсутствует", false)
                binding.autoCompleteSubContractor.isEnabled = false
                binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
                binding.autoCompleteSubContractor.keyListener = null
                binding.textInputLayoutSubContractor.isErrorEnabled = false
                binding.textInputLayoutSubContractor.error = null
                Log.d("Tagg-Arrangement", "Manual subContractor enabled, autoComplete disabled")
            } else {
                binding.autoCompleteSubContractor.setText("", false)
                binding.autoCompleteSubContractor.isEnabled = true
                binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
                binding.autoCompleteSubContractor.keyListener = null
                Log.d("Tagg-Arrangement", "Manual subContractor disabled, autoComplete enabled")
            }

            binding.textInputEditTextRepSubContractor.setText(if (isChecked) "Субподрядчик отсутствует" else "")
            binding.textInputEditTextRepSubContractor.isEnabled = !isChecked
            binding.textInputEditTextRepSubContractor.inputType = if (isChecked) InputType.TYPE_NULL else InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            binding.textInputEditTextRepSubContractor.keyListener = if (isChecked) null else binding.textInputEditTextRepSubContractor.keyListener
            binding.textInputLayoutRepSubContractor.isErrorEnabled = false
            binding.textInputLayoutRepSubContractor.error = null

            binding.textInputEditTextRepSSKSub.setText(if (isChecked) "Субподрядчик отсутствует" else "")
            binding.textInputEditTextRepSSKSub.isEnabled = !isChecked
            binding.textInputEditTextRepSSKSub.inputType = if (isChecked) InputType.TYPE_NULL else InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            binding.textInputEditTextRepSSKSub.keyListener = if (isChecked) null else binding.textInputEditTextRepSSKSub.keyListener
            binding.textInputLayoutRepSSKSub.isErrorEnabled = false
            binding.textInputLayoutRepSSKSub.error = null

            if (isChecked) {
                sharedViewModel.setSelectedSubContractor("Субподрядчик отсутствует")
                sharedViewModel.setRepSubContractorText("Субподрядчик отсутствует")
                sharedViewModel.setRepSSKSubText("Субподрядчик отсутствует")
            } else {
                sharedViewModel.setSelectedSubContractor(null)
                sharedViewModel.setRepSubContractorText("")
                sharedViewModel.setRepSSKSubText("")
            }
        }

        sharedViewModel.isManualSubContractor.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualSubContractor.isChecked = isChecked
            if (isChecked) {
                if (binding.autoCompleteSubContractor.text.toString() != "Субподрядчик отсутствует") {
                    binding.autoCompleteSubContractor.setText("Субподрядчик отсутствует", false)
                    binding.autoCompleteSubContractor.isEnabled = false
                    binding.textInputLayoutSubContractor.isErrorEnabled = false
                    binding.textInputLayoutSubContractor.error = null
                    Log.d("Tagg-Arrangement", "Manual subContractor observed, autoComplete disabled")
                }
                if (binding.textInputEditTextRepSubContractor.text.toString() != "Субподрядчик отсутствует") {
                    binding.textInputEditTextRepSubContractor.setText("Субподрядчик отсутствует")
                    binding.textInputEditTextRepSubContractor.isEnabled = false
                    binding.textInputLayoutRepSubContractor.isErrorEnabled = false
                    binding.textInputLayoutRepSubContractor.error = null
                }
                if (binding.textInputEditTextRepSSKSub.text.toString() != "Субподрядчик отсутствует") {
                    binding.textInputEditTextRepSSKSub.setText("Субподрядчик отсутствует")
                    binding.textInputEditTextRepSSKSub.isEnabled = false
                    binding.textInputLayoutRepSSKSub.isErrorEnabled = false
                    binding.textInputLayoutRepSSKSub.error = null
                }
            } else {
                if (binding.autoCompleteSubContractor.text.toString() == "Субподрядчик отсутствует") {
                    binding.autoCompleteSubContractor.setText("", false)
                    binding.autoCompleteSubContractor.isEnabled = true
                    binding.textInputLayoutSubContractor.isErrorEnabled = false
                    binding.textInputLayoutSubContractor.error = null
                    Log.d("Tagg-Arrangement", "Manual subContractor observed, autoComplete enabled")
                }
                if (binding.textInputEditTextRepSubContractor.text.toString() == "Субподрядчик отсутствует") {
                    binding.textInputEditTextRepSubContractor.setText("")
                    binding.textInputEditTextRepSubContractor.isEnabled = true
                    binding.textInputLayoutRepSubContractor.isErrorEnabled = false
                    binding.textInputLayoutRepSubContractor.error = null
                }
                if (binding.textInputEditTextRepSSKSub.text.toString() == "Субподрядчик отсутствует") {
                    binding.textInputEditTextRepSSKSub.setText("")
                    binding.textInputEditTextRepSSKSub.isEnabled = true
                    binding.textInputLayoutRepSSKSub.isErrorEnabled = false
                    binding.textInputLayoutRepSSKSub.error = null
                }
            }
        }
    }

    private fun setupPlotCheckbox() {
        binding.checkBoxManualPlot.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setIsManualPlot(isChecked)
            if (isChecked) {
                binding.autoCompletePlot.setText("Объект не делится на участки")
                binding.autoCompletePlot.isEnabled = false
                binding.autoCompletePlot.inputType = InputType.TYPE_NULL
                binding.autoCompletePlot.keyListener = null
                binding.textInputLayoutPlot.isErrorEnabled = false
                binding.textInputLayoutPlot.error = null
            } else {
                binding.autoCompletePlot.setText("")
                binding.autoCompletePlot.isEnabled = true
                binding.autoCompletePlot.inputType = InputType.TYPE_NULL
                binding.autoCompletePlot.keyListener = null
                binding.autoCompletePlot.showDropDown()
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutPlot.top)
            }
        }

        sharedViewModel.isManualPlot.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualPlot.isChecked = isChecked
            if (isChecked && binding.autoCompletePlot.text.toString() != "Объект не делится на участки") {
                binding.autoCompletePlot.setText("Объект не делится на участки")
                binding.autoCompletePlot.isEnabled = false
                binding.textInputLayoutPlot.isErrorEnabled = false
                binding.textInputLayoutPlot.error = null
            } else if (!isChecked && binding.autoCompletePlot.text.toString() == "Объект не делится на участки") {
                binding.autoCompletePlot.setText("")
                binding.autoCompletePlot.isEnabled = true
                binding.textInputLayoutPlot.isErrorEnabled = false
                binding.textInputLayoutPlot.error = null
            }
        }
    }

    private fun setupButtons() {
        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                Log.d("Tagg-Arrangement", "Валидация прошла")
                // Логирование значений
                Log.d("Tagg-Arrangement",
                    "Заказчик: ${sharedViewModel.selectedCustomer.value}\nДоговор СК: ${sharedViewModel.selectedContract.value}\n" +
                            "Объект: ${sharedViewModel.selectedObject.value}\nУчасток: ${sharedViewModel.plotText.value}\n" +
                            "Генподрядчик: ${sharedViewModel.selectedContractor.value}\nПредставитель Генподрядчика: ${sharedViewModel.selectedRepContractor.value}\n" +
                            "Представитель ССК ГП (ПО): ${sharedViewModel.repSSKGpText.value}\nСубподрядчик: ${sharedViewModel.selectedSubContractor.value}\n" +
                            "Представитель Субподрядчика: ${sharedViewModel.repSubContractorText.value}\nПредставитель ССК ГП (Суб): ${sharedViewModel.repSSKSubText.value}")
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val reportId = withContext(Dispatchers.IO) {
                            sharedViewModel.saveArrangementData()
                        }
                        Log.d("Tagg-Arrangement", "Номер отчета (ID): $reportId")
                        if (reportId > 0) {
                            // Переход с передачей reportId и objectID
                            val action = ArrangementFragmentDirections
                                .actionArrangementFragmentToTransportFragment()
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(requireContext(), "Ошибка сохранения отчета", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("Tagg-Arrangement", "Ошибка при сохранении отчета: ${e.message}")
                        Toast.makeText(requireContext(), "Ошибка сохранения отчета", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("Tagg-Arrangement", "Валидация НЕ прошла")
            }
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_ArrangementFragment_to_StartFragment)
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
        val repContractor = binding.autoCompleteRepContractor.text?.toString()?.trim()
        val repSubContractorText = binding.textInputEditTextRepSubContractor.text?.toString()?.trim()
        val repSSKSubText = binding.textInputEditTextRepSSKSub.text?.toString()?.trim()
        val isManualPlot = binding.checkBoxManualPlot.isChecked
        val isManualSubContractor = binding.checkBoxManualSubContractor.isChecked

        val errors = sharedViewModel.validateArrangementInputs(
            contract,
            customers,
            objects,
            plotText,
            contractors,
            if (isManualSubContractor) "Субподрядчик отсутствует" else subContractors,
            repSSKGpText,
            repContractor,
            if (isManualSubContractor) "Субподрядчик отсутствует" else repSubContractorText,
            if (isManualSubContractor) "Субподрядчик отсутствует" else repSSKSubText,
            isManualPlot
        )

        if (errors.isNotEmpty()) {
            Snackbar
                .make(binding.root, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                .show()
        }

        // Очистка всех ошибок
        clearErrors(
            binding.textInputLayoutAutoCustomer,
            binding.textInputLayoutContract,
            binding.textInputLayoutAutoObject,
            binding.textInputLayoutPlot,
            binding.textInputLayoutAutoContractor,
            binding.textInputLayoutAutoRepContractor,
            binding.textInputLayoutRepSSKGp,
            binding.textInputLayoutSubContractor,
            binding.textInputLayoutRepSubContractor,
            binding.textInputLayoutRepSSKSub
        )

        // Установка ошибок и прокрутка к первому полю с ошибкой
        var firstErrorField: TextInputLayout? = null

        // Заказчик
        if (!errors["customers"].isNullOrBlank()) {
            setError(binding.textInputLayoutAutoCustomer, errors["customers"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutAutoCustomer
        }

        // Договор СК
        if (!errors["contract"].isNullOrBlank()) {
            setError(binding.textInputLayoutContract, errors["contract"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutContract
        }

        // Объект
        if (!errors["objects"].isNullOrBlank()) {
            setError(binding.textInputLayoutAutoObject, errors["objects"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutAutoObject
        }

        // Участок
        if (!errors["plotText"].isNullOrBlank() && !isManualPlot) {
            setError(binding.textInputLayoutPlot, errors["plotText"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutPlot
        }

        // Генподрядчик
        if (!errors["contractors"].isNullOrBlank()) {
            setError(binding.textInputLayoutAutoContractor, errors["contractors"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutAutoContractor
        }

        // Представитель Генподрядчика
        if (!errors["repContractor"].isNullOrBlank()) {
            setError(binding.textInputLayoutAutoRepContractor, errors["repContractor"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutAutoRepContractor
        }

        // Представитель ССК ПО (ГП)
        if (!errors["repSSKGpText"].isNullOrBlank()) {
            setError(binding.textInputLayoutRepSSKGp, errors["repSSKGpText"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutRepSSKGp
        }

        // Субподрядчик
        if (!isManualSubContractor && !errors["subContractors"].isNullOrBlank()) {
            setError(binding.textInputLayoutSubContractor, errors["subContractors"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutSubContractor
        }

        // Представитель Субподрядчика
        if (!isManualSubContractor && !errors["repSubContractorText"].isNullOrBlank()) {
            setError(binding.textInputLayoutRepSubContractor, errors["repSubContractorText"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutRepSubContractor
        }

        // Представитель ССК ПО (Суб)
        if (!isManualSubContractor && !errors["repSSKSubText"].isNullOrBlank()) {
            setError(binding.textInputLayoutRepSSKSub, errors["repSSKSubText"])
            if (firstErrorField == null) firstErrorField = binding.textInputLayoutRepSSKSub
        }

        // Прокрутка к первому полю с ошибкой
        firstErrorField?.let {
            binding.scrollView.smoothScrollTo(0, it.top)
            Log.d("Tagg-Arrangement", "Scroll to first error field: ${it.id}")
        }

        return errors.isEmpty()
    }

    private fun clearErrors(vararg layouts: TextInputLayout) {
        layouts.forEach { it.isErrorEnabled = false; it.error = null }
    }

    private fun setError(layout: TextInputLayout, errorMessage: String?) {
        layout.isErrorEnabled = !errorMessage.isNullOrBlank()
        layout.error = errorMessage
    }

    private fun setupScrollToError() {
        // Прокрутка к полям при получении фокуса
        binding.autoCompleteCustomer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoCustomer.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutAutoCustomer at ${binding.textInputLayoutAutoCustomer.top}")
            }
        }
        binding.autoCompleteContract.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutContract.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutContract at ${binding.textInputLayoutContract.top}")
            }
        }
        binding.autoCompleteObject.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoObject.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutAutoObject at ${binding.textInputLayoutAutoObject.top}")
            }
        }
        binding.autoCompletePlot.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !sharedViewModel.isManualPlot.value!!) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutPlot.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutPlot at ${binding.textInputLayoutPlot.top}")
            }
        }
        binding.autoCompleteContractor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoContractor.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutAutoContractor at ${binding.textInputLayoutAutoContractor.top}")
            }
        }
        binding.autoCompleteRepContractor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutAutoRepContractor.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutAutoRepContractor at ${binding.textInputLayoutAutoRepContractor.top}")
            }
        }
        binding.autoCompleteSubContractor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !sharedViewModel.isManualSubContractor.value!!) {
                binding.scrollView.smoothScrollTo(0, binding.textInputLayoutSubContractor.top)
                Log.d("Tagg-Arrangement", "Scroll to textInputLayoutSubContractor at ${binding.textInputLayoutSubContractor.top}")
            }
        }
    }

    private fun removeAllTextWatchers() {
        binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
        binding.autoCompleteSubContractor.removeTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)
    }

    private fun clearUiFields() {
        binding.autoCompleteCustomer.setText("")
        binding.autoCompleteContract.setText("")
        binding.autoCompleteObject.setText("")
        binding.autoCompletePlot.setText("")
        binding.autoCompleteContractor.setText("")
        binding.autoCompleteRepContractor.setText("")
        binding.textInputEditTextRepSSKGp.setText("")
        binding.autoCompleteSubContractor.setText("")
        binding.textInputEditTextRepSubContractor.setText("")
        binding.textInputEditTextRepSSKSub.setText("")
        binding.checkBoxManualPlot.isChecked = false
        binding.checkBoxManualSubContractor.isChecked = false
    }

    private fun addAllTextWatchers() {
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
        binding.autoCompleteSubContractor.addTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
    }

    private fun setupViewModelObservers() {
        sharedViewModel.selectedContract.observe(viewLifecycleOwner) { contract ->
            val currentText = binding.autoCompleteContract.text?.toString() ?: ""
            Log.d(
                "Tagg-Arrangement",
                "Observed selectedContract: $contract, currentText: $currentText, isEnabled: ${binding.autoCompleteContract.isEnabled}"
            )
            if (contract != null && currentText != contract) {
                binding.autoCompleteContract.setText(contract, false)
                Log.d("Tagg-Arrangement", "Set autoCompleteContract text to: $contract")
            } else if (contract == null) {
                binding.autoCompleteContract.setText("", false)
                Log.d("Tagg-Arrangement", "Cleared autoCompleteContract text")
            }
        }

        sharedViewModel.plotText.observe(viewLifecycleOwner) { text ->
            if (text != null && binding.autoCompletePlot.text.toString() != text) {
                binding.autoCompletePlot.setText(text)
            } else if (text == null) {
                binding.autoCompletePlot.setText("")
            }
        }

        sharedViewModel.repSSKGpText.observe(viewLifecycleOwner) { text ->
            if (text != null && text != binding.textInputEditTextRepSSKGp.text.toString()) {
                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
                val cursorPosition = binding.textInputEditTextRepSSKGp.selectionStart
                binding.textInputEditTextRepSSKGp.setText(text)
                binding.textInputEditTextRepSSKGp.setSelection(cursorPosition.coerceAtMost(text.length))
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
                Log.d("Tagg-Arr", "repSSKGp-LiveData-Updated text: '$text', Cursor: $cursorPosition")
            } else if (text == null) {
                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
                binding.textInputEditTextRepSSKGp.setText("")
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
            }
        }

        sharedViewModel.selectedSubContractor.observe(viewLifecycleOwner) { subContractor ->
            val currentText = binding.autoCompleteSubContractor.text.toString()
            if (subContractor != null && currentText != subContractor) {
                binding.autoCompleteSubContractor.removeTextChangedListener(subContractorTextWatcher)
                binding.autoCompleteSubContractor.setText(subContractor, false)
                binding.autoCompleteSubContractor.addTextChangedListener(subContractorTextWatcher)
            } else if (subContractor == null) {
                binding.autoCompleteSubContractor.removeTextChangedListener(subContractorTextWatcher)
                binding.autoCompleteSubContractor.setText("", false)
                binding.autoCompleteSubContractor.addTextChangedListener(subContractorTextWatcher)
            }
        }

        sharedViewModel.repSubContractorText.observe(viewLifecycleOwner) { text ->
            if (text != null && binding.textInputEditTextRepSubContractor.text.toString() != text) {
                binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
                binding.textInputEditTextRepSubContractor.setText(text)
                binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
            } else if (text == null) {
                binding.textInputEditTextRepSubContractor.removeTextChangedListener(repSubContractorTextWatcher)
                binding.textInputEditTextRepSubContractor.setText("")
                binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
            }
        }

        sharedViewModel.repSSKSubText.observe(viewLifecycleOwner) { text ->
            if (text != null && binding.textInputEditTextRepSSKSub.text.toString() != text) {
                binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)
                binding.textInputEditTextRepSSKSub.setText(text)
                binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
            } else if (text == null) {
                binding.textInputEditTextRepSSKSub.removeTextChangedListener(repSSKSubTextWatcher)
                binding.textInputEditTextRepSSKSub.setText("")
                binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
            }
        }

        sharedViewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            val currentText = binding.autoCompleteCustomer.text?.toString() ?: ""
            if (customer != null && currentText != customer) {
                binding.autoCompleteCustomer.setText(customer, false)
            } else if (customer == null) {
                binding.autoCompleteCustomer.setText("", false)
            }
        }

        sharedViewModel.selectedObject.observe(viewLifecycleOwner) { objectId ->
            val currentText = binding.autoCompleteObject.text?.toString() ?: ""
            if (objectId != null && currentText != objectId) {
                binding.autoCompleteObject.setText(objectId, false)
            } else if (objectId == null) {
                binding.autoCompleteObject.setText("", false)
            }
        }

        sharedViewModel.selectedContractor.observe(viewLifecycleOwner) { contractor ->
            val currentText = binding.autoCompleteContractor.text?.toString() ?: ""
            if (contractor != null && currentText != contractor) {
                binding.autoCompleteContractor.setText(contractor, false)
            } else if (contractor == null) {
                binding.autoCompleteContractor.setText("", false)
            }
        }

        sharedViewModel.selectedRepContractor.observe(viewLifecycleOwner) { repContractor ->
            val currentText = binding.autoCompleteRepContractor.text?.toString() ?: ""
            if (repContractor != null && currentText != repContractor) {
                binding.autoCompleteRepContractor.setText(repContractor, false)
            } else if (repContractor == null) {
                binding.autoCompleteRepContractor.setText("", false)
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