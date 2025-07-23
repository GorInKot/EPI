package com.example.epi.Fragments.Reports.Reports

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.epi.*
import com.example.epi.DataBase.AppDatabase
import com.example.epi.DataBase.ReportRepository
import com.example.epi.databinding.FragmentReportsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExpandableAdapter
    private val viewModel: ReportsViewModel by viewModels {
        ReportsViewModelFactory(
            ReportRepository(
                Room.databaseBuilder(
                    requireContext(),
                    AppDatabase::class.java,
                    "app_database"
                ).build().reportDao()
            )
        )
    }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на данные из ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reports.collectLatest { reports ->
                adapter = ExpandableAdapter(mutableListOf<Any>().apply {
                    addAll(reports.map { report ->
                        ParentItem(
                            date = report.date,
                            obj = report.obj,
                            children = listOf(
                                ChildItem(
                                    workType = report.workType,
                                    customer = report.customer,
                                    contractor = report.contractor,
                                    transportCustomer = report.executor
                                )
                            ),
                            isExpanded = false
                        )
                    })
                })
                binding.recyclerView.adapter = adapter
            }
        }

        // Кнопка выбора дат
        binding.btnSelectDates.setOnClickListener {
            showDateRangePicker()
        }

        // Кнопка экспорта
        binding.btnExportDataToCSV.setOnClickListener {
            if (selectedStartDate != null && selectedEndDate != null) {
                checkStoragePermission()
            } else {
                Toast.makeText(context, "Выберите диапазон дат", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка перехода к главному меню
        binding.RepFrMainMenuBtn.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(16)) // Отступ между элементами
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите диапазон дат")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
            selectedStartDate = dateFormat.format(Date(dateRange.first))
            selectedEndDate = dateFormat.format(Date(dateRange.second))

            // Фильтрация отчетов по датам
            viewModel.filterReportsByDateRange(selectedStartDate!!, selectedEndDate!!)

            Toast.makeText(
                context,
                "Выбрано: $selectedStartDate - $selectedEndDate",
                Toast.LENGTH_SHORT
            ).show()
        }

        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        } else {
            exportToCsv(selectedStartDate!!, selectedEndDate!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (selectedStartDate != null && selectedEndDate != null) {
                exportToCsv(selectedStartDate!!, selectedEndDate!!)
            }
        } else {
            Toast.makeText(context, "Разрешение на запись не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToCsv(startDate: String, endDate: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reports = withContext(Dispatchers.IO) {
                    viewModel.getReportsForExport(startDate, endDate)
                }

                if (reports.isEmpty()) {
                    Toast.makeText(context, "Нет данных за выбранный период", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val fileName = "reports_${System.currentTimeMillis()}.csv"
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

                withContext(Dispatchers.IO) {
                    FileWriter(file).use { writer ->
                        // Заголовки CSV
                        writer.append("ID,Date,Time,WorkType,Customer,Object,Plot,Contractor,RepContractor,RepSSKGp,SubContractor,RepSubContractor,RepSSKSub,IsEmpty,Executor,StartDate,StartTime,StateNumber,Contract,ContractTransport,EndDate,EndTime,InViolation,Equipment,ComplexWork,OrderNumber,Report,Remarks,IsSend\n")
                        // Данные
                        reports.forEach { report ->
                            writer.append("${report.id},${report.date},${report.time},${report.workType.escapeCsv()},${report.customer.escapeCsv()},${report.obj.escapeCsv()},${report.plot.escapeCsv()},${report.contractor.escapeCsv()},${report.repContractor.escapeCsv()},${report.repSSKGp.escapeCsv()},${report.subContractor.escapeCsv()},${report.repSubContractor.escapeCsv()},${report.repSSKSub.escapeCsv()},${report.isEmpty},${report.executor.escapeCsv()},${report.startDate},${report.startTime},${report.stateNumber.escapeCsv()},${report.contract.escapeCsv()},${report.contractTransport.escapeCsv()},${report.endDate},${report.endTime},${report.inViolation},${report.equipment.escapeCsv()},${report.complexWork.escapeCsv()},${report.orderNumber.escapeCsv()},${report.report.escapeCsv()},${report.remarks.escapeCsv()},${report.isSend}\n")
                        }
                    }
                }

                Toast.makeText(context, "Файл сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка при экспорте: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Расширение для экранирования значений в CSV
fun String.escapeCsv(): String {
    if (this.contains(",") || this.contains("\"") || this.contains("\n")) {
        return "\"${this.replace("\"", "\"\"")}\""
    }
    return this
}