package com.example.epi.Fragments.Reports.SendReport

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.R
import com.example.epi.ViewModel.SharedViewModel
import com.example.epi.databinding.FragmentSendReportBinding


class SendReportFragment : Fragment() {

    private var _binding : FragmentSendReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSendReportBinding.inflate(inflater, container, false)

        binding.SeRFrBtnInfo.setOnClickListener {
            showAllEnteredData()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.SeRFrBtnNext.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }

        binding.SeRFrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.fixFragment)
        }
    }

    private fun showAllEnteredData() {
        val info = buildString {
            appendLine("Дата: ${viewModel.currentDate.value}")
            appendLine("Время: ${viewModel.currentTime.value}")
            appendLine("Тип работ: ${viewModel.selectedWorkType.value ?: ""}")

            val customer = if (viewModel.isManualCustomer.value == true)
                "Заказчик (вручную): ${viewModel.manualCustomer.value}"
            else
                "Заказчик (выбран): ${viewModel.selectedCustomer.value}"
            appendLine(customer)

            val obj = if (viewModel.isManualObject.value == true)
                "Объект (вручную): ${viewModel.manualObject.value}"
            else
                "Объект (выбран): ${viewModel.selectedObject.value}"
            appendLine(obj)

            val contractor = if (viewModel.isManualContractor.value == true)
                "Генподрядчик (вручную): ${viewModel.manualContractor.value}"
            else
                "Генподрядчик (выбран): ${viewModel.selectedContractor.value}"
            appendLine(contractor)

            val subContractor = if (viewModel.isManualSubContractor.value == true)
                "Представитель генподрядчика (вручную): ${viewModel.manualSubContractor.value}"
            else
                "Представитель генподрядчика (выбран): ${viewModel.selectedSubContractor.value}"
            appendLine(subContractor)

            appendLine("\n\nУчасток: ${viewModel.plotText.value}")
            appendLine("Представитель ССК (ГП): ${viewModel.repSSKGpText.value}")
            appendLine("Подрядчик: ${viewModel.subContractorText.value}")
            appendLine("Представитель подрядчика: ${viewModel.repSubcontractorText.value}")
            appendLine("Представитель ССК (Подрядчик): ${viewModel.repSSKSubText.value}")

            // Transport data
            appendLine("\n\nТранспорт:")
            val isAbsent = viewModel.isTransportAbsent.value ?: false
            if (isAbsent) {
                appendLine("Транспорт отсутствует")
            } else {
                appendLine("Заказчик транспорта: ${viewModel.customerName.value}")
                appendLine("Договор заказчика: ${viewModel.contractCustomer.value}")
                appendLine("Исполнитель: ${viewModel.executorName.value}")
                appendLine("Договор исполнителя: ${viewModel.contractTransport.value}")
                appendLine("Гос. номер: ${viewModel.stateNumber.value}")
                appendLine("Начало: ${viewModel.startDate.value} ${viewModel.startTime.value}")
                appendLine("Окончание: ${viewModel.endDate.value} ${viewModel.endTime.value}")
            }
//            if (viewModel.isTransportAbsent.value == false) {
//                appendLine("Транспорт отсутствует")
//            } else {
//                appendLine("Заказчик транспорта: ${viewModel.customerName.value}")
//                appendLine("Договор заказчика: ${viewModel.contractCustomer.value}")
//                appendLine("Исполнитель: ${viewModel.executorName.value}")
//                appendLine("Договор исполнителя: ${viewModel.contractTransport.value}")
//                appendLine("Гос. номер: ${viewModel.stateNumber.value}")
//                appendLine("Начало: ${viewModel.startDate.value} ${viewModel.startTime.value}")
//                appendLine("Окончание: ${viewModel.endDate.value} ${viewModel.endTime.value}")
//            }

            // Нарушения
            appendLine("\n\nНарушение:")
            appendLine("Нарушение есть: ${viewModel.isViolation.value}")
            appendLine("Номер предписания: ${viewModel.orderNumber.value}")

            // Контрольные строки
            appendLine("\n\nКонтроль:")
            viewModel.controlRow.value?.forEachIndexed { i, row ->
                appendLine("[$i] ${row}")
            }

            // Зафиксированные объемы
            appendLine("\n\nЗафиксированные объемы:")
            viewModel.fixRows.value?.forEachIndexed { i, row ->
                appendLine("[$i] ${row}")
            }
        }

        // Покажем через AlertDialog
        AlertDialog.Builder(requireContext())
            .setTitle("Введенные данные")
            .setMessage(info)
            .setPositiveButton("ОК", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}