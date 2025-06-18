package com.example.epi.Fragments.Arrangement

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentArrangmentBinding
import java.text.SimpleDateFormat
import java.util.*

class ArrangementFragment : Fragment() {
    private var _binding: FragmentArrangmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentArrangmentBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка даты и времени
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.AttrFrTvDate.text = "Дата: $currentDate"
        binding.AttrFrTvTime.text = "Время: $currentTime"

        // <!-- Левый блок -->

        // Режим работы
        val workTypesList = listOf("Вахта", "Стандартный", "Суммированный")
        val workTypeAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, workTypesList)

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
        val customerList = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3", "Заказчик 4", "Заказчик 5")
        val customerListAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, customerList)

        binding.autoCompleteCustomer.setAdapter(customerListAdapter)

        binding.autoCompleteCustomer.setOnTouchListener { v, event ->
            binding.autoCompleteCustomer.showDropDown()
            false
        }
        binding.autoCompleteCustomer.setOnItemClickListener { parent, view, position, id ->
            val selectedWorkType = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedWorkType", Toast.LENGTH_SHORT).show()
        }

        // Заказчик: обработка CheckBox
        binding.checkBoxManualCustomer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoCustomer.visibility = View.GONE
                binding.textInputLayoutManualCustomer.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoCustomer.visibility = View.VISIBLE
                binding.textInputLayoutManualCustomer.visibility = View.GONE
            }
        }

        // Объект
        val objectList = listOf("Объект 1", "Объект 2", "Объект 3", "Объект 4", "Объект 5")
        val objectListAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, objectList)

        binding.autoCompleteObject.setAdapter(objectListAdapter)

        binding.autoCompleteObject.setOnTouchListener { v, event ->
            binding.autoCompleteObject.showDropDown()
            false
        }
        binding.autoCompleteObject.setOnItemClickListener { parent, view, position, id ->
            val selectedWorkType = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedWorkType", Toast.LENGTH_SHORT).show()
        }

        // Объект: обработка CheckBox
        binding.checkBoxManualObject.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoObject.visibility = View.GONE
                binding.textInputLayoutManualObject.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoObject.visibility = View.VISIBLE
                binding.textInputLayoutManualObject.visibility = View.GONE
            }
        }

        // TODO - Участок
        val inputTextPlot = binding.edPlot.text!!.toString().trim()
        if (inputTextPlot.isNotEmpty()) {
            Log.d("TextUnput", "Введено: $inputTextPlot")
        }

        // Генподрядчик
        val contractorList = listOf("Генподрядчик 1", "Генподрядчик 2", "Генподрядчик 3", "Генподрядчик 4", "Генподрядчик 5")
        val contractorListAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, contractorList)

        binding.autoCompleteContractor.setAdapter(contractorListAdapter)

        binding.autoCompleteContractor.setOnTouchListener { v, event ->
            binding.autoCompleteContractor.showDropDown()
            false
        }
        binding.autoCompleteContractor.setOnItemClickListener { parent, view, position, id ->
            val selectedWorkType = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedWorkType", Toast.LENGTH_SHORT).show()
        }

        // Генподрядчик: обработка CheckBox
        binding.checkBoxManualContractor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoContractor.visibility = View.GONE
                binding.textInputLayoutManualContractor.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoContractor.visibility = View.VISIBLE
                binding.textInputLayoutManualContractor.visibility = View.GONE
            }
        }

        // Кнопка "Копия предыдущего отчета"
        binding.AttrFrBtnCopy.setOnClickListener {
            Toast.makeText(requireContext(),"Пока ничего не происходит",Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Очистить"
        binding.AttrFrBtnClear.setOnClickListener {
            // Режим работы
            binding.autoCompleteWorkType.text!!.clear()

            // Заказчик
            binding.autoCompleteCustomer.text!!.clear()
            binding.edManualCustomer.text!!.clear()

            // Объект
            binding.autoCompleteObject.text!!.clear()
            binding.edManualObject.text!!.clear()

            // Участок
            binding.edPlot.text!!.clear()

            // Генподрядчик
            binding.autoCompleteContractor.text!!.clear()
            binding.edManualContractor.text!!.clear()

            // Представитель генподрядчика
            binding.autoCompleteSubContractor.text!!.clear()
            binding.edManualSubContractor.text!!.clear()

            // Представитель ССК ПО (ГП)
            binding.edRepSSKGp.text!!.clear()

            // Субподрядчик
            binding.edSubcontractor.text!!.clear()

            // Представитель Субподрядчика
            binding.edRepSubcontractor.text!!.clear()

            // Представитель ССУ ПО (Суб)
            binding.edRepSSKSub.text!!.clear()

            binding.checkBoxManualCustomer.isChecked = false
            binding.checkBoxManualObject.isChecked = false
            binding.checkBoxManualContractor.isChecked = false
            binding.checkBoxManualSubContractor.isChecked = false
        }

        // <!-- Правый блок -->

        // Представитель Генподрядчика
        val subContractorList = listOf("Представитель Генподрядчика 1", "Представитель Генподрядчика 2", "Представитель Генподрядчика 3", "Представитель Генподрядчика 4", "Представитель Генподрядчика 5")
        val subContractorListAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, subContractorList)

        binding.autoCompleteSubContractor.setAdapter(subContractorListAdapter)

        binding.autoCompleteSubContractor.setOnTouchListener { v, event ->
            binding.autoCompleteSubContractor.showDropDown()
            false
        }
        binding.autoCompleteSubContractor.setOnItemClickListener { parent, view, position, id ->
            val selectedWorkType = parent.getItemAtPosition(position).toString()
            Toast.makeText(requireContext(),  "Вы выбрали: $selectedWorkType", Toast.LENGTH_SHORT).show()
        }

        // Генподрядчик: обработка CheckBox
        binding.checkBoxManualSubContractor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoSubContractor.visibility = View.GONE
                binding.textInputLayoutManualSybContractor.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoSubContractor.visibility = View.VISIBLE
                binding.textInputLayoutManualSybContractor.visibility = View.GONE
            }
        }

        // Представитель ССК ПО (ГП)
        // TODO - Представитель ССК ПО (ГП)
        val inputTextRepSSKGp = binding.edRepSSKGp.text!!.toString().trim()
        if (inputTextRepSSKGp.isNotEmpty()) {
            Log.d("TextUnput", "Введено: $inputTextRepSSKGp")
        }

        // Субподрядчик
        // TODO - Субподрядчик
        val inputTextSubConstractor = binding.edSubcontractor.text!!.toString().trim()
        if (inputTextSubConstractor.isNotEmpty()) {
            Log.d("TextUnput", "Введено: $inputTextSubConstractor")
        }

        // Представитель Субподрядчика
        // TODO - Представитель Субподрядчика
        val inputTextRepSubConstractor = binding.edRepSubcontractor.text!!.toString().trim()
        if (inputTextRepSubConstractor.isNotEmpty()) {
            Log.d("TextUnput", "Введено: $inputTextRepSubConstractor")
        }

        // Представитель ССК ПО (Суб)
        // TODO - Представитель ССК ПО (Суб)
        val inputTextRepSSKSub = binding.edRepSSKSub.text!!.toString().trim()
        if (inputTextRepSSKSub.isNotEmpty()) {
            Log.d("TextUnput", "Введено: $inputTextRepSSKSub")
        }

        // Кнопка "Далее"
        binding.AttrFrBtnNext.setOnClickListener {
            findNavController().navigate(R.id.transportFragment)
        }
        // Кнопка "Назад"
        binding.AttrFrrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
