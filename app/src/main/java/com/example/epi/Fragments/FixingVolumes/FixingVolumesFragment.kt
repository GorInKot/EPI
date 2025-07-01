package com.example.epi.Fragments.FixingVolumes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
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
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

import com.google.android.material.textfield.MaterialAutoCompleteTextView


class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FixVolumesViewModel

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

        viewModel = ViewModelProvider(requireActivity())[FixVolumesViewModel::class.java]


        viewModel.rows.observe(viewLifecycleOwner) { rows ->
            binding.table.removeAllViews()
            addTableHeader()
            rows.forEach { row ->
                addRowToTable(row)
            }
        }

        // Наименование приборы/оборудования


        // Вид работ
        val workTypeOptions = listOf("Монтаж", "Демонтаж", "Профилактика", "Тестирование")

        val workTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            workTypeOptions
        )

        binding.btnOpen.setOnClickListener {
            openExcelFilePicker()
        }

        // Добавить запись
        binding.btnAdd.setOnClickListener {
            val workType = binding.TextInputEditTextWorkType.text.toString().trim()
            val measures = binding.TextInputEditTextMeasureUnits.text.toString().trim()
            val plan = binding.TextInputEditTextPlan.text.toString().trim()
            val fact = binding.TextInputEditTextFact.text.toString().trim()

            Log.d(
                "FixingVolumesFragment",
                "Получили workType: '$workType'," +
                        "Получили measures: '$measures'," +
                        "Получили plan: '$plan'," +
                        "Получили fact: '$fact'"
            )

            if ( workType.isBlank() || measures.isBlank() || plan.isBlank() || fact.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val planValue = plan.toDoubleOrNull()
            val factValue = fact.toDoubleOrNull()

            if (factValue == null || planValue == null) {
                Toast.makeText(requireContext(), "Некорректные значения для расчёта", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = planValue - factValue
//            Log.d("FixingVolumesFragment","result: '$result', " )

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
                layoutParams = TableRow.LayoutParams(0, 150, 1f).apply {
                    gravity = Gravity.CENTER
                }.apply {
                    marginStart = 32
                    marginEnd = 32
                }
            }

            val editButton = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.edit_24)
                setBackgroundColor(Color.TRANSPARENT)
//                setPadding(8, 8, 8, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    val cells = (0 until tableRow.childCount - 1).map { index ->
                        tableRow.getChildAt(index) as TextView
                    }

                    val currentRow = FixVolumesRow(
                        cells[0].text.toString(),
                        cells[1].text.toString(),
                        cells[2].text.toString(),
                        cells[3].text.toString(),
                        cells[4].text.toString(),
                        cells[5].text.toString()
                    )

                    showEditDialog(currentRow, cells)
                }

            }

            Log.d(
                "FixingVolumesFragment",
                "workType: '$workType'," +
                        "measures: '$measures'," +
                        "plan: '$plan'," +
                        "fact: '$fact'" +
                        "result: '$result"
            )

            // Добавляем кнопки в контейнер
            buttonContainer.addView(editButton)

            tableRow.addView(createCell(rowCount.toString()))
            rowCount++

            tableRow.addView(createCell(workType))
            tableRow.addView(createCell(measures))
            tableRow.addView(createCell(plan))
            tableRow.addView(createCell(fact))
            tableRow.addView(createCell(result.toString())) // Вычисляем остаток

            tableRow.addView(buttonContainer)

            // Добавляем строку в таблицу
            binding.table.addView(tableRow)
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


            binding.TextInputEditTextWorkType.setText(workType)
            binding.TextInputEditTextMeasureUnits.setText(measure)
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
        val editResult = dialogView.findViewById<EditText>(R.id.editResult)

        editIdObject.setText((cells[0].text))
        editWorkProject.setText((cells[1].text))
        editMeasures.setText((cells[2].text))
        editPlan.setText((cells[3].text))
        editFact.setText((cells[4].text))
        editResult.setText((cells[5].text))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnFixCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnFixSave).setOnClickListener {
            val updatedRow = FixVolumesRow(
                editIdObject.text.toString(),
                editWorkProject.text.toString(),
                editMeasures.text.toString(),
                editPlan.text.toString(),
                editFact.text.toString(),
                editResult.text.toString()
            )
            viewModel.updateRow(oldRow = row, newRow = updatedRow)

            Toast.makeText(requireContext(),
                "Изменения сохранены", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }
        dialog.show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}