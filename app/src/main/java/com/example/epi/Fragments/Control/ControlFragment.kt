package com.example.epi.Fragments.Control

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentControlBinding
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
        binding.CtrFrTvDate.text = "Дата: $currentDate"

        // Получить номер предписания
        var counter = 1
        binding.btnOrderNumber.setOnClickListener {
            val orderNumber = "1234_$counter"
            binding.tvOrderNumber.text = orderNumber
            counter++
        }


        // Добавить вид работ
        binding.btnAddRow.setOnClickListener {
            val inputEquipmentName = binding.InputEditTextEquipmentName.text.toString().trim()
            val inputType = binding.InputEditTextType.text.toString().trim()
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
            Log.d(
                "TransportFragment",
                "Прибор: '$inputEquipmentName', " +
                        "Вид работ: '$inputType'," +
                        "Номер предписания: '$tvOrderNumber'," +
                        "Отчет о результатах: '$inputReport'," +
                        "Замечания: '$inputRemarks'"
            )

//            Toast.makeText(requireContext(), "input1: $inputEquipmentName", Toast.LENGTH_SHORT).show()

            // Добавляем ячейки в строку
            tableRow.addView(createCell(inputEquipmentName))
            tableRow.addView(createCell(inputType))
            tableRow.addView(createCell(tvOrderNumber))
            tableRow.addView(createCell(inputReport))
            tableRow.addView(createCell(inputRemarks))


            Log.d("Table", "Добавление данных в таблицу")

            // Очищаем поля ввода
            binding.InputEditTextEquipmentName.setText("")
            binding.InputEditTextType.setText("")
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