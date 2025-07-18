package com.example.epi.Fragments.Control

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.R
import com.example.epi.ViewModel.RowValidationResult
import com.example.epi.databinding.FragmentControlBinding
import androidx.navigation.fragment.navArgs
import com.example.epi.App
import com.example.epi.Fragments.Transport.TransportViewModel
import com.example.epi.Fragments.Transport.TransportViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ControlViewModel by viewModels {
        ControlViewModelFactory((requireActivity().application as App).reportRepository)
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


        viewModel.loadPreviousReport()



        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на номер предписания
        viewModel.orderNumber.observe(viewLifecycleOwner) {
            binding.tvOrderNumber.text = it
        }

        // Подписка на чекбокс
        viewModel.isViolation.observe(viewLifecycleOwner) { isChecked ->
            binding.btnOrderNumber.isEnabled = !isChecked
        }

        // Подписка на строки
        viewModel.controlRow.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

        // Подписка на дату и время
        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.tvDate.text = "Дата: $it"
        }

        // Чекбокс
        binding.checkBoxManualType.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setViolation(isChecked)
        }

        // Получить номер предписания
        binding.btnOrderNumber.setOnClickListener {
            viewModel.generateOrderNumber()
//            Toast.makeText(requireContext(), "Номер предписания сгенерирован", Toast.LENGTH_SHORT).show()
        }

        // Подписка на списки для AutoCompleteTextView
        viewModel.equipmentNames.observe(viewLifecycleOwner) { equipmentList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, equipmentList)
            binding.AutoCompleteTextViewEquipmentName.setAdapter(adapter)
            binding.AutoCompleteTextViewEquipmentName.setOnClickListener {
                binding.AutoCompleteTextViewEquipmentName.showDropDown()
            }
        }

        viewModel.controlWorkTypes.observe(viewLifecycleOwner) { workTypesList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                workTypesList
            )
            binding.AutoCompleteTextViewType.setAdapter(adapter)
            binding.AutoCompleteTextViewType.setOnClickListener {
                binding.AutoCompleteTextViewType.showDropDown()
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

            when (val result = viewModel.validateRowInput(input)) {
                is RowValidationResult.Valid -> {
                    viewModel.addRow(
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
            }
        }

        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            // Запускаем корутину для вызова suspend функции updateControlReport
            CoroutineScope(Dispatchers.Main).launch {
                val reportId = viewModel.updateControlReport()
                if (reportId > 0) {
                    // Успешно сохранено, переходим к следующему фрагменту
                    findNavController().navigate(R.id.action_controlFragment_to_fixVolumesFragment)
                } else {
                    // Ошибка уже отображается через errorEvent, дополнительно ничего не делаем
                }
            }
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_controlFragment_to_transportFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = ControlRowAdapter(
            onEditClick = { row, position ->
                showEditDialog(row, position)
            },
            onDeleteClick = { row ->
                viewModel.removeRow(row)
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
            viewModel.updateRow(oldRow = row, newRow = updatedRow)
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