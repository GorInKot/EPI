package com.example.epi.Fragments.FixingVolumes

import android.graphics.Color
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
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentFixingVolumesBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView


class FixingVolumesFragment : Fragment() {

    private var _binding: FragmentFixingVolumesBinding? = null
    private val binding get() = _binding!!

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

        // Наименование приборы/оборудования


        // Вид работ
        val workTypeOptions = listOf("Монтаж", "Демонтаж", "Профилактика", "Тестирование")

        val workTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            workTypeOptions
        )


        // Добавить запись
        binding.btnAdd.setOnClickListener {

            val measures = binding.TextInputEditTextMeasureUnits.text.toString().trim()
            val plan = binding.TextInputEditTextPlan.text.toString().trim()
            val fact = binding.TextInputEditTextFact.text.toString().trim()

            if ( measures.isBlank() || plan.isBlank() || fact.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val planValue = plan.toDoubleOrNull()
            val factValue = fact.toDoubleOrNull()
            Log.d("FixingVolumesFragment","planValue: '$planValue', " )
            Log.d("FixingVolumesFragment","volumeValue: '$factValue', " )

            if (factValue == null || planValue == null) {
                Toast.makeText(requireContext(), "Некорректные значения для расчёта", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = planValue - factValue
            Log.d("FixingVolumesFragment","result: '$result', " )

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
                "FixingVolumesFragment",
                "workType: 'тест'," +
                        "measures: '$measures'," +
                        "plan: '$plan'," +
                        "fact: '$fact'" +
                        "result: '$result"
            )

            tableRow.addView(createCell(rowCount.toString()))
            rowCount++

            tableRow.addView(createCell(measures))
            tableRow.addView(createCell(plan))
            tableRow.addView(createCell(fact))
            tableRow.addView(createCell(result.toString())) // Вычисляем остаток

            // Добавляем строку в таблицу
            binding.table.addView(tableRow)
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.sendReportFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


}