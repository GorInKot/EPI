package com.example.epi.Fragments.Control

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.R
import com.example.epi.databinding.FragmentControlBinding
import com.example.epi.App
import com.example.epi.RowValidationResult
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
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

    companion object {
        private val TAG = "Tagg-ControlFragment"
    }

    private lateinit var adapter: ControlRowAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.setSelectedComplex("")
        binding.AutoCompleteTextViewControlComplexOfWork.setText("", false)


        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на номер предписания
        sharedViewModel.orderNumber.observe(viewLifecycleOwner) {
            binding.tvOrderNumber.text = it
        }

        // Подписка на чекбокс
        sharedViewModel.isViolation.observe(viewLifecycleOwner) { isChecked ->
            binding.btnOrderNumber.isEnabled = !isChecked
        }

        // Подписка на строки
        sharedViewModel.controlRows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

        // Подписка на дату и время
        sharedViewModel.currentDate.observe(viewLifecycleOwner) {
            binding.tvDate.text = "Дата: $it"
        }

        // Чекбокс Комплекс работ
        binding.checkBoxManualType.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setViolation(isChecked)
        }

        // Чекбокс Оборудование отсутствует
        binding.checkBoxManualEquipmentName.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setEquipmentAbsent(isChecked)
        }

        // Подписка на состояние чекбокса "Оборудование отсутствует"
        sharedViewModel.isEquipmentAbsent.observe(viewLifecycleOwner) { isChecked ->
            binding.AutoCompleteTextViewEquipmentName.isEnabled = !isChecked
            if (isChecked) {
                binding.AutoCompleteTextViewEquipmentName.setText("Оборудование отсутствует")
            } else {
                binding.AutoCompleteTextViewEquipmentName.setText("")
            }
        }

        binding.btnOrderNumber.setOnClickListener {
            sharedViewModel.generateOrderNumber()
        }

        // Подписка на списки для AutoCompleteTextView
        sharedViewModel.equipmentNames.observe(viewLifecycleOwner) { equipmentList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, equipmentList)
            binding.AutoCompleteTextViewEquipmentName.setAdapter(adapter)
            binding.AutoCompleteTextViewEquipmentName.inputType = InputType.TYPE_NULL
            binding.AutoCompleteTextViewEquipmentName.keyListener = null
            binding.AutoCompleteTextViewEquipmentName.setOnClickListener {
                binding.AutoCompleteTextViewEquipmentName.showDropDown()
            }
        }


        // Обзервер на Комплекс работ
        // New Version
        sharedViewModel.controlsComplexOfWork.observe(viewLifecycleOwner) { complexOfWorks ->
            if(complexOfWorks.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, complexOfWorks)
                binding.AutoCompleteTextViewControlComplexOfWork.setAdapter(adapter)
                binding.AutoCompleteTextViewControlComplexOfWork.inputType = InputType.TYPE_NULL
                binding.AutoCompleteTextViewControlComplexOfWork.keyListener = null
                sharedViewModel.selectedComplex.value?.let { selected ->
                    val position = complexOfWorks.indexOf(selected)
                    if (position >= 0) {
                        binding.AutoCompleteTextViewControlComplexOfWork.setText(complexOfWorks[position], false)
                    }
                }
                binding.AutoCompleteTextViewControlComplexOfWork.setOnItemClickListener { parent, view, position, id ->
                    val selectedComplex = parent.getItemAtPosition(position) as String
                    sharedViewModel.setSelectedComplex(selectedComplex) // Обновляем комплекс и виды работ
                    binding.AutoCompleteTextViewControlTypeOfWork.setText("", false) // Очищаем поле "Вид работ"
                }
                binding.AutoCompleteTextViewControlComplexOfWork.setOnClickListener {
                    binding.AutoCompleteTextViewControlComplexOfWork.showDropDown()
                }
            }else {
                Toast.makeText(requireContext(), "Не удалось загрузить список комплексов работ", Toast.LENGTH_SHORT).show()
            }
        }

        // Обзервер на Вид работ
        // New Version
        sharedViewModel.controlTypesOfWork.observe(viewLifecycleOwner) { typesOfWork ->
            if (typesOfWork.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typesOfWork)
                binding.AutoCompleteTextViewControlTypeOfWork.setAdapter(adapter)
                binding.AutoCompleteTextViewControlTypeOfWork.inputType = InputType.TYPE_NULL
                binding.AutoCompleteTextViewControlTypeOfWork.keyListener = null
                // Сбрасываем текст, если список обновился
                if (binding.AutoCompleteTextViewControlTypeOfWork.text.isNullOrEmpty()) {
                    binding.AutoCompleteTextViewControlTypeOfWork.setText("", false)
                }
                binding.AutoCompleteTextViewControlTypeOfWork.setOnClickListener { binding.AutoCompleteTextViewControlTypeOfWork.showDropDown() }
            } else {
                binding.AutoCompleteTextViewControlTypeOfWork.setText("", false) // Очищаем, если нет видов работ
            }
        }

        // Добавить строку
        binding.btnAddRow.setOnClickListener {
            val input = RowInput(
                equipmentName = if (sharedViewModel.isEquipmentAbsent.value == true) "Оборудование отсутствует" else binding.AutoCompleteTextViewEquipmentName.text.toString().trim(),
                complexOfWork = binding.AutoCompleteTextViewControlComplexOfWork.text.toString().trim(),
                typeOfWork = binding.AutoCompleteTextViewControlTypeOfWork.text.toString().trim(),
                orderNumber = binding.tvOrderNumber.text.toString().trim(),
                report = binding.InputEditTextReport.text.toString().trim(),
                remarks = binding.InputEditTextRemarks.text.toString().trim(),
                isViolationChecked = binding.checkBoxManualType.isChecked,
                isEquipmentAbsent = sharedViewModel.isEquipmentAbsent.value == true
            )

            when (val result = sharedViewModel.validateRowInput(input)) {
                is RowValidationResult.Valid -> {
                    sharedViewModel.addRow(
                        ControlRow(
                            input.equipmentName,
                            input.complexOfWork,
                            input.typeOfWork,
                            input.orderNumber,
                            input.report,
                            input.remarks,
                            input.isEquipmentAbsent, // Сохраняем состояние
                            input.isViolationChecked

                        )
                    )
                    clearInputFields()
                }
                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
            binding.AutoCompleteTextViewControlComplexOfWork.setText("", false)
        }


        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val updatedReportId = sharedViewModel.updateControlReport()
                if (updatedReportId > 0) {
                    val action = ControlFragmentDirections.actionControlFragmentToFixVolumesFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_ControlFragment_to_TransportFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = ControlRowAdapter(
            onEditClick = { row, position ->
                showEditDialog(row, position)
            },
            onDeleteClick = { row ->
                sharedViewModel.removeRow(row)
                Toast.makeText(requireContext(), "Строка удалена", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewControl.adapter = adapter
    }

    private fun showEditDialog(row: ControlRow, position: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_row, null)

        val editEquipment = dialogView.findViewById<AutoCompleteTextView>(R.id.editEquipment)
        val editComplexOfWork = dialogView.findViewById<AutoCompleteTextView>(R.id.editComplex)
        val editType = dialogView.findViewById<AutoCompleteTextView>(R.id.editType)
        val editOrder = dialogView.findViewById<EditText>(R.id.editOrderNumber)
        val editReport = dialogView.findViewById<EditText>(R.id.editReport)
        val editRemarks = dialogView.findViewById<EditText>(R.id.editRemarks)

        // Заполняем поля данными из ControlRow
        editEquipment.setText(row.equipmentName)
        editComplexOfWork.setText(row.complexOfWork)
        editType.setText(row.typeOfWork)
        editOrder.setText(row.orderNumber)
        editReport.setText(row.report)
        editRemarks.setText(row.remarks)

        // Отключаем поле оборудования, если isEquipmentAbsent == true
        editEquipment.isEnabled = !row.isEquipmentAbsent

        // Настройка адаптера для оборудования
        sharedViewModel.equipmentNames.value?.let { equipmentList ->
            if (equipmentList.isNotEmpty()) {
                val equipmentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, equipmentList)
                editEquipment.setAdapter(equipmentAdapter)
                editEquipment.inputType = InputType.TYPE_NULL
                editEquipment.keyListener = null
                editEquipment.setOnClickListener {
                    if (editEquipment.isEnabled) {
                        editEquipment.showDropDown()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Список оборудования пуст", Toast.LENGTH_SHORT).show()
            }
        }

        // Настройка адаптера для комплекса работ
        sharedViewModel.controlsComplexOfWork.value?.let { complexOfWorks ->
            if (complexOfWorks.isNotEmpty()) {
                val complexAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, complexOfWorks)
                editComplexOfWork.setAdapter(complexAdapter)
                editComplexOfWork.inputType = InputType.TYPE_NULL
                editComplexOfWork.keyListener = null
                editComplexOfWork.setOnClickListener {
                    editComplexOfWork.showDropDown()
                }
                editComplexOfWork.setOnItemClickListener { parent, _, position, _ ->
                    val selectedComplex = parent.getItemAtPosition(position) as String
                    sharedViewModel.setSelectedComplex(selectedComplex) // Обновляем комплекс и виды работ
                    editType.setText("", false) // Очищаем поле "Тип работы"
                }
            } else {
                Toast.makeText(requireContext(), "Список комплексов работ пуст", Toast.LENGTH_SHORT).show()
            }
        }

        // Настройка адаптера для типа работы
        sharedViewModel.controlTypesOfWork.value?.let { typesOfWork ->
            if (typesOfWork.isNotEmpty()) {
                val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typesOfWork)
                editType.setAdapter(typeAdapter)
                editType.inputType = InputType.TYPE_NULL
                editType.keyListener = null
                editType.setOnClickListener {
                    editType.showDropDown()
                }
            } else {
                editType.setText("", false) // Очищаем, если нет видов работ
                Toast.makeText(requireContext(), "Список типов работ пуст", Toast.LENGTH_SHORT).show()
            }
        }

        // Подписка на обновление списка типов работ
        sharedViewModel.controlTypesOfWork.observe(viewLifecycleOwner) { typesOfWork ->
            if (typesOfWork.isNotEmpty()) {
                val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typesOfWork)
                editType.setAdapter(typeAdapter)
                if (editType.text.toString().isEmpty()) {
                    editType.setText("", false)
                }
            } else {
                editType.setText("", false)
                Toast.makeText(requireContext(), "Список типов работ пуст", Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val updatedRow = ControlRow(
                equipmentName = editEquipment.text.toString().trim(),
                complexOfWork = editComplexOfWork.text.toString().trim(),
                typeOfWork = editType.text.toString().trim(),
                orderNumber = editOrder.text.toString().trim(),
                report = editReport.text.toString().trim(),
                remarks = editRemarks.text.toString().trim(),
                isEquipmentAbsent = row.isEquipmentAbsent // Сохраняем исходное состояние
            )
            when (val result = sharedViewModel.validateRowInput(
                RowInput(
                    equipmentName = updatedRow.equipmentName,
                    complexOfWork = updatedRow.complexOfWork,
                    typeOfWork = updatedRow.typeOfWork,
                    orderNumber = updatedRow.orderNumber,
                    report = updatedRow.report,
                    remarks = updatedRow.remarks,
                    isViolationChecked = row.isViolation,
                    isEquipmentAbsent = row.isEquipmentAbsent
                )
            )) {
                is RowValidationResult.Valid -> {
                    sharedViewModel.updateRow(oldRow = row, newRow = updatedRow)
                    Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun clearInputFields() {
        if (sharedViewModel.isEquipmentAbsent.value != true) {
            binding.AutoCompleteTextViewEquipmentName.setText("")
            }
        binding.AutoCompleteTextViewControlTypeOfWork.setText("")
        binding.InputEditTextReport.setText("")
        binding.InputEditTextRemarks.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}