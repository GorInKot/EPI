package com.example.epi.Fragments.Arrangement

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentArrangmentBinding

class ArrangementFragment : Fragment() {
    private var _binding: FragmentArrangmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ArrangementViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArrangmentBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ArrangementViewModel::class.java]

        setupDateTime()
        setupLeftBlock()
        setupRightBlock()
        setupButtons()
        setupViewModelObservers()
    }

    private fun setupDateTime() {
        // Обработка даты и времени
        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.AttrFrTvDate.text = "Дата: $it"
        }
        viewModel.currentTime.observe(viewLifecycleOwner) {
            binding.AttrFrTvTime.text = "Время: $it"
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
        binding.autoCompleteWorkType.setOnTouchListener { v, event ->
            binding.autoCompleteWorkType.showDropDown()
            false
        }
        binding.autoCompleteWorkType.setOnItemClickListener { parent, view, position, id ->
            val selectedWorkType = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedWorkType", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedCustomer", Toast.LENGTH_SHORT).show()
        }

        // Заказчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualCustomer,
            binding.textInputLayoutAutoCustomer,
            binding.textInputLayoutManualCustomer
        )

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
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedObject", Toast.LENGTH_SHORT).show()
        }

        // Объект: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualObject,
            binding.textInputLayoutAutoObject,
            binding.textInputLayoutManualObject
        )

        binding.edPlot.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onPlotChanged(s.toString())
            }
        })

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
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedContractor", Toast.LENGTH_SHORT).show()
        }
        // Генподрядчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualContractor,
            binding.textInputLayoutAutoContractor,
            binding.textInputLayoutManualContractor
        )
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
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedSubContractor", Toast.LENGTH_SHORT).show()
        }

        // Генподрядчик: обработка CheckBox
        setupToggleCheckbox(
            binding.checkBoxManualSubContractor,
            binding.textInputLayoutAutoSubContractor,
            binding.textInputLayoutManualSubContractor
        )

        // Представитель ССК ПО (ГП)
        binding.edRepSSKGp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSSKGpChanged(s.toString())
            }
        })

        // Субподрядчик
        binding.edSubcontractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSubContractorChanged(s.toString())
            }
        })

        // Представитель Субподрядчика
        binding.edRepSubcontractor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSubcontractorChanged(s.toString())
            }
        })

        // Представитель ССК ПО (Суб)
        binding.edRepSSKSub.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onRepSSKSubChanged(s.toString())
            }
        })
    }

    private fun setupButtons() {
        // Кнопка "Далее"
        binding.AttrFrBtnNext.setOnClickListener {
            findNavController().navigate(R.id.transportFragment)
        }
        // Кнопка "Назад"
        binding.AttrFrrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
        // Кнопка "Копия предыдущего отчета"
        binding.AttrFrBtnCopy.setOnClickListener {
            Toast.makeText(requireContext(),"Пока ничего не происходит",Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Очистить"
        binding.AttrFrBtnClear.setOnClickListener {
            viewModel.clearAll()
            binding.checkBoxManualCustomer.isChecked = false
            binding.checkBoxManualObject.isChecked = false
            binding.checkBoxManualContractor.isChecked = false
            binding.checkBoxManualSubContractor.isChecked = false
        }
    }

    private fun setupViewModelObservers() {
        // Участок
        viewModel.plotText.observe(viewLifecycleOwner) {text ->
            if (binding.edPlot.text.toString() != text) { binding.edPlot.setText(text) }
        }

        // Представитель ССК ПО (ГП)
        viewModel.repSSKGpText.observe(viewLifecycleOwner) {text ->
            if (binding.edRepSSKGp.text.toString() != text) { binding.edRepSSKGp.setText(text) }
        }

        // Субподрядчик
        viewModel.subContractorText.observe(viewLifecycleOwner) { text ->
            if (binding.edSubcontractor.text.toString() != text) {
                binding.edSubcontractor.setText(text) }
        }

        // Представитель Субподрядчика
        viewModel.repSubcontractorText.observe(viewLifecycleOwner) { text ->
            if (binding.edRepSubcontractor.text.toString() != text) {
                binding.edRepSubcontractor.setText(text)
            }
        }

        // Представитель ССК ПО (Суб)
        viewModel.repSSKSubText.observe(viewLifecycleOwner) { text ->
            if (binding.edRepSSKSub.text.toString() != text) {
                binding.edRepSSKSub.setText(text)
            }
        }
    }

    private fun setupToggleCheckbox(
        checkbox: CheckBox,
        autoInputLayout: View,
        manualInputLayout: View
    ) {
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                autoInputLayout.visibility = View.GONE
                manualInputLayout.visibility = View.VISIBLE
            } else {
                autoInputLayout.visibility = View.VISIBLE
                manualInputLayout.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
