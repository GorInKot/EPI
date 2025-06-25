package com.example.epi.Fragments.Control

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentControlBinding
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = "Дата: $currentDate"

        // Получить номер предписания
        var counter = 1
        binding.btnOrderNumber.setOnClickListener {
            val orderNumber = "1234_$counter"
            binding.tvOrderNumber.text = orderNumber
            counter++
        }


        // Добавить вид работ
        binding.btnAddRow.setOnClickListener {
            val inputEquipmentName = binding.textInputEditTextEquipmentName.text.toString().trim()
            val inputType = binding.textInputEditTextType.text.toString().trim()
            val inputReport = binding.InputEditTextReport.text.toString().trim()
            val inputRemarks = binding.InputEditTextRemarks.text.toString().trim()
            val tvOrderNumber = binding.tvOrderNumber.text.toString().trim()

            if (inputEquipmentName.isBlank() || inputType.isBlank() || tvOrderNumber.isBlank() || inputReport.isBlank() || inputRemarks.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f).apply {
                    gravity = Gravity.CENTER
                }
            }

            val deleteButton = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.delete_24)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    binding.table.removeView(tableRow)
                    Toast.makeText(requireContext(), "Строка удалена", Toast.LENGTH_SHORT).show()
                }
            }

            val editButton = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.edit_24)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(8, 8, 8, 8)
                setOnClickListener {

                    // Кастомный AlertDialog
                    val dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_edit_row, null)

                    val editEquipment = dialogView.findViewById<EditText>(R.id.editEquipment)
                    val editType = dialogView.findViewById<EditText>(R.id.editType)
                    val editOrder = dialogView.findViewById<EditText>(R.id.editOrderNumber)
                    val editReport = dialogView.findViewById<EditText>(R.id.editReport)
                    val editRemarks = dialogView.findViewById<EditText>(R.id.editRemarks)

                    val cells = (0 until tableRow.childCount - 1).map { index ->
                        tableRow.getChildAt(index) as TextView
                    }

                    editEquipment.setText(cells[0].text)
                    editType.setText(cells[1].text)
                    editOrder.setText(cells[2].text)
                    editReport.setText(cells[3].text)
                    editRemarks.setText(cells[4].text)

                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create()

                    // Устанавливаем фон для CustomAlertDialog
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                        dialog.dismiss()
                    }

                    dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                        cells[0].text = editEquipment.text.toString()
                        cells[1].text = editType.text.toString()
                        cells[2].text = editOrder.text.toString()
                        cells[3].text = editReport.text.toString()
                        cells[4].text = editRemarks.text.toString()

                        Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    dialog.show()
                }

            }

            // Добавляем кнопки в контейнер
            buttonContainer.addView(editButton)
            buttonContainer.addView(deleteButton)

            // Добавляем ячейки в строку
            tableRow.addView(createCell(inputEquipmentName))
            tableRow.addView(createCell(inputType))
            tableRow.addView(createCell(tvOrderNumber))
            tableRow.addView(createCell(inputReport))
            tableRow.addView(createCell(inputRemarks))

            // Добавление контейнера с кнопками в ячейку строки
            tableRow.addView(buttonContainer)

            Log.d("Table", "Добавление данных в таблицу")

            // Очищаем поля ввода
            binding.textInputEditTextEquipmentName.setText("")
            binding.textInputEditTextType.setText("")
            binding.InputEditTextReport.setText("")
            binding.InputEditTextRemarks.setText("")

            // Добавляем строку в таблицу
            binding.table.addView(tableRow)
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        // Кнопка "Далее"
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.fixFragment)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}