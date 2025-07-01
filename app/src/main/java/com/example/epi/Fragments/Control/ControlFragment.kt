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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.R
import com.example.epi.databinding.FragmentControlBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ControlViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentControlBinding.inflate(inflater, container, false)

        setupDropdownMenu()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ControlViewModel::class.java]

        // Подписка на номер предписания
        viewModel.orderNumber.observe(viewLifecycleOwner) {
            binding.tvOrderNumber.text = it
        }
        // Подписка на чекбокс
        viewModel.isViolation.observe(viewLifecycleOwner) { isChecked ->
            binding.btnOrderNumber.isEnabled = !isChecked
        }

        // Подписка на строки
        viewModel.rows.observe(viewLifecycleOwner) { rows ->
            binding.table.removeAllViews()
            rows.forEach { row ->
                addRowToTable(row)
            }
        }

        binding.tvDate.text = "Дата: ${getCurrentFormattedDate()}"

        // Чек бокс
        binding.checkBoxManualType.setOnCheckedChangeListener { _ , isChecked ->
            viewModel.setViolation(isChecked)
        }

        // Получить номер предписания
        binding.btnOrderNumber.setOnClickListener {
            viewModel.generateOrderNumber()
        }

        // Добавление заголовка таблицы
        viewModel.rows.observe(viewLifecycleOwner) { rows ->
            binding.table.removeAllViews()
            addTableHeader()
            rows.forEach { row ->
                addRowToTable(row)
            }
        }

        // Добавить строки (вид работ)
        binding.btnAddRow.setOnClickListener {
            val inputEquipmentName = binding.AutoCompleteTextViewEquipmentName.text.toString().trim()
            val inputType = binding.AutoCompleteTextViewType.text.toString().trim()
            val inputReport = binding.InputEditTextReport.text.toString().trim()
            val inputRemarks = binding.InputEditTextRemarks.text.toString().trim()
            val tvOrderNumber = binding.tvOrderNumber.text.toString().trim()

            val isViolationChecked = binding.checkBoxManualType.isChecked

            if (inputEquipmentName.isBlank() || inputType.isBlank() || inputReport.isBlank() || inputRemarks.isBlank() || (!isViolationChecked && tvOrderNumber.isBlank())) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRow = ControlRow(
                inputEquipmentName,
                inputType,
                tvOrderNumber,
                inputReport,
                inputRemarks
            )
            viewModel.addRow(newRow)

            clearInputFields()
        }

        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.fixFragment)
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.transportFragment)
        }
    }

    private fun addTableHeader() {
        val headerRow = TableRow(requireContext())
        headerRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        val headers = listOf("Оборудование", "Вид работ", "№ предписания", "Отчёт", "Примечание", "Действия")

        headers.forEach { title ->
            val textView = TextView(requireContext()).apply {
                text = title
                setPadding(8, 8, 8, 8)
                textSize = 18f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.black))
                setTextColor(ContextCompat.getColor(context, R.color.background))

                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(textView)
        }

        binding.table.addView(headerRow)
    }

    // Очищаем поля ввода
    private fun clearInputFields() {
        binding.AutoCompleteTextViewEquipmentName.setText("")
        binding.AutoCompleteTextViewType.setText("")
        binding.InputEditTextReport.setText("")
        binding.InputEditTextRemarks.setText("")
    }

    private fun addRowToTable(row: ControlRow) {
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
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f).apply {
                gravity = Gravity.CENTER
            }
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
                viewModel.removeRow(row)
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

        // Добавляем кнопки в контейнер
        buttonContainer.addView(editButton)
        buttonContainer.addView(deleteButton)

        // Добавляем ячейки в строку
        tableRow.addView(createCell(row.equipmentName))
        tableRow.addView(createCell(row.workType))
        tableRow.addView(createCell(row.orderNumber))
        tableRow.addView(createCell(row.report))
        tableRow.addView(createCell(row.remarks))

        // Добавление контейнера с кнопками в ячейку строки
        tableRow.addView(buttonContainer)

        // Добавляем строку в таблицу
        binding.table.addView(tableRow)
    }

    private fun setupDropdownMenu() {

        val equipmentNameItems = listOf("Прибор 1", "Прибор 2", "Прибор 3", "Прибор 4", "Прибор 5")
        val workTypeItems = listOf("Вид работ 1", "Вид работ 2", "Вид работ 3", "Вид работ 4", "Вид работ 5")

        binding.AutoCompleteTextViewEquipmentName.apply {
            setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, equipmentNameItems))
            setOnClickListener {
                showDropDown()
            }
        }

        binding.AutoCompleteTextViewType.apply {
            setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workTypeItems))
            setOnClickListener {
                showDropDown()
            }
        }
    }

    private fun showEditDialog(row: ControlRow, cells: List<TextView>) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_row, null)

        val editEquipment = dialogView.findViewById<EditText>(R.id.edEquipment)
        val editType = dialogView.findViewById<EditText>(R.id.textInputType)
        val editOrder = dialogView.findViewById<EditText>(R.id.editOrderNumber)
        val editReport = dialogView.findViewById<EditText>(R.id.editReport)
        val editRemarks = dialogView.findViewById<EditText>(R.id.editRemarks)

        editEquipment.setText(cells[0].text)
        editType.setText(cells[1].text)
        editOrder.setText(cells[2].text)
        editReport.setText(cells[3].text)
        editRemarks.setText(cells[4].text)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val updatedRow = ControlRow(
                editEquipment.text.toString(),
                editType.text.toString(),
                editOrder.text.toString(),
                editReport.text.toString(),
                editRemarks.text.toString()
            )
            viewModel.updateRow(oldRow = row, newRow = updatedRow)

            Toast.makeText(requireContext(),
                "Изменения сохранены", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getCurrentFormattedDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}