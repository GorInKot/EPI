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

        sharedViewModel.loadPreviousReport()

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
        // Обработчик чекбокса "Оборудование отсутствует"
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
            Toast.makeText(requireContext(), "Номер предписания сгенерирован", Toast.LENGTH_SHORT).show()
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
                sharedViewModel.selectedComplex.value?.let { selected ->
                    val position = complexOfWorks.indexOf(selected)
                    if (position >= 0) {
                        binding.AutoCompleteTextViewControlComplexOfWork.setText(complexOfWorks[position], false)
                    }
                }
                binding.AutoCompleteTextViewControlComplexOfWork.setOnItemClickListener { parent, view, position, id ->
                    val selectedComplex = parent.getItemAtPosition(position) as String
                    sharedViewModel.setSelectedComplex(selectedComplex) // Обновляем комплекс и виды работ
                }
                binding.AutoCompleteTextViewControlComplexOfWork.setOnClickListener {
                    binding.AutoCompleteTextViewControlComplexOfWork.showDropDown()
                }
            }else {
                Toast.makeText(requireContext(), "Не удалось загрузить список комплексов работ", Toast.LENGTH_SHORT).show()
            }
        }

        // Old Version
//        sharedViewModel.controlsComplexOfWork.observe(viewLifecycleOwner) { workComplexList ->
//            if (!workComplexList.isNullOrEmpty()) {
//                val adapter = ArrayAdapter(
//                    requireContext(),
//                    android.R.layout.simple_spinner_dropdown_item,
//                    workComplexList
//                )
//                binding.AutoCompleteTextViewControlComplexOfWork.setAdapter(adapter)
//                binding.AutoCompleteTextViewControlComplexOfWork.inputType = InputType.TYPE_NULL
//                binding.AutoCompleteTextViewControlComplexOfWork.keyListener = null
//                binding.AutoCompleteTextViewControlComplexOfWork.setOnClickListener {
//                    binding.AutoCompleteTextViewControlComplexOfWork.showDropDown()
//                }
//            }else {
//                Toast.makeText(requireContext(), "Не удалось загрузить список комплексов работ", Toast.LENGTH_SHORT).show()
//                Log.d(TAG, "Не удалось загрузить список комплексов работ")
//            }
//
//        }



        // Обзервер на Вид работ
        // New Version
        sharedViewModel.controlTypesOfWork.observe(viewLifecycleOwner) { typesOfWork ->
            if (typesOfWork.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typesOfWork)
                binding.AutoCompleteTextViewControlTypeOfWork.setAdapter(adapter)
                // Сбрасываем текст, если список обновился
                if (binding.AutoCompleteTextViewControlTypeOfWork.text.isNullOrEmpty()) {
                    binding.AutoCompleteTextViewControlTypeOfWork.setText("", false)
                }
                binding.AutoCompleteTextViewControlTypeOfWork.setOnClickListener { binding.AutoCompleteTextViewControlTypeOfWork.showDropDown() }
            } else {
                binding.AutoCompleteTextViewControlTypeOfWork.setText("", false) // Очищаем, если нет видов работ
            }
        }

        // Old Version
//        sharedViewModel.controlTypesOfWork.observe(viewLifecycleOwner) { workTypesList ->
//            if (!workTypesList.isNullOrEmpty()) {
//                val adapter = ArrayAdapter(
//                    requireContext(),
//                    android.R.layout.simple_spinner_dropdown_item,
//                    workTypesList
//                )
//                binding.AutoCompleteTextViewControlTypeOfWork.setAdapter(adapter)
//                binding.AutoCompleteTextViewControlTypeOfWork.inputType = InputType.TYPE_NULL
//                binding.AutoCompleteTextViewControlTypeOfWork.keyListener = null
//                binding.AutoCompleteTextViewControlTypeOfWork.setOnClickListener {
//                    binding.AutoCompleteTextViewControlTypeOfWork.showDropDown()
//                }
//            }else {
//                Toast.makeText(requireContext(), "Не удалось загрузить список видов работ", Toast.LENGTH_SHORT).show()
//                Log.d(TAG, "Не удалось загрузить список видов работ")
//            }
//
//        }

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
                            input.isEquipmentAbsent // Сохраняем состояние
                        )
                    )
                    clearInputFields()
                }
                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
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

        val editEquipment = dialogView.findViewById<EditText>(R.id.editEquipment)
        val editComplexOfWork = dialogView.findViewById<EditText>(R.id.editComplex)
        val editType = dialogView.findViewById<EditText>(R.id.editType)
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

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val updatedRow = ControlRow(
                equipmentName = editEquipment.text.toString(),
                complexOfWork = editComplexOfWork.text.toString(),
                typeOfWork = editType.text.toString(),
                orderNumber = editOrder.text.toString(),
                report = editReport.text.toString(),
                remarks = editRemarks.text.toString(),
                isEquipmentAbsent = row.isEquipmentAbsent // Сохраняем исходное состояние
            )
            sharedViewModel.updateRow(oldRow = row, newRow = updatedRow)
            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
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