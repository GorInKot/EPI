package com.example.epi.Fragments.FixingVolumes

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.DataBase.PlanValue.PlanValue
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
import kotlinx.coroutines.withContext
import kotlin.math.PI

class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
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
        private val TAG = "Tagg-FixingVolumesFragment"
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

        sharedViewModel.setSelectedComplex("")
        binding.AutoCompleteTextViewFixComplexOfWork.setText("", false)

        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на строки
        sharedViewModel.fixRows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
            // Лог для отладки
            android.util.Log.d(TAG, "Submitting list to adapter: $rows")
        }

        // Подписка на ошибки
        sharedViewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        // Обзервер на Комплекс работ
        // New Version
        sharedViewModel.controlsComplexOfWork.observe(viewLifecycleOwner) { complexOfWorks ->
            if(complexOfWorks.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, complexOfWorks)
                binding.AutoCompleteTextViewFixComplexOfWork.setAdapter(adapter)
                sharedViewModel.selectedComplex.value?.let { selected ->
                    val position = complexOfWorks.indexOf(selected)
                    if (position >= 0) {
                        binding.AutoCompleteTextViewFixComplexOfWork.setText(complexOfWorks[position], false)
                    }
                }
                binding.AutoCompleteTextViewFixComplexOfWork.setOnItemClickListener { parent, view, position, id ->
                    val selectedComplex = parent.getItemAtPosition(position) as String
                    sharedViewModel.setSelectedComplex(selectedComplex) // Обновляем комплекс и виды работ
                    checkComplexAndTypeOfWork(selectedComplex)
                }
                binding.AutoCompleteTextViewFixComplexOfWork.setOnClickListener {
                    binding.AutoCompleteTextViewFixComplexOfWork.showDropDown()
                }
            }else {
                Toast.makeText(requireContext(), "Не удалось загрузить список комплексов работ", Toast.LENGTH_SHORT).show()
            }
        }

        // Обзервер на Вид работ
        sharedViewModel.controlTypesOfWork.observe(viewLifecycleOwner) { typesOfWork ->
            if (typesOfWork.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typesOfWork)
                binding.AutoCompleteTextViewFixTypeOfWork.setAdapter(adapter)
                // Сбрасываем текст, если список обновился
                if (binding.AutoCompleteTextViewFixTypeOfWork.text.isNullOrEmpty()) {
                    binding.AutoCompleteTextViewFixTypeOfWork.setText("", false)
                }
                binding.AutoCompleteTextViewFixTypeOfWork.setOnItemClickListener { parent, view, position, id ->
                    val selectedType = parent.getItemAtPosition(position) as String
                    checkTypeOfWork(selectedType) // Проверяем наличие вида работ
                }
                binding.AutoCompleteTextViewFixTypeOfWork.setOnClickListener {
                    binding.AutoCompleteTextViewFixTypeOfWork.showDropDown()
                }
            } else {
                binding.AutoCompleteTextViewFixTypeOfWork.setText("", false) // Очищаем, если нет видов работ
                binding.AutoCompleteTextViewFixTypeOfWork.isEnabled = false // Дезактивируем, если список пуст
            }
        }
        binding.TextInputEditTextPlan.inputType = InputType.TYPE_NULL
        binding.TextInputEditTextPlan.keyListener = null

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
            val objectId = sharedViewModel.selectedObject.value.orEmpty()
            val newRow = FixVolumesRow(
                ID_object = objectId,
                complexOfWork = binding.AutoCompleteTextViewFixComplexOfWork.text.toString().trim(),
                projectWorkType = binding.AutoCompleteTextViewFixTypeOfWork.text.toString().trim(),
                measure = binding.AutoCompleteTextViewMeasureUnits.text.toString().trim(),
                plan = binding.TextInputEditTextPlan.text.toString().trim(),
                fact = binding.TextInputEditTextFact.text.toString().trim(),
                result = "" // result будет установлен в recalculateFixRows
            )

            when (val result = sharedViewModel.validateAndCalculateRemainingVolume(newRow)) {
                is RowValidationResult.Valid -> {
                    sharedViewModel.addFixRow(newRow)
                    clearInputFields()
                }

                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_LONG).show()
                    highlightErrorField(result.reason)
                }

                else -> {}
            }
        }

        // Кнопка "Сохранить"
        binding.btnNext.setOnClickListener {
            if (sharedViewModel.fixRows.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Добавьте хотя бы одну строку фиксации объемов", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                val updatedReportId = sharedViewModel.updateFixVolumesReport()
                if (updatedReportId > 0) {
                    val action = FixingVolumesFragmentDirections.actionFixVolumesFragmentToSendReportFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Ошибка при сохранении отчета", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_FixVolumesFragment_to_ControlFragment)
        }

        // Кнопка "Добавить плановые значения
        binding.btnAddPlanValues.setOnClickListener {
            val objectId = sharedViewModel.selectedObject.value ?: run {
                Toast.makeText(requireContext(), "Не выбран объект", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val action = FixingVolumesFragmentDirections.actionFixVolumesFragmentToAddPlanValue(objectId)
            findNavController().navigate(action)
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

        val editIdObject = dialogView.findViewById<TextInputEditText>(R.id.textInputLayout_dialog_id)
        val editComplexOfWork = dialogView.findViewById<AutoCompleteTextView>(R.id.textInputLayout_dialog_complexofwork)
        val editWorkProject = dialogView.findViewById<AutoCompleteTextView>(R.id.textInputLayout_dialog_worktype)
        val editMeasures = dialogView.findViewById<AutoCompleteTextView>(R.id.textInputLayout_dialog_measures)
        val editPlan = dialogView.findViewById<TextInputEditText>(R.id.textInputLayout_dialog_volumes)
        val editFact = dialogView.findViewById<TextInputEditText>(R.id.textInputLayout_dialog_fact)

        editIdObject.setText(row.ID_object)
        editIdObject.isEnabled = false // ID_object не редактируется
        editComplexOfWork.setText(row.complexOfWork)
        editWorkProject.setText(row.projectWorkType)
        editMeasures.setText(row.measure)
        editPlan.setText(row.plan)
        editFact.setText(row.fact)

        // Настройка автодополнения
        sharedViewModel.controlsComplexOfWork.value?.let { workTypeList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypeList)
            editWorkProject.setAdapter(adapter)
            editWorkProject.setOnClickListener {
                editWorkProject.showDropDown()
            }
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
                complexOfWork = row.complexOfWork,
                projectWorkType = editWorkProject.text.toString().trim(),
                measure = editMeasures.text.toString().trim(),
                plan = editPlan.text.toString().trim(),
                fact = editFact.text.toString().trim(),
                result = "" // result будет установлен в recalculateFixRows
            )

            when (val result = sharedViewModel.validateAndCalculateRemainingVolume(newRow, excludeRow = row)) {
                is RowValidationResult.Valid -> {
                    sharedViewModel.updateFixRow(oldRow = row, newRow = newRow)
                    Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                is RowValidationResult.Invalid -> {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_LONG).show()
                    highlightErrorField(result.reason, dialogView)
                }

                else -> {}
            }
        }

        dialog.show()
    }

    private fun highlightErrorField(reason: String, dialogView: View? = null) {
        val context = dialogView ?: binding.root
        when {
//            reason.contains("ID объекта") -> {
//                (context.findViewById<TextInputEditText>(R.id.edIdObject)
//                    ?: binding.TextInputEditTextIdObject)?.error = reason
//            }
            // TODO - Комплекс работ
            reason.contains("Комплекс работ") -> {
                (context.findViewById<AutoCompleteTextView>(R.id.textInputLayout_dialog_complexofwork)
                    ?: binding.AutoCompleteTextViewFixComplexOfWork)?.error = reason
            }
            reason.contains("Вид работ") -> {
                (context.findViewById<AutoCompleteTextView>(R.id.textInputLayout_dialog_worktype)
                    ?: binding.AutoCompleteTextViewFixTypeOfWork)?.error = reason
            }
            reason.contains("Единица измерения") -> {
                (context.findViewById<AutoCompleteTextView>(R.id.textInputLayout_dialog_measures)
                    ?: binding.AutoCompleteTextViewMeasureUnits)?.error = reason
            }
            reason.contains("План") -> {
                (context.findViewById<TextInputEditText>(R.id.textInputLayout_dialog_volumes)
                    ?: binding.TextInputEditTextPlan)?.error = reason
            }
            reason.contains("Факт") -> {
                (context.findViewById<TextInputEditText>(R.id.textInputLayout_dialog_fact)
                    ?: binding.TextInputEditTextFact)?.error = reason
            }
        }
    }

    private fun clearInputFields() {
        binding.AutoCompleteTextViewFixComplexOfWork.setText("")
        binding.AutoCompleteTextViewFixTypeOfWork.setText("")
        binding.AutoCompleteTextViewMeasureUnits.setText("")
        binding.TextInputEditTextPlan.setText("")
        binding.TextInputEditTextFact.setText("")
    }

    // Проверяем Объем и комплекс работ
    private suspend fun checkComplexExistence(complexOfWork: String, objectId: String): PlanValue? {
        return withContext(Dispatchers.IO) {
            val planValues = sharedViewModel.getPlanValuesByObjectIdAndComplex(objectId, complexOfWork)
                .filter { it.objectId == objectId && it.complexWork == complexOfWork }
            val result = planValues.firstOrNull()
            Log.d(TAG, "Checking Complex: objectId=$objectId, complexWork=$complexOfWork, result=$result")
            result
        }
    }

    // Проверяем полную комбинация: Объект, Комплекс, вид работ
    private suspend fun checkTypeOfWorkExistence(complexOfWork: String, typeOfWork: String, objectId: String): PlanValue? {
        return withContext(Dispatchers.IO) {
            val planValues = sharedViewModel.getPlanValuesByObjectIdAndComplexAndType(objectId, complexOfWork, typeOfWork)
            val result = planValues.firstOrNull()
            Log.d(TAG, "Checking TypeOfWork: objectId=$objectId, complexWork=$complexOfWork, typeOfWork=$typeOfWork, result=$result")
            result
        }
    }

    private fun checkComplexAndTypeOfWork(selectedComplex: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val objectId = sharedViewModel.selectedObject.value ?: run {
                Toast.makeText(requireContext(), "Объект не указан", Toast.LENGTH_SHORT).show()
                return@launch
            }
            Log.d(TAG, "Checking complex:\nobjectId=$objectId,\ncomplexWork=$selectedComplex")
            val planValue = checkComplexExistence(selectedComplex, objectId)
            Log.d(TAG, "PlanValue в checkComplexAndTypeOfWork:\nselectedComplex: ${selectedComplex}\nobjectId: ${objectId}")
            if (planValue == null) {
                showStyledAlertDialog(
                    title = "Ошибка",
                    message = "Комплекс работ '$selectedComplex' отсутствует для выбранного объекта, добавьте плановое значение",
                    positiveButtonText = "OK",
                    positiveAction = {
                        val action = FixingVolumesFragmentDirections.actionFixVolumesFragmentToAddPlanValue(objectId)
                        findNavController().navigate(action)
                    },
                    negativeButtonText = "Отмена",
                    negativeAction = { }
                )
            } else {
                // Активируем поле Вид работ и сбрасываем его значение
                binding.AutoCompleteTextViewFixTypeOfWork.setText("", false)
                binding.AutoCompleteTextViewFixTypeOfWork.isEnabled = true
            }
        }
    }

    // Проверяем Вид работ
    private fun checkTypeOfWork(selectedType: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val objectId = sharedViewModel.selectedObject.value ?: run {
                Toast.makeText(requireContext(), "Не выбран объект", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val selectedComplex = sharedViewModel.selectedComplex.value ?: run {
                Toast.makeText(requireContext(), "Комплекс работ не выбран", Toast.LENGTH_SHORT).show()
                return@launch
            }
            Log.d(TAG, "Checking TypeOfWork: objectId=$objectId, selectedComplex=$selectedComplex, selectedType=$selectedType")
            val planValue = checkTypeOfWorkExistence(selectedComplex, selectedType, objectId)
            Log.d(TAG, "PlanValue в checkTypeOfWork: selectedComplex=$selectedComplex, selectedType=$selectedType, objectId=$objectId, planValue=$planValue")
            if (planValue == null) {
                showStyledAlertDialog(
                    title = "Ошибка",
                    message = "Вид работ '$selectedType' отсутствует для комплекса '$selectedComplex', добавьте плановое значение",
                    positiveButtonText = "OK",
                    positiveAction = {
                        val action = FixingVolumesFragmentDirections.actionFixVolumesFragmentToAddPlanValue(objectId)
                        findNavController().navigate(action)
                    },
                    negativeButtonText = "Отмена",
                    negativeAction = { }
                )
            } else {
                // Загрузка единиц измерения и плана
                binding.TextInputEditTextPlan.setText(planValue.planValue.toString())
                binding.AutoCompleteTextViewMeasureUnits.setText(planValue.measures)
                binding.TextInputEditTextFact.requestFocus() // Фокус на поле Факт
            }
        }
    }

    private fun showStyledAlertDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        positiveAction: () -> Unit,
        negativeButtonText: String? = null,
        negativeAction: () -> Unit = {}
    ) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog, null)
        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.Theme_EPI)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = dialogBuilder.create()

        // Настройка элементов
        dialogView.findViewById<TextView>(R.id.alertTitle)?.text = title
        dialogView.findViewById<TextView>(R.id.alertMessage)?.text = message

        // Настройка кнопок
        dialogView.findViewById<Button>(R.id.positiveButton)?.apply {
            text = positiveButtonText // Устанавливаем переданный текст
            setOnClickListener {
                positiveAction()
                alertDialog.dismiss()
            }
        }
        negativeButtonText?.let { buttonText ->
            dialogView.findViewById<Button>(R.id.negativeButton)?.apply {
                visibility = View.VISIBLE
                text = buttonText // Устанавливаем переданный текст
                setOnClickListener {
                    negativeAction()
                    alertDialog.dismiss()
                }
            }
        } ?: run {
            dialogView.findViewById<Button>(R.id.negativeButton)?.visibility = View.GONE
        }

        // Настройка фона и размера
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Показываем диалог
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}