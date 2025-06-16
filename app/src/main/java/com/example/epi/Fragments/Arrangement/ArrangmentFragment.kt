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

        // Кнопка "На главное меню"
        binding.AttFrBackToMainMenuBtn.setOnClickListener { findNavController().navigate(R.id.StartFragment) }

        // Левый блок

        // Выпадающий список заказчиков
        val customerList = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, customerList)
        binding.autoCompleteCustomer.setAdapter(adapter)

        // Обработка CheckBox-а
        binding.checkBoxManualCustomer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textInputLayoutAutoCustomer.visibility = View.GONE
                binding.textInputLayoutManualCustomer.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAutoCustomer.visibility = View.VISIBLE
                binding.textInputLayoutManualCustomer.visibility = View.GONE
            }
        }

        // Правый блок
        binding.AttrFrBtnTransport.setOnClickListener {
            findNavController().navigate(R.id.transportFragment)
        }
        binding.AttrFrBtnVolume.setOnClickListener {
            findNavController().navigate(R.id.volumeCMPFragment)
        }

        // Кнопка "Копия предыдущего отчета"

        // Кнопка "Очистить"
        binding.AttrFrBtnClear.setOnClickListener {
//            binding.edEdCustomer.text!!.clear()
            binding.edEdSpecialist.text!!.clear()
//            binding.edEdObject.text!!.clear()
            binding.edEdPlot.text!!.clear()
//            binding.edEdGenConstractor.text!!.clear()
//            binding.edEdConstractor.text!!.clear()
            binding.edEdRepSSKGp.text!!.clear()
            binding.edEdSubcontractor.text!!.clear()
            binding.edEdRepSubcontractor.text!!.clear()
            binding.edEdRepSSKSub.text!!.clear()

            Toast.makeText(requireContext(),
                "Все поля очищены",
                Toast.LENGTH_LONG).show()

            Log.d("TAG","All editText cleared")

        }

        // Кнопка "Далее"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
