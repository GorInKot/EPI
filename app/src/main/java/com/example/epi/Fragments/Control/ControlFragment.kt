package com.example.epi.Fragments.Control

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.epi.App
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.R
import com.example.epi.ViewModel.RowValidationResult
import com.example.epi.databinding.FragmentControlBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        setupRecyclerView()
        setupObservers()
        setupListeners()
        viewModel.loadPreviousReport()
    }

    private fun setupRecyclerView() {
        adapter = ControlRowAdapter(
            onEditClick = { row, position -> showEditDialog(row, position) },
            onDeleteClick = { row ->
                viewModel.removeRow(row)
                Toast.makeText(requireContext(), "Строка удалена", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewControl.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.orderNumber.observe(viewLifecycleOwner) {
            binding.tvOrderNumber.text = it
        }

        viewModel.isViolation.observe(viewLifecycleOwner) { isChecked ->
            binding.btnOrderNumber.isEnabled = !isChecked
            binding.checkBoxManualType.isChecked = isChecked
        }

        viewModel.controlRows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

        viewModel.currentDate.observe(viewLifecycleOwner) {
            binding.tvDate.text = "Дата: $it"
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.checkBoxManualType.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setViolation(isChecked)
        }

        binding.btnOrderNumber.setOnClickListener {
            viewModel.generateOrderNumber()
        }

        binding.btnAddRow.setOnClickListener {
            addNewRow()
        }

        binding.btnNext.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val reportId = viewModel.updateControlReport()
                if (reportId > 0) {
                    findNavController().navigate(R.id.action_controlFragment_to_fixVolumesFragment)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_controlFragment_to_transportFragment)
        }
    }

    private fun addNewRow() {
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

    private fun showEditDialog(row: ControlRow, position: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_row, null)

        with(dialogView) {
            findViewById<EditText>(R.id.edEquipment).setText(row.equipmentName)
            findViewById<EditText>(R.id.textInputType).setText(row.workType)
            findViewById<EditText>(R.id.editOrderNumber).setText(row.orderNumber)
            findViewById<EditText>(R.id.editReport).setText(row.report)
            findViewById<EditText>(R.id.editRemarks).setText(row.remarks)
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
                equipmentName = dialogView.findViewById<EditText>(R.id.edEquipment).text.toString(),
                workType = dialogView.findViewById<EditText>(R.id.textInputType).text.toString(),
                orderNumber = dialogView.findViewById<EditText>(R.id.editOrderNumber).text.toString(),
                report = dialogView.findViewById<EditText>(R.id.editReport).text.toString(),
                remarks = dialogView.findViewById<EditText>(R.id.editRemarks).text.toString()
            )
            viewModel.updateRow(oldRow = row, newRow = updatedRow)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun clearInputFields() {
        binding.AutoCompleteTextViewEquipmentName.text?.clear()
        binding.AutoCompleteTextViewType.text?.clear()
        binding.InputEditTextReport.text?.clear()
        binding.InputEditTextRemarks.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}