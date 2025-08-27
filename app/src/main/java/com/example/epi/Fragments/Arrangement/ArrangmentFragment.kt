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
import com.example.epi.DataBase.PlanValue.PlanValueRepository
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
            (requireActivity().application as App).orderNumberRepository
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
                false
            }
            binding.autoCompleteCustomer.setOnItemClickListener { parent, _, position, _ ->
                val selectedCustomer = customerList[position]
                sharedViewModel.setSelectedCustomer(selectedCustomer)
                updateContractorDropdown(selectedCustomer)
                updateContractDropdown(selectedCustomer)
                updateSubContractorDropdown(selectedCustomer)
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
                false
            }
            binding.autoCompleteContract.setOnItemClickListener { parent, _, position, _ ->
                val selectedContract = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedContract(selectedContract)
                updateObjectDropdown(selectedContract)
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
                false
            }
            binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
                val selectedObject = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedObject(selectedObject)
                updatePlotDropdown(selectedObject)
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
                }
                false
            }
            binding.autoCompletePlot.setOnItemClickListener { parent, _, position, _ ->
                val selectedPlot = parent.getItemAtPosition(position).toString()
                sharedViewModel.setPlotText(selectedPlot)
                sharedViewModel.setIsManualPlot(false)
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
                false
            }
            binding.autoCompleteContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedContractor = contractorsForCustomer[position]
                sharedViewModel.setSelectedContractor(selectedContractor)
                updateRepSSKGpDropdown(selectedContractor)
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
                false
            }
            binding.autoCompleteContract.setOnItemClickListener { parent, _, position, _ ->
                val selectedContract = contractsForCustomer[position]
                sharedViewModel.setSelectedContract(selectedContract)
                updateObjectDropdown(selectedContract)
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
                false
            }
            binding.autoCompleteObject.setOnItemClickListener { parent, _, position, _ ->
                val selectedObject = objectsForContract[position]
                sharedViewModel.setSelectedObject(selectedObject)
                updatePlotDropdown(selectedObject)
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
                }
                false
            }
            binding.autoCompletePlot.setOnItemClickListener { parent, _, position, _ ->
                val selectedPlot = plotsForObject[position]
                sharedViewModel.setPlotText(selectedPlot)
                sharedViewModel.setIsManualPlot(false)
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
                if (!sharedViewModel.isManualSubContractor.value!!) {
                    binding.autoCompleteSubContractor.showDropDown()
                }
                false
            }
            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedSubContractor = subContractorsForCustomer[position]
                sharedViewModel.setSelectedSubContractor(selectedSubContractor)
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
            binding.autoCompleteRepContractor.inputType = InputType.TYPE_NULL
            binding.autoCompleteRepContractor.keyListener = null
            binding.autoCompleteRepContractor.setOnTouchListener { _, _ ->
                binding.autoCompleteRepContractor.showDropDown()
                false
            }
            binding.autoCompleteRepContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedRepSSKGp = repSSKGpsForContractor[position]
                sharedViewModel.setSelectedRepContractor(selectedRepSSKGp)
            }
        }
    }
    // endregion

    private fun setupRightBlock() {
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
                Log.d("Tagg-Arrangement", "Touch on autoCompleteContractor, enable: ${binding.autoCompleteContractor.isEnabled}")
                binding.autoCompleteContractor.showDropDown()
                false
            }
            binding.autoCompleteContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedContractor = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedContractor(selectedContractor)
                updateRepSSKGpDropdown(selectedContractor)
                Log.d("Tagg-Arrangement", "Selected contractor: $selectedContractor")
            }
        }

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
                false
            }
            binding.autoCompleteRepContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedRepSSKGp = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedRepContractor(selectedRepSSKGp)
                Log.d("Tagg-Arrangement", "Selected repSSKGp: $selectedRepSSKGp")
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
                }
                false
            }
            binding.autoCompleteSubContractor.setOnItemClickListener { parent, _, position, _ ->
                val selectedSubContractor = parent.getItemAtPosition(position).toString()
                sharedViewModel.setSelectedSubContractor(selectedSubContractor)
                Log.d("Tagg-Arrangement", "Selected subContractor: $selectedSubContractor")
            }
        }

        // Представитель ССК ПО (ГП)
        repSSKGpTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSSKGpText(s.toString())
            }
        }
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)

        // Субподрядчик (TextWatcher для выпадающего списка)
        subContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setSelectedSubContractor(s.toString())
                Log.d("Tagg-Arrangement", "TextWatcher subContractor changed to: ${s.toString()}")
            }
        }
        binding.autoCompleteSubContractor.addTextChangedListener(subContractorTextWatcher)

        // Представитель субподрядчика
        repSubContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSubContractorText(s.toString())
            }
        }
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)

        // Представитель ССК ПО (Суб)
        repSSKSubTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sharedViewModel.setRepSSKSubText(s.toString())
            }
        }
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
    }

    private fun setupSubContractorCheckbox() {
        binding.checkBoxManualSubContractor.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setIsManualSubContractor(isChecked)
            if (isChecked) {
                binding.autoCompleteSubContractor.setText("Отсутствует субподрядчик", false)
                binding.autoCompleteSubContractor.isEnabled = false
                binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
                binding.autoCompleteSubContractor.keyListener = null
                Log.d("Tagg-Arrangement", "Manual subContractor enabled, autoComplete disabled")
            } else {
                binding.autoCompleteSubContractor.setText("", false)
                binding.autoCompleteSubContractor.isEnabled = true
                binding.autoCompleteSubContractor.inputType = InputType.TYPE_NULL
                binding.autoCompleteSubContractor.keyListener = null
                Log.d("Tagg-Arrangement", "Manual subContractor disabled, autoComplete enabled")
            }

            binding.textInputEditTextRepSubContractor.setText(if (isChecked) "Отсутствует субподрядчик" else "")
            binding.textInputEditTextRepSubContractor.isEnabled = !isChecked
            binding.textInputEditTextRepSubContractor.inputType = if (isChecked) InputType.TYPE_NULL else InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            binding.textInputEditTextRepSubContractor.keyListener = if (isChecked) null else binding.textInputEditTextRepSubContractor.keyListener

            binding.textInputEditTextRepSSKSub.setText(if (isChecked) "Отсутствует субподрядчик" else "")
            binding.textInputEditTextRepSSKSub.isEnabled = !isChecked
            binding.textInputEditTextRepSSKSub.inputType = if (isChecked) InputType.TYPE_NULL else InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            binding.textInputEditTextRepSSKSub.keyListener = if (isChecked) null else binding.textInputEditTextRepSSKSub.keyListener

            if (isChecked) {
                sharedViewModel.setSelectedSubContractor("Отсутствует субподрядчик")
                sharedViewModel.setRepSubContractorText("Отсутствует субподрядчик")
                sharedViewModel.setRepSSKSubText("Отсутствует субподрядчик")
            } else {
                sharedViewModel.setSelectedSubContractor(null)
                sharedViewModel.setRepSubContractorText("")
                sharedViewModel.setRepSSKSubText("")
            }
        }

        sharedViewModel.isManualSubContractor.observe(viewLifecycleOwner) { isChecked ->
            binding.checkBoxManualSubContractor.isChecked = isChecked
            if (isChecked) {
                if (binding.autoCompleteSubContractor.text.toString() != "Отсутствует субподрядчик") {
                    binding.autoCompleteSubContractor.setText("Отсутствует субподрядчик", false)
                    binding.autoCompleteSubContractor.isEnabled = false
                    Log.d("Tagg-Arrangement", "Manual subContractor observed, autoComplete disabled")
                }
                if (binding.textInputEditTextRepSubContractor.text.toString() != "Отсутствует субподрядчик") {
                    binding.textInputEditTextRepSubContractor.setText("Отсутствует субподрядчик")
                    binding.textInputEditTextRepSubContractor.isEnabled = false
                }
                if (binding.textInputEditTextRepSSKSub.text.toString() != "Отсутствует субподрядчик") {
                    binding.textInputEditTextRepSSKSub.setText("Отсутствует субподрядчик")
                    binding.textInputEditTextRepSSKSub.isEnabled = false
                }
            } else {
                if (binding.autoCompleteSubContractor.text.toString() == "Отсутствует субподрядчик") {
                    binding.autoCompleteSubContractor.setText("", false)
                    binding.autoCompleteSubContractor.isEnabled = true
                    Log.d("Tagg-Arrangement", "Manual subContractor observed, autoComplete enabled")
                }
                if (binding.textInputEditTextRepSubContractor.text.toString() == "Отсутствует субподрядчик") {
                    binding.textInputEditTextRepSubContractor.setText("")
                    binding.textInputEditTextRepSubContractor.isEnabled = true
                }
                if (binding.textInputEditTextRepSSKSub.text.toString() == "Отсутствует субподрядчик") {
                    binding.textInputEditTextRepSSKSub.setText("")
                    binding.textInputEditTextRepSSKSub.isEnabled = true
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
            } else {
                binding.autoCompletePlot.setText("")
                binding.autoCompletePlot.isEnabled = true
                binding.autoCompletePlot.inputType = InputType.TYPE_NULL
                binding.autoCompletePlot.keyListener = null
                binding.autoCompletePlot.showDropDown()
            }
        }

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
                Log.d("Tagg-Arrangement", "Валидация прошла")
                // Логирование значений
                Log.d("Tagg-Arrangement",
                    "Заказчик: ${sharedViewModel.selectedCustomer.value}\nДоговор СК: ${sharedViewModel.selectedContract.value}\n" +
                            "Объект: ${sharedViewModel.selectedObject.value}\nУчасток: ${sharedViewModel.plotText.value}\n" +
                            "Генподрядчик: ${sharedViewModel.selectedContractor.value}\nПредставитель Генподрядчика: ${sharedViewModel.selectedSubContractor.value}\n" +
                            "Представитель ССК ГП (ПО): ${sharedViewModel.repSSKGpText.value}\nСубподрядчик: ${sharedViewModel.selectedSubContractor.value}\n" +
                            "Представитель Субподрядчика: ${sharedViewModel.repSubContractorText.value}\nПредставитель ССК ГП (Суб): ${sharedViewModel.repSSKSubText.value}")
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val reportId = withContext(Dispatchers.IO) {
                            sharedViewModel.saveArrangementData()
                        }
                        Log.d("Tagg-Arrangement", "Номер отчета (ID): $reportId")
                        if (reportId > 0) {
//
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
        val subContractors = binding.autoCompleteSubContractor.text?.toString()?.trim() // Субподрядчик
        val repSSKGpText = binding.textInputEditTextRepSSKGp.text?.toString()?.trim()
        val repContractor = binding.autoCompleteRepContractor.text?.toString()?.trim() // Представитель генподрядчика
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
            if (isManualSubContractor) "Отсутствует субподрядчик" else subContractors,
            repSSKGpText,
            if (isManualSubContractor) "Отсутствует субподрядчик" else repContractor,
            if (isManualSubContractor) "Отсутствует субподрядчик" else repSubContractorText,
            if (isManualSubContractor) "Отсутствует субподрядчик" else repSSKSubText,
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
            binding.textInputLayoutContract,
            binding.textInputLayoutAutoCustomer,
            binding.textInputLayoutAutoObject,
            binding.textInputLayoutPlot,
            binding.textInputLayoutAutoContractor,
            binding.textInputLayoutAutoSubContractor,
            binding.textInputLayoutRepSSKGp,
            binding.textInputLayoutSubContractor,
            binding.textInputLayoutRepSubContractor,
            binding.textInputLayoutRepSSKSub
        )

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
            autoFieldVisible = binding.autoCompleteRepContractor.isShown,
            autoLayout = binding.textInputLayoutAutoSubContractor,
            errorMessage = errors["repContractor"]
        )
        if (!isManualSubContractor) {
            setConditionalDualError(
                autoFieldVisible = binding.autoCompleteSubContractor.isShown,
                autoLayout = binding.textInputLayoutSubContractor,
                errorMessage = errors["subContractors"]
            )
            setConditionalDualError(
                autoFieldVisible = binding.textInputEditTextRepSubContractor.isShown,
                autoLayout = binding.textInputLayoutRepSubContractor,
                errorMessage = errors["repSubContractorText"]
            )
            setError(binding.textInputLayoutRepSSKSub, errors["repSSKSubText"])
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