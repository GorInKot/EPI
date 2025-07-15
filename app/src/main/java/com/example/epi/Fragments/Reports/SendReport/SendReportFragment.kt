package com.example.epi.Fragments.Reports.SendReport

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.epi.DataBase.AppDatabase
import com.example.epi.Fragments.Reports.Reports.ChildItem
import com.example.epi.Fragments.Reports.Reports.ExpandableAdapter
import com.example.epi.Fragments.Reports.Reports.ParentItem
import com.example.epi.R
import com.example.epi.ViewModel.SharedViewModel
import com.example.epi.databinding.FragmentSendReportBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class SendReportFragment : Fragment() {

    private var _binding : FragmentSendReportBinding? = null
    private val binding get() = _binding!!



    private val viewModel: SharedViewModel by activityViewModels()

    companion object {
        private const val TAG = "SendReportFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSendReportBinding.inflate(inflater, container, false)

        binding.SeRFrBtnInfo.setOnClickListener {
            Log.d(TAG, "Info-кнопка нажата")
//            showAllEnteredData()
            exportDatabase(requireContext())
            Log.d(TAG, "Показали AlertDialog с информацией")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.SeRFrBtnNext.setOnClickListener {
            Log.d(TAG, "Next-кнопка нажата")
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "запускаем поток и загружаем данные в Room")
//                    saveReportToDatabase()
                    Log.d(TAG, "Закончили загружать данные в Room")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка загрузки данные в Room: ${e.message}", e)
                }

            }
            findNavController().navigate(R.id.reportsFragment)

        }

        binding.SeRFrBtnBack.setOnClickListener {
            findNavController().navigate(R.id.fixFragment)
        }
    }

    fun exportDatabase(context: Context) {
        val dbName = "app_database"
        val dbPath = context.getDatabasePath(dbName)

        val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "")
        if (!exportDir.exists()) exportDir.mkdirs()

        val outFile = File(exportDir, dbName)
        try {
            FileInputStream(dbPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("ExportDB", "БД экспортирована в: ${outFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("ExportDB", "Ошибка экспорта: ${e.message}")
        }
    }

//    suspend fun saveReportToDatabase(){
//        Log.d(TAG, "Вызвали функцию saveReportToDatabase")
//        val db = AppDatabase.getDatabase(requireContext())
//        val report = ReportEntity(
//            date = viewModel.currentDate.value ?: "",
//            time = viewModel.currentTime.value ?: "",
//            workType = viewModel.selectedWorkType.value,
//            customer = viewModel.selectedCustomer.value,
//            obj = viewModel.selectedObject.value,
//            plot = viewModel.plotText.value,
//            contractor = viewModel.selectedContractor.value,
//            repContractor = viewModel.selectedSubContractor.value,
//            repSSKGp = viewModel.repSSKGpText.value,
//            subContractor = viewModel.subContractorText.value,
//            repSubContractor = viewModel.repSubcontractorText.value,
//            repSskSub = viewModel.repSSKSubText.value,
//            isTransportAbsent = viewModel.isTransportAbsent.value ?: false,
//            transportCustomer = viewModel.customerName.value,
//            transportContract = viewModel.contractCustomer.value,
//            transportExecutor = viewModel.executorName.value,
//            transportContractNumber = viewModel.contractTransport.value,
//            stateNumber = viewModel.stateNumber.value,
//            startDate = viewModel.startDate.value,
//            startTime = viewModel.startTime.value,
//            endDate = viewModel.endDate.value,
//            endTime = viewModel.endTime.value,
//            isViolation = viewModel.isViolation.value ?: false,
//            control = Gson().toJson(viewModel.controlRow.value),
//            fixVolumes = Gson().toJson(viewModel.fixRows.value)
//
//        )
//
//        Log.d(TAG, "Report object created: $report")
//
//        db.reportDao().insertReport(report)
//
//        Log.d(TAG, "insertReport completed")
//    }

//    private fun showAllEnteredData() {
//        val info = buildString {
//
//            // -------- Расстановка --------
//            appendLine("Дата: ${viewModel.currentDate.value}")
//            appendLine("Время: ${viewModel.currentTime.value}")
//            appendLine("Тип работ: ${viewModel.selectedWorkType.value ?: ""}")
//
//            val customer = if (viewModel.isManualCustomer.value == true)
//                "Заказчик (вручную): ${viewModel.manualCustomer.value}"
//            else
//                "Заказчик (выбран): ${viewModel.selectedCustomer.value}"
//            appendLine(customer)
//
//            val obj = if (viewModel.isManualObject.value == true)
//                "Объект (вручную): ${viewModel.manualObject.value}"
//            else
//                "Объект (выбран): ${viewModel.selectedObject.value}"
//            appendLine(obj)
//
//            appendLine("Участок: ${viewModel.plotText.value}")
//
//            val contractor = if (viewModel.isManualContractor.value == true)
//                "Генподрядчик (вручную): ${viewModel.manualContractor.value}"
//            else
//                "Генподрядчик (выбран): ${viewModel.selectedContractor.value}"
//            appendLine(contractor)
//
//            val subContractor = if (viewModel.isManualSubContractor.value == true)
//                "Представитель генподрядчика (вручную): ${viewModel.manualSubContractor.value}"
//            else
//                "Представитель генподрядчика (выбран): ${viewModel.selectedSubContractor.value}"
//            appendLine(subContractor)
//
//            appendLine("Представитель ССК (ГП): ${viewModel.repSSKGpText.value}")
//            appendLine("(Суб)Подрядчик: ${viewModel.subContractorText.value}")
//            appendLine("Представитель (суб)подрядчика: ${viewModel.repSubcontractorText.value}")
//            appendLine("Представитель ССК ПО (Суб): ${viewModel.repSSKSubText.value}")
//
//            // -------- Транспорт --------
//            appendLine("\n\nТранспорт:")
//            val isAbsent = viewModel.isTransportAbsent.value ?: false
//            if (isAbsent) {
//                appendLine("Транспорт отсутствует")
//            } else {
//                appendLine("Заказчик: ${viewModel.customerName.value}")
//                appendLine("Договор СК: ${viewModel.contractCustomer.value}")
//                appendLine("Исполнитель по транспорту: ${viewModel.executorName.value}")
//                appendLine("Договор по транспорту: ${viewModel.contractTransport.value}")
//                appendLine("Гос. номер: ${viewModel.stateNumber.value}")
//                appendLine("Начало поездки: ${viewModel.startDate.value} ${viewModel.startTime.value}")
//                appendLine("Завершение поездки: ${viewModel.endDate.value} ${viewModel.endTime.value}")
//            }
//
//            // -------- ??? --------
//            appendLine("\n\nНарушение:")
//            val isViolation = viewModel.isViolation.value ?: false
//            if (isViolation) {
//                appendLine("Нарушение есть: ${viewModel.isViolation.value}")
//            }
//            else {
//                appendLine("Нарушений нет")
//            }
//
//            appendLine("Номер предписания: ${viewModel.orderNumber.value?.get(0)}")
//
//            // -------- Контроль --------
//            appendLine("\n\nКонтроль:")
//            viewModel.controlRow.value?.forEach { row ->
//                appendLine("— Прибор: ${row.equipmentName}")
//                appendLine("  Вид работ: ${row.workType}")
//                appendLine("  Номер предписания: ${row.orderNumber}")
//                appendLine("  Отчет: ${row.report}")
//                appendLine("  Замечания: ${row.remarks}")
//                appendLine()
//            }
//
//            // -------- Объемы --------
//            appendLine("\n\nЗафиксированные объемы:")
//            viewModel.fixRows.value?.forEach { row ->
//                appendLine("— ID Объекта: ${row.ID_object}")
//                appendLine(" Вид работ из проекта: ${row.projectWorkType}")
//                appendLine(" Единицы измерения: ${row.measure}")
//                appendLine(" Объем работ по проекту: ${row.plan}")
//                appendLine(" Выполненный объем работ: ${row.fact}")
//                appendLine(" Остаток по объему: ${row.result}")
//            }
//        }
//
//        // Покажем через AlertDialog
//        AlertDialog.Builder(requireContext())
//            .setTitle("Введенные данные")
//            .setMessage(info)
//            .setPositiveButton("ОК", null)
//            .show()
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}