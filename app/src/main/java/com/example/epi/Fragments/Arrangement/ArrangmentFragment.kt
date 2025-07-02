package com.example.epi.Fragments.Arrangement

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.ViewModel.SharedViewModel
import com.example.epi.databinding.FragmentArrangmentBinding

class ArrangementFragment : Fragment() {

    private var _binding: FragmentArrangmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()

    private lateinit var plotTextWatcher: TextWatcher
    private lateinit var repSSKGpTextWatcher: TextWatcher
    private lateinit var subContractorTextWatcher: TextWatcher
    private lateinit var repSubContractorTextWatcher: TextWatcher
    private lateinit var repSSKSubTextWatcher: TextWatcher

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArrangmentBinding.inflate(inflater, container, false )
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
        // Обработка даты и времени
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
            viewModel.customers)
        binding.autoCompleteCustomer.setAdapter(customerListAdapter)
        binding.autoCompleteCustomer.setOnTouchListener { v, event ->
            binding.autoCompleteCustomer.showDropDown()
            false
        }
        binding.autoCompleteCustomer.setOnItemClickListener { parent, view, position, id ->
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
        binding.autoCompleteObject.setOnTouchListener { v, event ->
            binding.autoCompleteObject.showDropDown()
            false
        }
        binding.autoCompleteObject.setOnItemClickListener { parent, view, position, id ->
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
        binding.autoCompleteContractor.setOnTouchListener { v, event ->
            binding.autoCompleteContractor.showDropDown()
            false
        }
        binding.autoCompleteContractor.setOnItemClickListener { parent, view, position, id ->
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
        binding.hiddenTextInputEditTextManualContractor.addTextChangedListener(object :
            TextWatcher {
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
        binding.autoCompleteSubContractor.setOnTouchListener { v, event ->
            binding.autoCompleteSubContractor.showDropDown()
            false
        }
        binding.autoCompleteSubContractor.setOnItemClickListener { parent, view, position, id ->
            val selectedSubContractor = parent.getItemAtPosition(position).toString()
            viewModel.selectedSubContractor.value = selectedSubContractor
        }

        // Генподрядчик: обработка CheckBox
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

        subContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSubContractorChanged(s.toString())
            }
        }
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)

        binding.hiddenTextInputEditTextManualSubContractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.manualSubContractor.value = s.toString()
            }
        })

        repSubContractorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSubcontractorChanged(s.toString())
            }
        }
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)

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
            findNavController().navigate(R.id.transportFragment)
        }
        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
        // Кнопка "Копия предыдущего отчета"
        binding.btnCopy.setOnClickListener {
            Toast.makeText(requireContext(),"Пока ничего не происходит", Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Очистить"
        binding.btnClear.setOnClickListener {
            viewModel.arrangementIsClearing.value = true

            // Чистим ViewModel, лучше если там операции быстрые
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

    private fun removeAllTextWatchers() {
        // Отключаем все TextWatcher перед очисткой
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

        // Очищаем поля UI
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
        // Восстанавливаем слушатели
        binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)
        binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
        binding.textInputEditTextSubcontractor.addTextChangedListener(subContractorTextWatcher)
        binding.textInputEditTextRepSubContractor.addTextChangedListener(repSubContractorTextWatcher)
        binding.textInputEditTextRepSSKSub.addTextChangedListener(repSSKSubTextWatcher)
    }


    private fun setupViewModelObservers() {
        // Участок
        viewModel.plotText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextPlot.text.toString() != text) {
                binding.textInputEditTextPlot.removeTextChangedListener(plotTextWatcher)
                binding.textInputEditTextPlot.setText(text)
                binding.textInputEditTextPlot.addTextChangedListener(plotTextWatcher)
            }
        }

        // Представитель ГП
        viewModel.repSSKGpText.observe(viewLifecycleOwner) { text ->
            if (binding.textInputEditTextRepSSKGp.text.toString() != text) {
                binding.textInputEditTextRepSSKGp.removeTextChangedListener(repSSKGpTextWatcher)
                binding.textInputEditTextRepSSKGp.setText(text)
                binding.textInputEditTextRepSSKGp.addTextChangedListener(repSSKGpTextWatcher)
            }
        }
        // Представитель Генподрядчика
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

        // Режим работы
        viewModel.selectedWorkType.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteWorkType.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteWorkType.setText(it ?: "", false )
            }
        }
        // Заказчик
        viewModel.selectedCustomer.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteCustomer.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteCustomer.setText(it ?: "", false)
            }
        }
        // Объект
        viewModel.selectedObject.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteObject.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteObject.setText(it ?: "", false)
            }
        }
        // Генподрядчик
        viewModel.selectedContractor.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteContractor.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteContractor.setText(it ?: "", false)
            }
        }
        // Представитель Генподрядчика
        viewModel.selectedSubContractor.observe(viewLifecycleOwner) {
            val currentText = binding.autoCompleteSubContractor.text?.toString() ?: ""
            if (currentText != it) {
                binding.autoCompleteSubContractor.setText(it ?: "", false)
            }
        }

        // Чекбоксы
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

        // Тексты ручного ввода
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