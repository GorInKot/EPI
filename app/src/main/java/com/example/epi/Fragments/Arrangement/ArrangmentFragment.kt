package com.example.epi.Fragments.Arrangement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.databinding.FragmentArrangmentBinding
import java.text.SimpleDateFormat
import java.util.*

class ArrangementFragment : Fragment() {
    private var _binding: FragmentArrangmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentArrangmentBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка даты и времени
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.AttrFrTvDate.text = "Дата: $currentDate"
        binding.AttrFrTvTime.text = "Время: $currentTime"

        // Левый блок

        // Выпадающий список заказчиков
        val customerList = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, customerList)
        binding.autoCompleteCustomer.setAdapter(adapter)

        // Заказчик: обработка CheckBox
        binding.checkBoxManualCustomer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoCustomer.visibility = View.GONE
                binding.textInputLayoutManualCustomer.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoCustomer.visibility = View.VISIBLE
                binding.textInputLayoutManualCustomer.visibility = View.GONE
            }
        }

        // Объект: обработка CheckBox
        binding.checkBoxManualObject.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoObject.visibility = View.GONE
                binding.textInputLayoutManualObject.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoObject.visibility = View.VISIBLE
                binding.textInputLayoutManualObject.visibility = View.GONE
            }
        }

        // Генподрядчик: обработка CheckBox
        binding.checkBoxManualObject.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoContractor.visibility = View.GONE
                binding.textInputLayoutManualContractor.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoContractor.visibility = View.VISIBLE
                binding.textInputLayoutManualContractor.visibility = View.GONE
            }
        }


        // Кнопка "Копия предыдущего отчета"
        binding.AttrFrBtnCopy.setOnClickListener {
            Toast.makeText(requireContext(),"Просто кнопка",Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Очистить"
        binding.AttrFrBtnClear.setOnClickListener {
            // Заказчик
            binding.autoCompleteCustomer.text!!.clear()
            binding.edManualCustomer.text!!.clear()

            // Объект
            binding.autoCompleteObject.text!!.clear()
            binding.edManualObject.text!!.clear()

            // Участок
            binding.edPlot.text!!.clear()

            // Генподрядчик
            binding.autoCompleteContractor.text!!.clear()
            binding.edManualContractor.text!!.clear()

            // Представитель генподрядчика
            binding.autoCompleteSubContractor.text!!.clear()
            binding.edManualSubContractor.text!!.clear()

            // Представитель ССК ПО (ГП)
            binding.edRepSSKGp.text!!.clear()

            // Субподрядчик
            binding.edSubcontractor.text!!.clear()

            // Представитель Субподрядчика
            binding.edRepSubcontractor.text!!.clear()

            // Представитель ССУ ПО (Суб)
            binding.edRepSSKSub.text!!.clear()
        }

        // Кнопка "Далее"
        binding.AttrFrBtnNext.setOnClickListener {
            findNavController().navigate(R.id.transportFragment)
        }

        binding.AttrFrrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
