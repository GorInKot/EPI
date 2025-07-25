package com.example.epi.Fragments.Reports.Reports

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.epi.App
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentReportsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpandableAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory((requireActivity().application as App).reportRepository)
    }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
        private const val TAG = "ReportsFragment"
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

        // Отключение кнопки "Назад"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Ничего не делаем, чтобы заблокировать возврат
        }

        // Настройка RecyclerView
        setupRecyclerView()

        // Подписка на данные из ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.reports.collectLatest { reports ->
                adapter = ExpandableAdapter(mutableListOf<Any>().apply {
                    addAll(reports.map { report ->
                        ParentItem(
                            date = report.date,
                            time = report.time,
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
                Toast.makeText(requireContext(), "Выберите диапазон дат", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка перехода к главному меню
        binding.RepFrMainMenuBtn.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(16))
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите диапазон дат")
            .build()
        dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
            selectedStartDate = dateFormat.format(Date(dateRange.first))
            selectedEndDate = dateFormat.format(Date(dateRange.second))
            sharedViewModel.filterReportsByDateRange(selectedStartDate!!, selectedEndDate!!)
            Toast.makeText(
                context,
                "Выбрано: $selectedStartDate - $selectedEndDate",
                Toast.LENGTH_SHORT
            ).show()
        }
        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
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
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (selectedStartDate != null && selectedEndDate != null) {
                exportToCsv(selectedStartDate!!, selectedEndDate!!)
            }
        } else {
            Toast.makeText(requireContext(), "Разрешение на запись не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun exportToCsv(startDate: String, endDate: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.parentProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.textProgressBar.visibility = View.VISIBLE
                delay(2000)

                val reports = withContext(Dispatchers.IO) {
                    sharedViewModel.getReportsForExport(startDate, endDate)
                }
                if (reports.isEmpty()) {
                    binding.parentProgressBar.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.textProgressBar.visibility = View.GONE
                    Toast.makeText(context, "Нет данных за выбранный период", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d(TAG, "Exporting ${reports.size} reports")

                val fileName = "reports_${System.currentTimeMillis()}.csv"
                val csvHeader = "ID,Date,Time,WorkType,Customer,Object,Plot,Contractor,RepContractor,RepSSKGp,SubContractor,RepSubContractor,RepSSKSub,IsEmpty,Executor,StartDate,StartTime,StateNumber,Contract,ContractTransport,EndDate,EndTime,InViolation,Equipment,ComplexWork,OrderNumber,Report,Remarks,IsSend\n"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Используем MediaStore для Android 10+
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri = requireContext().contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    uri?.let {
                        requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(csvHeader.toByteArray())
                            reports.forEach { report ->
                                val line = "${report.id},${report.date},${report.time},${report.workType.escapeCsv()},${report.customer.escapeCsv()},${report.obj.escapeCsv()},${report.plot.escapeCsv()},${report.contractor.escapeCsv()},${report.repContractor.escapeCsv()},${report.repSSKGp.escapeCsv()},${report.subContractor.escapeCsv()},${report.repSubContractor.escapeCsv()},${report.repSSKSub.escapeCsv()},${report.isEmpty},${report.executor.escapeCsv()},${report.startDate},${report.startTime},${report.stateNumber.escapeCsv()},${report.contract.escapeCsv()},${report.contractTransport.escapeCsv()},${report.endDate},${report.endTime},${report.inViolation},${report.equipment.escapeCsv()},${report.complexWork.escapeCsv()},${report.orderNumber.escapeCsv()},${report.report.escapeCsv()},${report.remarks.escapeCsv()},${report.isSend}\n"
                                outputStream.write(line.toByteArray())
                            }
                        }
                        delay(1000)
                        Toast.makeText(requireContext(), "Файл сохранен в папке Загрузки: $fileName", Toast.LENGTH_LONG).show()
                    } ?: run {
                        Toast.makeText(requireContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Используем File API для Android 9 и ниже
                    if (!isExternalStorageWritable()) {
                        Toast.makeText(requireContext(), "Хранилище недоступно", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!directory.exists()) {
                        directory.mkdirs()
                    }
                    val file = File(directory, fileName)
                    withContext(Dispatchers.IO) {
                        FileWriter(file).use { writer ->
                            writer.append(csvHeader)
                            reports.forEach { report ->
                                writer.append("${report.id},${report.date},${report.time},${report.workType.escapeCsv()},${report.customer.escapeCsv()},${report.obj.escapeCsv()},${report.plot.escapeCsv()},${report.contractor.escapeCsv()},${report.repContractor.escapeCsv()},${report.repSSKGp.escapeCsv()},${report.subContractor.escapeCsv()},${report.repSubContractor.escapeCsv()},${report.repSSKSub.escapeCsv()},${report.isEmpty},${report.executor.escapeCsv()},${report.startDate},${report.startTime},${report.stateNumber.escapeCsv()},${report.contract.escapeCsv()},${report.contractTransport.escapeCsv()},${report.endDate},${report.endTime},${report.inViolation},${report.equipment.escapeCsv()},${report.complexWork.escapeCsv()},${report.orderNumber.escapeCsv()},${report.report.escapeCsv()},${report.remarks.escapeCsv()},${report.isSend}\n")
                            }
                        }
                    }
                    // Дополнительная пауза после записи для тестирования ProgressBar
                    delay(1000)
                    Toast.makeText(requireContext(), "Файл сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export error: ${e.message}", e)
                Toast.makeText(requireContext(), "Ошибка при экспорте: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.parentProgressBar.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.textProgressBar.visibility = View.GONE
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