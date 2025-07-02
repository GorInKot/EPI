package com.example.epi.Fragments.FixingVolumes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentFixingVolumesBinding
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.ViewModel.SharedViewModel
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream


class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()

    private var rowCount = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFixingVolumesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.fixRows.observe(viewLifecycleOwner) { rows ->
            binding.table.removeAllViews()
            addTableHeader()
            rows.forEach { row ->
                addRowToTable(row)
            }
        }

        viewModel.fixWorkType.observe(viewLifecycleOwner) { workTypeList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                workTypeList
            )
            binding.AutoCompleteTextViewWorkType.setAdapter(adapter)
            binding.AutoCompleteTextViewWorkType.setOnClickListener {
                binding.AutoCompleteTextViewWorkType.showDropDown()
            }
        }

        viewModel.fixMeasures.observe(viewLifecycleOwner) {measuresList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                measuresList
            )
            binding.AutoCompleteTextViewMeasureUnits.setAdapter(adapter)
            binding.AutoCompleteTextViewMeasureUnits.setOnClickListener {
                binding.AutoCompleteTextViewMeasureUnits.showDropDown()
            }
        }

//        binding.btnOpen.setOnClickListener {
//            openExcelFilePicker()
//        }

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


        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.sendReportFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.controlFragment)
        }
    }

    private fun addTableHeader() {
        val headerRow = TableRow(requireContext())
        headerRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val headers = listOf(
            "ID\nОбъекта",
            "Вид работ из проекта",
            "Единицы измерения",
            "Объем работ по проекту",
            "Выполненный объем работ",
            "Остаток\nпо объему",
            "\nДействия"
        )

        headers.forEach { title ->
            val textView = TextView(requireContext()).apply {
                text = title
                setPadding(8, 0, 8, 32)
                textSize = 18f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                setTextColor(ContextCompat.getColor(context, R.color.background))

                layoutParams = TableRow.LayoutParams(0, 120, 1f)
            }
            headerRow.addView(textView)
        }

        binding.table.addView(headerRow)
    }

    private fun addRowToTable(row: FixVolumesRow) {
        val tableRow = TableRow(requireContext())
        tableRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        fun createCell(text: String): TextView {
            return TextView(requireContext()).apply {
                this.text = text
                setPadding(8, 8, 8, 8)
                textSize = 18f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
        }

        val buttonContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = TableRow.LayoutParams(0, 150, 1f)
        }

        val deleteButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.delete_24)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 32
            }
            setOnClickListener {
                viewModel.removeFixRow(row)
                Toast.makeText(requireContext(),
                    "Строка удалена", Toast.LENGTH_SHORT).show()
            }
        }

        val editButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.edit_24)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 132
                marginEnd = 32
            }
            setOnClickListener {
                val cells = (0 until tableRow.childCount - 1).map { index ->
                    tableRow.getChildAt(index) as TextView
                }
                showEditDialog(row, cells)
            }
        }

        buttonContainer.addView(editButton)
        buttonContainer.addView(deleteButton)

//        tableRow.addView(createCell(row.ID_object))
        tableRow.addView(createCell(viewModel.selectedObject.value.toString()))
        tableRow.addView(createCell(row.projectWorkType))
        tableRow.addView(createCell(row.measure))
        tableRow.addView(createCell(row.plan))
        tableRow.addView(createCell(row.fact))
        tableRow.addView(createCell(row.result
        ))
        tableRow.addView(buttonContainer)

        binding.table.addView(tableRow)
    }


    // Запуск выбора Excel-файла
    private val excelPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                parseExcelFile(it)
            }
        }
    }

    // Открытие Excel-файла
    private fun openExcelFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
        }
        excelPickerLauncher.launch(intent)
    }

    // Парсинг Excel-файла и заполнение EditText полей
    private fun parseExcelFile(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Предполагаем, что данные в строке 2 (index = 1)
            val row = sheet.getRow(22) ?: throw Exception("Нет строки с данными")

            val workType = row.getCell(0)?.toString()?.trim() ?: ""
            val measure = row.getCell(2)?.toString()?.trim() ?: ""
            val plan = row.getCell(3)?.toString()?.trim() ?: ""
            val fact = row.getCell(6)?.toString()?.trim() ?: ""

            Log.d(
                "FixingVolumesFragment",
                "Получили Excel: 0 столбец: '$workType'," +
                        "Получили 2 столбец: '$measure'," +
                        "Получили 3 столбец: '$plan'," +
                        "Получили 6 стобец: '$fact'"
            )

            Toast.makeText(requireContext(),"0 столбец: '$workType'", Toast.LENGTH_LONG).show()


            binding.AutoCompleteTextViewWorkType.setText(workType)
            binding.AutoCompleteTextViewMeasureUnits.setText(measure)
            binding.TextInputEditTextPlan.setText(plan)
            binding.TextInputEditTextFact.setText(fact)

            Toast.makeText(requireContext(), "Данные из Excel загружены", Toast.LENGTH_SHORT).show()

            workbook.close()
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка чтения Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditDialog(row: FixVolumesRow, cells: List<TextView>) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_fixvolumes_row, null)

        val editIdObject = dialogView.findViewById<EditText>(R.id.edIdObject)
        val editWorkProject = dialogView.findViewById<EditText>(R.id.textInputWorkProject)
        val editMeasures = dialogView.findViewById<EditText>(R.id.editMeasures)
        val editPlan = dialogView.findViewById<EditText>(R.id.editPlan)
        val editFact = dialogView.findViewById<EditText>(R.id.editFact)

        // Заполняем поля значениями из ячеек таблицы
        editIdObject.setText(cells[0].text)
        editWorkProject.setText(cells[1].text)
        editMeasures.setText(cells[2].text)
        editPlan.setText(cells[3].text)
        editFact.setText(cells[4].text)

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

            val remainder = if (planValue != null && factValue != null) {
                (planValue - factValue).toString()
            } else {
                "0.0"
            }

            val updateFixRow = FixVolumesRow(
                ID_object = editIdObject.text.toString(),
                projectWorkType = editWorkProject.text.toString(),
                measure = editMeasures.text.toString(),
                plan = editPlan.text.toString(),
                fact = editFact.text.toString(),
                result = remainder
            )

            viewModel.updateFixRow(oldRow = row, newRow = updateFixRow)

            Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}