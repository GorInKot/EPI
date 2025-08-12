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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.epi.App
import com.example.epi.DataBase.Report.Report
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
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpandableAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository
        )
    }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
        private const val TAG = "ReportsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
                                    workType = report.contract,
                                    customer = report.customer,
                                    contractor = "report.contractor",
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

        // Кнопка экспорта с выбором формата
        binding.btnExportDataToCSV.setOnClickListener {
            if (selectedStartDate != null && selectedEndDate != null) {
                showExportFormatDialog()
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
            .setTheme(R.style.CustomPickerTheme)
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

    private fun showExportFormatDialog() {
        val formats = arrayOf("CSV", "Excel (XLSX)")
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите формат экспорта")
            .setItems(formats) { _, which ->
                when (which) {
                    0 -> checkStoragePermission("csv")
                    1 -> checkStoragePermission("xlsx")
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun checkStoragePermission(format: String) {
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
            exportData(selectedStartDate!!, selectedEndDate!!, format)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (selectedStartDate != null && selectedEndDate != null) {
                exportData(selectedStartDate!!, selectedEndDate!!, "csv") // По умолчанию CSV
            }
        } else {
            Toast.makeText(requireContext(), "Разрешение на запись не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun exportData(startDate: String, endDate: String, format: String) {
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

                Log.d(TAG, "Exporting ${reports.size} reports to $format")
                val fileName = "Сводный отчет за ${startDate}-${endDate}.${format.lowercase()}"
//                val fileName = "Сводный отчет_${System.currentTimeMillis()}.${format.lowercase()}"

                if (format == "csv") {
                    exportToCsv(startDate, endDate, reports, fileName)
                } else if (format == "xlsx") {
                    exportToExcel(startDate, endDate, reports, fileName)
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

    private suspend fun exportToCsv(startDate: String, endDate: String, reports: List<Report>, fileName: String) {
        val separator = ";"
        val csvHeader = listOf(
            "ИД", "Дата", "Время", "Договор СК", "Заказчик", "Объект", "Участок",
            "Генподрядчик", "Представитель генподрядчика", "Представитель ССК ПО (ГП)",
            "Субподрядчик", "Представитель субподрядчика", "Представитель ССК ПО (Суб)",
            "Отсутствие транспорта", "Исполнитель", "Дата начала", "Время начала",
            "Госномер", "Договор", "Договор транспорта", "Дата окончания", "Время окончания",
            "Нарушение", "Оборудование", "Комплекс работ", "Номер предписания",
            "Отчет", "Примечания", "Отправлено"
        ).joinToString(separator) { it.escapeCsv() } + "\n"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = requireContext().contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let { outputUri ->
                requireContext().contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                    outputStream.write(csvHeader.toByteArray(Charsets.UTF_8))
                    reports.forEach { report ->
                        val line = listOf(
                            report.id.toString(),
                            report.date,
                            report.time,
                            report.contract,
                            report.customer,
                            report.obj,
                            report.plot,
                            report.genContractor,
                            report.repGenContractor,
                            report.repSSKGp,
                            report.subContractor,
                            report.repSubContractor,
                            report.repSSKSub,
                            report.isEmpty.toString(),
                            report.executor,
                            report.startDate,
                            report.startTime,
                            report.stateNumber,
//                            report.contract,
                            report.contractTransport,
                            report.endDate,
                            report.endTime,
                            report.inViolation.toString(),
                            report.equipment,
                            report.complexWork,
                            report.orderNumber,
                            report.report,
                            report.remarks,
                            report.isSend.toString()
                        ).joinToString(separator) { it.escapeCsv() } + "\n"
                        outputStream.write(line.toByteArray(Charsets.UTF_8))
                    }
                }
                delay(1000)
                Toast.makeText(requireContext(), "Файл сохранен в папке Загрузки: $fileName", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(requireContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (!isExternalStorageWritable()) {
                Toast.makeText(requireContext(), "Хранилище недоступно", Toast.LENGTH_SHORT).show()
                return
            }
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            withContext(Dispatchers.IO) {
                FileWriter(file, Charsets.UTF_8).use { writer ->
                    writer.write("\uFEFF")
                    writer.append(csvHeader)
                    reports.forEach { report ->
                        val line = listOf(
                            report.id.toString(),
                            report.date,
                            report.time,
                            report.contract,
                            report.customer,
                            report.obj,
                            report.plot,
                            report.genContractor,
                            report.repGenContractor,
                            report.repSSKGp,
                            report.subContractor,
                            report.repSubContractor,
                            report.repSSKSub,
                            report.isEmpty.toString(),
                            report.executor,
                            report.startDate,
                            report.startTime,
                            report.stateNumber,
//                            report.contract,
                            report.contractTransport,
                            report.endDate,
                            report.endTime,
                            report.inViolation.toString(),
                            report.equipment,
                            report.complexWork,
                            report.orderNumber,
                            report.report,
                            report.remarks,
                            report.isSend.toString()
                        ).joinToString(separator) { it.escapeCsv() } + "\n"
                        writer.append(line)
                    }
                }
            }
            delay(1000)
            Toast.makeText(requireContext(), "Файл сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun exportToExcel(startDate: String, endDate: String, reports: List<Report>, fileName: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Отчеты")
        val headerStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                bold = true
            })
            setFillForegroundColor(IndexedColors.LIGHT_BLUE.index)
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
        }

        // Заголовки
        val headers = listOf(
            "ИД", "Дата", "Время", "Договор СК", "Заказчик", "Объект", "Участок",
            "Генподрядчик", "Представитель генподрядчика", "Представитель ССК ПО (ГП)",
            "Субподрядчик", "Представитель субподрядчика", "Представитель ССК ПО (Суб)",
            "Отсутствие транспорта", "Исполнитель", "Дата начала", "Время начала",
            "Госномер", "Договор", "Договор транспорта", "Дата окончания", "Время окончания",
            "Нарушение", "Оборудование", "Комплекс работ", "Номер предписания",
            "Отчет", "Примечания", "Отправлено"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // Данные
        reports.forEachIndexed { rowIndex, report ->
            val row = sheet.createRow(rowIndex + 1)
            val values = listOf(
                report.id.toString(),
                report.date,
                report.time,
                report.contract,
                report.customer,
                report.obj,
                report.plot,
                report.genContractor,
                report.repGenContractor,
                report.repSSKGp,
                report.subContractor,
                report.repSubContractor,
                report.repSSKSub,
                report.isEmpty.toString(),
                report.executor,
                report.startDate,
                report.startTime,
                report.stateNumber,
//                report.contract,
                report.contractTransport,
                report.endDate,
                report.endTime,
                report.inViolation.toString(),
                report.equipment,
                report.complexWork,
                report.orderNumber,
                report.report,
                report.remarks,
                report.isSend.toString()
            )
            values.forEachIndexed { colIndex, value ->
                row.createCell(colIndex).setCellValue(value)
            }
        }

        // Устанавливаем фиксированную ширину столбцов (в единицах 1/256 символа)
        headers.indices.forEach { index ->
            sheet.setColumnWidth(index, 15 * 256) // 15 символов ширины
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = requireContext().contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let { outputUri ->
                requireContext().contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                delay(1000)
                Toast.makeText(requireContext(), "Файл сохранен в папке Загрузки: $fileName", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(requireContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (!isExternalStorageWritable()) {
                Toast.makeText(requireContext(), "Хранилище недоступно", Toast.LENGTH_SHORT).show()
                return
            }
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            withContext(Dispatchers.IO) {
                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }
            }
            delay(1000)
            Toast.makeText(requireContext(), "Файл сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
        workbook.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Расширение для экранирования значений в CSV
fun String.escapeCsv(): String {
    if (this.contains(",") || this.contains(";") || this.contains("\"") || this.contains("\n")) {
        return "\"${this.replace("\"", "\"\"")}\""
    }
    return this
}