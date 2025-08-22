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
            requireActivity().applicationContext
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

        // Чекбокс
        binding.checkBoxManualType.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.setViolation(isChecked)
        }

        // Получить номер предписания
        binding.btnOrderNumber.setOnClickListener {
            sharedViewModel.generateOrderNumber()
//            Toast.makeText(requireContext(), "Номер предписания сгенерирован", Toast.LENGTH_SHORT).show()
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

        sharedViewModel.controlsWorkTypes.observe(viewLifecycleOwner) { workTypesList ->
            if (!workTypesList.isNullOrEmpty()) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    workTypesList
                )
                binding.AutoCompleteTextViewType.setAdapter(adapter)
                binding.AutoCompleteTextViewType.inputType = InputType.TYPE_NULL
                binding.AutoCompleteTextViewType.keyListener = null
                binding.AutoCompleteTextViewType.setOnClickListener {
                    binding.AutoCompleteTextViewType.showDropDown()
                }
            }else {
                Toast.makeText(requireContext(), "Не удалось загрузить список видов работ", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Не удалось загрузить список видов работ")
            }

        }

        // Добавить строку
        binding.btnAddRow.setOnClickListener {
            val input = RowInput(
                equipmentName = binding.AutoCompleteTextViewEquipmentName.text.toString().trim(),
                workType = binding.AutoCompleteTextViewType.text.toString().trim(),
                orderNumber = binding.tvOrderNumber.text.toString().trim(),
                report = binding.InputEditTextReport.text.toString().trim(),
                remarks = binding.InputEditTextRemarks.text.toString().trim(),
                isViolationChecked = binding.checkBoxManualType.isChecked
            )

            when (val result = sharedViewModel.validateRowInput(input)) {
                is RowValidationResult.Valid -> {
                    sharedViewModel.addRow(
                        ControlRow(
                            input.equipmentName,
                            input.workType,
                            input.orderNumber,
                            input.report,
                            input.remarks
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

        val editEquipment = dialogView.findViewById<EditText>(R.id.edEquipment)
        val editType = dialogView.findViewById<EditText>(R.id.textInputType)
        val editOrder = dialogView.findViewById<EditText>(R.id.editOrderNumber)
        val editReport = dialogView.findViewById<EditText>(R.id.editReport)
        val editRemarks = dialogView.findViewById<EditText>(R.id.editRemarks)

        // Заполняем поля данными из ControlRow
        editEquipment.setText(row.equipmentName)
        editType.setText(row.workType)
        editOrder.setText(row.orderNumber)
        editReport.setText(row.report)
        editRemarks.setText(row.remarks)

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
                workType = editType.text.toString(),
                orderNumber = editOrder.text.toString(),
                report = editReport.text.toString(),
                remarks = editRemarks.text.toString()
            )
            sharedViewModel.updateRow(oldRow = row, newRow = updatedRow)
            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun clearInputFields() {
        binding.AutoCompleteTextViewEquipmentName.setText("")
        binding.AutoCompleteTextViewType.setText("")
        binding.InputEditTextReport.setText("")
        binding.InputEditTextRemarks.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}