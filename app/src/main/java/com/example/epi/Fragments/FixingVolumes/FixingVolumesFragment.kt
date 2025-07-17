package com.example.epi.Fragments.FixingVolumes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentFixingVolumesBinding
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import android.net.Uri
import android.widget.AutoCompleteTextView

class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FixVolumesViewModel by activityViewModels()
    private lateinit var adapter: FixVolumesRowAdapter
    private var rowCount = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFixingVolumesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на строки
        viewModel.fixRows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

        // Подписка на списки автодополнения
        viewModel.fixWorkType.observe(viewLifecycleOwner) { workTypeList ->
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

        viewModel.fixMeasures.observe(viewLifecycleOwner) { measuresList ->
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
            val workType = binding.AutoCompleteTextViewWorkType.text.toString().trim()
            val measures = binding.AutoCompleteTextViewMeasureUnits.text.toString().trim()
            val plan = binding.TextInputEditTextPlan.text.toString().trim()
            val fact = binding.TextInputEditTextFact.text.toString().trim()

            if (workType.isBlank() || measures.isBlank() || plan.isBlank() || fact.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val planValue = plan.toDoubleOrNull()
            val factValue = fact.toDoubleOrNull()

            if (planValue == null || factValue == null) {
                Toast.makeText(requireContext(), "Некорректные значения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (factValue > planValue) {
                Toast.makeText(requireContext(), "Значение Факт не может превышать значение План", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = planValue - factValue

            val newRow = FixVolumesRow(
                ID_object = rowCount.toString(),
                projectWorkType = workType,
                measure = measures,
                plan = plan,
                fact = fact,
                result = result.toString()
            )

            viewModel.addFixRow(newRow)
            rowCount++

            binding.AutoCompleteTextViewWorkType.setText("")
            binding.AutoCompleteTextViewMeasureUnits.setText("")
            binding.TextInputEditTextPlan.setText("")
            binding.TextInputEditTextFact.setText("")
        }

        // Навигация
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.sendReportFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.controlFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = FixVolumesRowAdapter(
            onEditClick = { row, position ->
                showEditDialog(row, position)
            },
            onDeleteClick = { row ->
                viewModel.removeFixRow(row)
                Toast.makeText(requireContext(), "Строка удалена", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerViewFixVolumes.adapter = adapter
    }

    private fun showEditDialog(row: FixVolumesRow, position: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_fixvolumes_row, null)

        val editIdObject = dialogView.findViewById<TextInputEditText>(R.id.edIdObject)
//        val textInputLayoutWorkProject = dialogView.findViewById<TextInputLayout>(R.id.textInputLayoutWorkProject)
        val editWorkProject = dialogView.findViewById<AutoCompleteTextView>(R.id.textInputWorkProject)
//        val textInputLayoutMeasures = dialogView.findViewById<TextInputLayout>(R.id.textInputLayoutMeasures)
        val editMeasures = dialogView.findViewById<AutoCompleteTextView>(R.id.editMeasures)
        val editPlan = dialogView.findViewById<TextInputEditText>(R.id.editPlan)
        val editFact = dialogView.findViewById<TextInputEditText>(R.id.editFact)

        // Заполняем поля данными из FixVolumesRow
        editIdObject.setText(row.ID_object)
        editWorkProject.setText(row.projectWorkType)
        editMeasures.setText(row.measure)
        editPlan.setText(row.plan)
        editFact.setText(row.fact)

        // Настройка автодополнения
        viewModel.fixWorkType.value?.let { workTypeList ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypeList)
            editWorkProject.setAdapter(adapter)
            editWorkProject.setOnClickListener { editWorkProject.showDropDown() }
        }
        viewModel.fixMeasures.value?.let { measuresList ->
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
            val planValue = editPlan.text.toString().toDoubleOrNull()
            val factValue = editFact.text.toString().toDoubleOrNull()

            if (planValue == null || factValue == null) {
                Toast.makeText(requireContext(), "Некорректные значения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (factValue > planValue) {
                Toast.makeText(requireContext(), "Факт не может превышать План", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = planValue - factValue

            val updateFixRow = FixVolumesRow(
                ID_object = editIdObject.text.toString(),
                projectWorkType = editWorkProject.text.toString(),
                measure = editMeasures.text.toString(),
                plan = editPlan.text.toString(),
                fact = editFact.text.toString(),
                result = result.toString()
            )

            viewModel.updateFixRow(oldRow = row, newRow = updateFixRow)
            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // TODO - парсинг Excel (на всякий оставим)
    // Запуск выбора Excel-файла
//    private val excelPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val uri = result.data?.data
//            uri?.let {
//                parseExcelFile(it)
//            }
//        }
//    }
//
//    // Открытие Excel-файла
//    private fun openExcelFilePicker() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
//        }
//        excelPickerLauncher.launch(intent)
//    }
//
//    // Парсинг Excel-файла и заполнение EditText полей
//    private fun parseExcelFile(uri: Uri) {
//        try {
//            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
//            val workbook = XSSFWorkbook(inputStream)
//            val sheet = workbook.getSheetAt(0)
//
//            // Предполагаем, что данные в строке 23 (index = 22)
//            val row = sheet.getRow(22) ?: throw Exception("Нет строки с данными")
//
//            val workType = row.getCell(0)?.toString()?.trim() ?: ""
//            val measure = row.getCell(2)?.toString()?.trim() ?: ""
//            val plan = row.getCell(3)?.toString()?.trim() ?: ""
//            val fact = row.getCell(6)?.toString()?.trim() ?: ""
//
//            Log.d(
//                "FixingVolumesFragment",
//                "Получили Excel: 0 столбец: '$workType'," +
//                        "Получили 2 столбец: '$measure'," +
//                        "Получили 3 столбец: '$plan'," +
//                        "Получили 6 стобец: '$fact'"
//            )
//
//            Toast.makeText(requireContext(), "0 столбец: '$workType'", Toast.LENGTH_LONG).show()
//
//            binding.AutoCompleteTextViewWorkType.setText(workType)
//            binding.AutoCompleteTextViewMeasureUnits.setText(measure)
//            binding.TextInputEditTextPlan.setText(plan)
//            binding.TextInputEditTextFact.setText(fact)
//
//            Toast.makeText(requireContext(), "Данные из Excel загружены", Toast.LENGTH_SHORT).show()
//
//            workbook.close()
//            inputStream?.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "Ошибка чтения Excel: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}