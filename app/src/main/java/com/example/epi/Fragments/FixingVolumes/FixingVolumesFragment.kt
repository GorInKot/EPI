package com.example.epi.Fragments.FixingVolumes

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.R
import com.example.epi.RowValidationResult
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentFixingVolumesBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository
        )
    }
    private lateinit var adapter: FixVolumesRowAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFixingVolumesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка данных из последнего незавершенного отчета
        sharedViewModel.loadPreviousReport()

        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на строки
        sharedViewModel.fixRows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

        // Подписка на ошибки
        sharedViewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        // Подписка на списки автодополнения
        sharedViewModel.fixWorkType.observe(viewLifecycleOwner) { workTypeList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                workTypeList
            )
            binding.AutoCompleteTextViewWorkType.setAdapter(adapter)
            binding.AutoCompleteTextViewWorkType.inputType = android.text.InputType.TYPE_NULL
            binding.AutoCompleteTextViewWorkType.keyListener = null
            binding.AutoCompleteTextViewWorkType.setOnClickListener {
                binding.AutoCompleteTextViewWorkType.showDropDown()
            }
        }

        sharedViewModel.fixMeasures.observe(viewLifecycleOwner) { measuresList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                measuresList
            )
            binding.AutoCompleteTextViewMeasureUnits.setAdapter(adapter)
            binding.AutoCompleteTextViewMeasureUnits.inputType = android.text.InputType.TYPE_NULL
            binding.AutoCompleteTextViewMeasureUnits.keyListener = null
            binding.AutoCompleteTextViewMeasureUnits.setOnClickListener {
                binding.AutoCompleteTextViewMeasureUnits.showDropDown()
            }
        }

        // Добавить запись
        binding.btnAdd.setOnClickListener {
            val objectId = sharedViewModel.selectedObject.value.orEmpty() // Используем существующее поле
            val newRow = FixVolumesRow(
                ID_object = objectId,
                projectWorkType = binding.AutoCompleteTextViewWorkType.text.toString().trim(),
                measure = binding.AutoCompleteTextViewMeasureUnits.text.toString().trim(),
                plan = binding.TextInputEditTextPlan.text.toString().trim(),
                fact = binding.TextInputEditTextFact.text.toString().trim(),
                result = "" // Вычисляется в validateFixRowInput
            )

            when (val result = sharedViewModel.validateFixRowInput(newRow)) {
                is RowValidationResult.Valid -> {
                    val planValue = newRow.plan.toDoubleOrNull() ?: 0.0
                    val factValue = newRow.fact.toDoubleOrNull() ?: 0.0
                    val updatedRow = newRow.copy(result = (planValue - factValue).toString())
                    sharedViewModel.addFixRow(updatedRow)
                    clearInputFields()
                }
                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }

        // Кнопка "Сохранить"
        binding.btnNext.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val updatedReportId = sharedViewModel.updateFixVolumesReport()
                if (updatedReportId > 0) {
                    val action = FixingVolumesFragmentDirections.actionFixVolumesFragmentToSendReportFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_fixVolumesFragment_to_controlFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = FixVolumesRowAdapter(
            onEditClick = { row, position ->
                showEditDialog(row, position)
            },
            onDeleteClick = { row ->
                sharedViewModel.removeFixRow(row)
                Toast.makeText(requireContext(), "Строка удалена", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewFixVolumes.adapter = adapter
    }

    private fun showEditDialog(row: FixVolumesRow, position: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_fixvolumes_row, null)

        val editIdObject = dialogView.findViewById<TextInputEditText>(R.id.edIdObject)
        val editWorkProject = dialogView.findViewById<AutoCompleteTextView>(R.id.textInputWorkProject)
        val editMeasures = dialogView.findViewById<AutoCompleteTextView>(R.id.editMeasures)
        val editPlan = dialogView.findViewById<TextInputEditText>(R.id.editPlan)
        val editFact = dialogView.findViewById<TextInputEditText>(R.id.editFact)

        editIdObject.setText(row.ID_object)
        editIdObject.isEnabled = false // ID_object не редактируется
        editWorkProject.setText(row.projectWorkType)
        editMeasures.setText(row.measure)
        editPlan.setText(row.plan)
        editFact.setText(row.fact)

        // Настройка автодополнения
        sharedViewModel.fixWorkType.value?.let { workTypeList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypeList)
            editWorkProject.setAdapter(adapter)
            editWorkProject.setOnClickListener { editWorkProject.showDropDown() }
        }
        sharedViewModel.fixMeasures.value?.let { measuresList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, measuresList)
            editMeasures.setAdapter(adapter)
            editMeasures.setOnClickListener { editMeasures.showDropDown() }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnFixCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnFixSave).setOnClickListener {
            val newRow = FixVolumesRow(
                ID_object = row.ID_object,
                projectWorkType = editWorkProject.text.toString().trim(),
                measure = editMeasures.text.toString().trim(),
                plan = editPlan.text.toString().trim(),
                fact = editFact.text.toString().trim(),
                result = ""
            )

            when (val result = sharedViewModel.validateFixRowInput(newRow)) {
                is RowValidationResult.Valid -> {
                    val planValue = newRow.plan.toDoubleOrNull() ?: 0.0
                    val factValue = newRow.fact.toDoubleOrNull() ?: 0.0
                    val updatedRow = newRow.copy(result = (planValue - factValue).toString())
                    sharedViewModel.updateFixRow(oldRow = row, newRow = updatedRow)
                    Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }

        dialog.show()
    }

    private fun clearInputFields() {
        binding.AutoCompleteTextViewWorkType.setText("")
        binding.AutoCompleteTextViewMeasureUnits.setText("")
        binding.TextInputEditTextPlan.setText("")
        binding.TextInputEditTextFact.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}