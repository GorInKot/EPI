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
import com.example.epi.DataBase.User.User
import com.example.epi.R
import com.example.epi.SharedViewModel
import com.example.epi.ViewModel.SharedViewModelFactory
import com.example.epi.databinding.FragmentReportsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import kotlin.String

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpandableAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            (requireActivity().application as App).reportRepository,
            (requireActivity().application as App).userRepository,
            requireActivity().applicationContext,
            (requireActivity().application as App).planValueRepository,
            (requireActivity().application as App).orderNumberRepository,
            (requireActivity().application as App).factValueRepository
        )
    }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null
    private val gson = Gson()
    private val jsonParser = JsonParser()

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
        private const val TAG = "Tagg-Reports"
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

        // Загрузка отчётов авторизованного пользователя (новое!)
        sharedViewModel.loadUserReports()

        // Подписка на данные из ViewModel (изменено на userReports)
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.userReports.collectLatest { reports ->  // Изменено!
                if (reports.isEmpty()) {
                    // Опционально: обработка пустого списка
                    Toast.makeText(requireContext(), "Нет отчётов для просмотра. Авторизуйтесь или создайте новый.", Toast.LENGTH_SHORT).show()
                }
                adapter = ExpandableAdapter(mutableListOf<Any>().apply {
                    addAll(reports.map { report ->
                        ParentItem(
                            date = report.date,
                            time = report.time,
                            obj = report.obj,
                            children = listOf(
                                ChildItem(
                                    customer = report.customer,
                                    contract = report.contract,
                                    genContractor = report.genContractor,
                                    repGenContractor = report.repGenContractor,
                                    repSSKGp = report.repSSKGp,
                                    subContractor = report.subContractor,
                                    repSubContractor = report.repSubContractor,
                                    repSSKSub = report.repSSKSub,
                                    transportCustomer = if (report.isEmpty == true) "Транспорт отсутствует" else report.contractTransport,
                                    transportExecutor = if (report.isEmpty == true) "Транспорт отсутствует" else report.executor,
                                    stateNumber = if (report.isEmpty == true) "Транспорт отсутствует" else report.stateNumber,
                                    startDate = if (report.isEmpty == true) "Транспорт отсутствует" else report.startDate,
                                    startTime = if (report.isEmpty == true) "Транспорт отсутствует" else report.startTime,
                                    endDate = if (report.isEmpty == true) "Транспорт отсутствует" else report.endDate,
                                    endTime = if (report.isEmpty == true) "Транспорт отсутствует" else report.endTime,
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
            findNavController().navigate(R.id.action_ReportsFragment_to_StartFragment)
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
            sharedViewModel.filterUserReportsByDateRange(selectedStartDate!!, selectedEndDate!!)  // Изменено!
            Toast.makeText(
                context,
                "Выбрано: $selectedStartDate - $selectedEndDate",
                Toast.LENGTH_SHORT
            ).show()
        }
        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun showExportFormatDialog() {
        val formats = arrayOf("Excel (XLSX)", "CSV")
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите формат экспорта")
            .setItems(formats) { _, which ->
                when (which) {
                    0 -> checkStoragePermission("xlsx")
                    1 -> checkStoragePermission("csv")
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
                exportData(selectedStartDate!!, selectedEndDate!!, "xlsx") // По умолчанию XLSX
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
                    sharedViewModel.getUserReportsForExport(startDate, endDate)  // Изменено!
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

                if (format == "xlsx") {
                    exportToExcel(startDate, endDate, reports, fileName)
                } else if (format == "csv") {
                    exportToCsv(startDate, endDate, reports, fileName)
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
            "№ п/п", "Дата оформления", "Время оформления", "Уникальный номер сотрудника", "Режим работы сотрудника",
            "Заказчик", "Договор СК", "Объект", "Участок", "Генподрядчик",
            "Представитель генподрядчика", "Представитель ССК ПО (ГП)", "Субподрядчик",
            "Представитель субподрядчика", "Представитель ССК ПО (Суб)",
            "Исполнитель по транспорту", "Договор по транспорту", "Госномер",
            "Дата начала поездки", "Время начала поездки", "Дата окончания поездки", "Время окончания поездки",
            "Название прибора/оборудования", "Комплекс работ", "Тип работы", "Номер предписания",
            "Отчет о проделанной работе", "Замечания к документации",
            "ID объекта", "Комплекс работ (Фиксация)", "Тип работы (Фиксация)", "Единицы измерения",
            "Значение по плану", "Значение по факту", "Результат"
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
                    outputStream.write(
                        byteArrayOf(
                            0xEF.toByte(),
                            0xBB.toByte(),
                            0xBF.toByte()
                        )
                    ) // BOM для UTF-8
                    outputStream.write(csvHeader.toByteArray(Charsets.UTF_8))

                    reports.forEach { report ->
                        // Базовые данные (0-21)
                        val baseValues = listOf(
                            report.id.toString(),
                            report.date ?: "",
                            report.time ?: "",
                            report.userName ?: "",
                            report.typeOfWork ?: "",
                            report.customer ?: "",
                            report.contract ?: "",
                            report.obj ?: "",
                            if (report.isManualPlot == true) "Объект не делится на участок" else report.plot
                                ?: "",
                            report.genContractor ?: "",
                            report.repGenContractor ?: "",
                            report.repSSKGp ?: "",
                            report.subContractor ?: "",
                            report.repSubContractor ?: "",
                            report.repSSKSub ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.executor
                                ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.contractTransport
                                ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.stateNumber
                                ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.startDate
                                ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.startTime
                                ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.endDate
                                ?: "",
                            if (report.isEmpty == true) "Транспорт отсутствует" else report.endTime
                                ?: ""
                        )

                        // Парсинг JSON
                        val controlRowsJson = jsonParser.parse(report.controlRows).asJsonArray
                        val fixVolumesRowsJson = jsonParser.parse(report.fixVolumesRows).asJsonArray

                        // Главная строка: базовые + первые записи control/fix
                        val mainValues = mutableListOf<String>().apply {
                            addAll(baseValues) // 0-21
                            addAll(List(13) { "" }) // 22-34 пустые
                        }

                        // Первая запись controlRows (если есть) — в той же строке, 22-27
                        if (controlRowsJson.size() > 0) {
                            val jsonObject = controlRowsJson[0].asJsonObject
                            mainValues[22] = getJsonValue(jsonObject, "equipmentName") ?: ""
                            mainValues[23] = getJsonValue(jsonObject, "complexOfWork") ?: ""
                            mainValues[24] = getJsonValue(jsonObject, "typeOfWork") ?: ""
                            mainValues[25] = getJsonValue(jsonObject, "orderNumber") ?: ""
                            mainValues[26] = getJsonValue(jsonObject, "report") ?: ""
                            mainValues[27] = getJsonValue(jsonObject, "remarks") ?: ""
                        }

                        // Первая запись fixVolumesRows (если есть) — в той же строке, 28-34
                        if (fixVolumesRowsJson.size() > 0) {
                            val jsonObject = fixVolumesRowsJson[0].asJsonObject
                            mainValues[28] = getJsonValue(jsonObject, "ID_object") ?: ""
                            mainValues[29] = getJsonValue(jsonObject, "complexOfWork") ?: ""
                            mainValues[30] = getJsonValue(jsonObject, "projectWorkType") ?: ""
                            mainValues[31] = getJsonValue(jsonObject, "measure") ?: ""
                            mainValues[32] = getJsonValue(jsonObject, "plan") ?: ""
                            mainValues[33] = getJsonValue(jsonObject, "fact") ?: ""
                            mainValues[34] = getJsonValue(jsonObject, "result") ?: ""
                        }

                        // Записываем главную строку
                        outputStream.write((mainValues.joinToString(separator) { it.escapeCsv() } + "\n").toByteArray(
                            Charsets.UTF_8
                        ))

                        // Дополнительные controlRows (2-я и далее, если есть) — новые строки, только 22-27
                        for (i in 1 until controlRowsJson.size()) {
                            val jsonObject = controlRowsJson[i].asJsonObject
                            val controlValues = List(22) { "" } + listOf( // 0-21 пустые
                                getJsonValue(jsonObject, "equipmentName") ?: "", // 22
                                getJsonValue(jsonObject, "complexOfWork") ?: "", // 23
                                getJsonValue(jsonObject, "typeOfWork") ?: "", // 24
                                getJsonValue(jsonObject, "orderNumber") ?: "", // 25
                                getJsonValue(jsonObject, "report") ?: "", // 26
                                getJsonValue(jsonObject, "remarks") ?: "", // 27
                                "", "", "", "", "", "", "" // 28-34 пустые
                            )
                            outputStream.write((controlValues.joinToString(separator) { it.escapeCsv() } + "\n").toByteArray(
                                Charsets.UTF_8
                            ))
                        }

                        // Дополнительные fixVolumesRows (2-я и далее, если есть) — новые строки, только 28-34
                        for (i in 1 until fixVolumesRowsJson.size()) {
                            val jsonObject = fixVolumesRowsJson[i].asJsonObject
                            val fixVolumeValues = List(28) { "" } + listOf( // 0-27 пустые
                                getJsonValue(jsonObject, "ID_object") ?: "", // 28
                                getJsonValue(jsonObject, "complexOfWork") ?: "", // 29
                                getJsonValue(jsonObject, "projectWorkType") ?: "", // 30
                                getJsonValue(jsonObject, "measure") ?: "", // 31
                                getJsonValue(jsonObject, "plan") ?: "", // 32
                                getJsonValue(jsonObject, "fact") ?: "", // 33
                                getJsonValue(jsonObject, "result") ?: "" // 34
                            )
                            outputStream.write((fixVolumeValues.joinToString(separator) { it.escapeCsv() } + "\n").toByteArray(
                                Charsets.UTF_8
                            ))
                        }
                    }
                    delay(1000)
                    Toast.makeText(
                        requireContext(),
                        "Файл сохранен в папке Загрузки: $fileName",
                        Toast.LENGTH_LONG
                    ).show()
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка при создании файла",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                        writer.write("\uFEFF") // BOM для UTF-8
                        writer.append(csvHeader)
                        reports.forEach { report ->
                            // Базовые данные (0-21)
                            val baseValues = listOf(
                                report.id.toString(),
                                report.date ?: "",
                                report.time ?: "",
                                report.userName ?: "",
                                report.typeOfWork ?: "",
                                report.customer ?: "",
                                report.contract ?: "",
                                report.obj ?: "",
                                if (report.isManualPlot == true) "Объект не делится на участок" else report.plot ?: "",
                                report.genContractor ?: "",
                                report.repGenContractor ?: "",
                                report.repSSKGp ?: "",
                                report.subContractor ?: "",
                                report.repSubContractor ?: "",
                                report.repSSKSub ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.executor ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.contractTransport ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.stateNumber ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.startDate ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.startTime ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.endDate ?: "",
                                if (report.isEmpty == true) "Транспорт отсутствует" else report.endTime ?: ""
                            )

                            // Парсинг JSON
                            val controlRowsJson = jsonParser.parse(report.controlRows).asJsonArray
                            val fixVolumesRowsJson = jsonParser.parse(report.fixVolumesRows).asJsonArray

                            // Главная строка: базовые + первые записи control/fix
                            val mainValues = mutableListOf<String>().apply {
                                addAll(baseValues) // 0-21
                                addAll(List(13) { "" }) // 22-34 пустые
                            }

                            // Первая запись controlRows (если есть) — в той же строке, 22-27
                            if (controlRowsJson.size() > 0) {
                                val jsonObject = controlRowsJson[0].asJsonObject
                                mainValues[22] = getJsonValue(jsonObject, "equipmentName") ?: ""
                                mainValues[23] = getJsonValue(jsonObject, "complexOfWork") ?: ""
                                mainValues[24] = getJsonValue(jsonObject, "typeOfWork") ?: ""
                                mainValues[25] = getJsonValue(jsonObject, "orderNumber") ?: ""
                                mainValues[26] = getJsonValue(jsonObject, "report") ?: ""
                                mainValues[27] = getJsonValue(jsonObject, "remarks") ?: ""
                            }

                            // Первая запись fixVolumesRows (если есть) — в той же строке, 28-34
                            if (fixVolumesRowsJson.size() > 0) {
                                val jsonObject = fixVolumesRowsJson[0].asJsonObject
                                mainValues[28] = getJsonValue(jsonObject, "ID_object") ?: ""
                                mainValues[29] = getJsonValue(jsonObject, "complexOfWork") ?: ""
                                mainValues[30] = getJsonValue(jsonObject, "projectWorkType") ?: ""
                                mainValues[31] = getJsonValue(jsonObject, "measure") ?: ""
                                mainValues[32] = getJsonValue(jsonObject, "plan") ?: ""
                                mainValues[33] = getJsonValue(jsonObject, "fact") ?: ""
                                mainValues[34] = getJsonValue(jsonObject, "result") ?: ""
                            }

                            // Записываем главную строку
                            writer.append(mainValues.joinToString(separator) { it.escapeCsv() } + "\n")

                            // Дополнительные controlRows (2-я и далее, если есть) — новые строки, только 22-27
                            for (i in 1 until controlRowsJson.size()) {
                                val jsonObject = controlRowsJson[i].asJsonObject
                                val controlValues = List(22) { "" } + listOf( // 0-21 пустые
                                    getJsonValue(jsonObject, "equipmentName") ?: "", // 22
                                    getJsonValue(jsonObject, "complexOfWork") ?: "", // 23
                                    getJsonValue(jsonObject, "typeOfWork") ?: "", // 24
                                    getJsonValue(jsonObject, "orderNumber") ?: "", // 25
                                    getJsonValue(jsonObject, "report") ?: "", // 26
                                    getJsonValue(jsonObject, "remarks") ?: "", // 27
                                    "", "", "", "", "", "", "" // 28-34 пустые
                                )
                                writer.append(controlValues.joinToString(separator) { it.escapeCsv() } + "\n")
                            }

                            // Дополнительные fixVolumesRows (2-я и далее, если есть) — новые строки, только 28-34
                            for (i in 1 until fixVolumesRowsJson.size()) {
                                val jsonObject = fixVolumesRowsJson[i].asJsonObject
                                val fixVolumeValues = List(28) { "" } + listOf( // 0-27 пустые
                                    getJsonValue(jsonObject, "ID_object") ?: "", // 28
                                    getJsonValue(jsonObject, "complexOfWork") ?: "", // 29
                                    getJsonValue(jsonObject, "projectWorkType") ?: "", // 30
                                    getJsonValue(jsonObject, "measure") ?: "", // 31
                                    getJsonValue(jsonObject, "plan") ?: "", // 32
                                    getJsonValue(jsonObject, "fact") ?: "", // 33
                                    getJsonValue(jsonObject, "result") ?: "" // 34
                                )
                                writer.append(fixVolumeValues.joinToString(separator) { it.escapeCsv() } + "\n")
                            }
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
            setFont(workbook.createFont().apply { bold = true })
            setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index)
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
        }

        val headers = listOf(
            "№ п/п", "Дата оформления", "Время оформления", "Уникальный номер сотрудника", "Режим работы сотрудника",
            "Заказчик", "Договор СК", "Объект", "Участок", "Генподрядчик",
            "Представитель генподрядчика", "Представитель ССК ПО (ГП)", "Субподрядчик",
            "Представитель субподрядчика", "Представитель ССК ПО (Суб)",
            "Исполнитель по транспорту", "Договор по транспорту", "Госномер",
            "Дата начала поездки", "Время начала поездки", "Дата окончания поездки", "Время окончания поездки",
            "Название прибора/оборудования", "Комплекс работ", "Тип работы", "Номер предписания",
            "Отчет о проделанной работе", "Замечания к документации",
            "ID объекта", "Комплекс работ (Фиксация)", "Тип работы (Фиксация)", "Единицы измерения",
            "Значение по плану", "Значение по факту", "Результат"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        var rowIndex = 1
        reports.forEach { report ->
//            println("Processing report ID: ${report.id}, rowIndex start: $rowIndex") // ЛОГ: начало отчёта
            Log.d("Rep","Processing report ID: ${report.id}, rowIndex start: $rowIndex")
            // Базовые значения для столбцов 0-21
            val baseValues = listOf(
                report.id.toString(),
                report.date ?: "",
                report.time ?: "",
                report.userName ?: "",
                report.typeOfWork ?: "",
                report.customer ?: "",
                report.contract ?: "",
                report.obj ?: "",
                if (report.isManualPlot == true) "Объект не делится на участок" else report.plot ?: "",
                report.genContractor ?: "",
                report.repGenContractor ?: "",
                report.repSSKGp ?: "",
                report.subContractor ?: "",
                report.repSubContractor ?: "",
                report.repSSKSub ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.executor ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.contractTransport ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.stateNumber ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.startDate ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.startTime ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.endDate ?: "",
                if (report.isEmpty == true) "Транспорт отсутствует" else report.endTime ?: ""
            ) // 22 элемента (0-21)

            // Парсинг JSON
            val controlRowsJson = jsonParser.parse(report.controlRows).asJsonArray
            val fixVolumesRowsJson = jsonParser.parse(report.fixVolumesRows).asJsonArray
//            println("controlRows size: ${controlRowsJson.size()}, fixVolumesRows size: ${fixVolumesRowsJson.size()}") // ЛОГ: размеры
            Log.d("Rep","controlRows size: ${controlRowsJson.size()}, fixVolumesRows size: ${fixVolumesRowsJson.size()}")

            // Создаём главную строку: базовые + первые записи control/fix
            val mainRow = sheet.createRow(rowIndex++)
            Log.d("Rep", "mainRow: $mainRow")

            Log.d("Rep","Создана главная строка на: ${rowIndex - 1}, следующий rowIndex: $rowIndex")
//            println("Created main row at: ${rowIndex - 1}, next rowIndex: $rowIndex") // ЛОГ: главная строка

            // Инициализируем значения для главной строки (35 столбцов) как MutableList
            val mainValues = mutableListOf<String>().apply {
                addAll(baseValues) // 0-21
                addAll(List(13) { "" }) // 22-34 пустые
            } // Теперь 35 элементов, можно менять по индексу

            // Первая запись controlRows (если есть) — в той же строке, 22-27
            if (controlRowsJson.size() > 0) {
                val jsonObject = controlRowsJson[0].asJsonObject
                mainValues[22] = getJsonValue(jsonObject, "equipmentName") ?: ""
                mainValues[23] = getJsonValue(jsonObject, "complexOfWork") ?: ""
                mainValues[24] = getJsonValue(jsonObject, "typeOfWork") ?: ""
                mainValues[25] = getJsonValue(jsonObject, "orderNumber") ?: ""
                mainValues[26] = getJsonValue(jsonObject, "report") ?: ""
                mainValues[27] = getJsonValue(jsonObject, "remarks") ?: ""
            }

            // Первая запись fixVolumesRows (если есть) — в той же строке, 28-34
            if (fixVolumesRowsJson.size() > 0) {
                val jsonObject = fixVolumesRowsJson[0].asJsonObject
                mainValues[28] = getJsonValue(jsonObject, "ID_object") ?: ""
                mainValues[29] = getJsonValue(jsonObject, "complexOfWork") ?: ""
                mainValues[30] = getJsonValue(jsonObject, "projectWorkType") ?: ""
                mainValues[31] = getJsonValue(jsonObject, "measure") ?: ""
                mainValues[32] = getJsonValue(jsonObject, "plan") ?: ""
                mainValues[33] = getJsonValue(jsonObject, "fact") ?: ""
                mainValues[34] = getJsonValue(jsonObject, "result") ?: ""
            }

            // Записываем главную строку
            mainValues.forEachIndexed { colIndex, value ->
                mainRow.createCell(colIndex).setCellValue(value)
            }

            // Дополнительные controlRows (2-я и далее, если есть) — новые строки, только 22-27
            for (i in 1 until controlRowsJson.size()) {
                Log.d("Rep","Размер controlRowsJson: ${controlRowsJson.size()}")
                Log.d("Rep","Создание дополнительной control row #$i на rowIndex: $rowIndex") // Log перед control
                val jsonObject = controlRowsJson[i].asJsonObject
                val controlRowData = sheet.createRow(rowIndex)
                Log.d("Rep","Создание дополнительной control row #$i, следующий rowIndex: $rowIndex") // Log после control
                val controlValues = List(22) { "" } + listOf( // 0-21 пустые
                    getJsonValue(jsonObject, "equipmentName") ?: "", // 22
                    getJsonValue(jsonObject, "complexOfWork") ?: "", // 23
                    getJsonValue(jsonObject, "typeOfWork") ?: "", // 24
                    getJsonValue(jsonObject, "orderNumber") ?: "", // 25
                    getJsonValue(jsonObject, "report") ?: "", // 26
                    getJsonValue(jsonObject, "remarks") ?: "", // 27
                    "", "", "", "", "", "", "" // 28-34 пустые
                )
                controlValues.forEachIndexed { colIndex, value ->
                    controlRowData.createCell(colIndex).setCellValue(value)
                }
            }

            // Дополнительные fixVolumesRows (2-я и далее, если есть) — новые строки, только 28-34
            for (i in 1 until fixVolumesRowsJson.size()) {
                Log.d("Rep","Размер fixVolumesRowsJson: ${fixVolumesRowsJson.size()}")
                Log.d("Rep","Создание дополнительной fix row #$i на rowIndex: $rowIndex") // Log перед control
                val jsonObject = fixVolumesRowsJson[i].asJsonObject
                val fixVolumeRowData = sheet.createRow(rowIndex++)
                Log.d("Rep","Создание дополнительной fix row #$i, следующий rowIndex: $rowIndex") // Log после control
                val fixVolumeValues = List(28) { "" } + listOf( // 0-27 пустые
                    getJsonValue(jsonObject, "ID_object") ?: "", // 28
                    getJsonValue(jsonObject, "complexOfWork") ?: "", // 29
                    getJsonValue(jsonObject, "projectWorkType") ?: "", // 30
                    getJsonValue(jsonObject, "measure") ?: "", // 31
                    getJsonValue(jsonObject, "plan") ?: "", // 32
                    getJsonValue(jsonObject, "fact") ?: "", // 33
                    getJsonValue(jsonObject, "result") ?: "" // 34
                )
                Log.d("Rep","Finished report ID: ${report.id}, final rowIndex for next: $rowIndex") // Log после control
                fixVolumeValues.forEachIndexed { colIndex, value ->
                    fixVolumeRowData.createCell(colIndex).setCellValue(value)
                }
            }
        }

        headers.indices.forEach { index ->
            sheet.setColumnWidth(index, 15 * 256)
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

    // Вспомогательная функция для получения значения по ключу из JSON
    private fun getJsonValue(jsonObject: JsonObject, key: String): String? {
        return jsonObject.get(key)?.let {
            when {
                it.isJsonNull -> null
                it.isJsonPrimitive -> it.asString
                else -> it.toString() // Для сложных типов (если есть)
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
    if (this.contains(",") || this.contains(";") || this.contains("\"") || this.contains("\n")) {
        return "\"${this.replace("\"", "\"\"")}\""
    }
    return this
}