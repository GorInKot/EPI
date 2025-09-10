package com.example.epi

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import com.example.epi.DataBase.ExtraDatabase.ExtraDatabaseHelper
import com.example.epi.DataBase.FactValue.FactValueRepository
import com.example.epi.DataBase.OrderNumber.OrderNumber
import com.example.epi.DataBase.OrderNumber.OrderNumberRepository
import com.example.epi.DataBase.PlanValue.PlanValue
import com.example.epi.DataBase.PlanValue.PlanValueRepository
import com.example.epi.DataBase.Report.Report
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.User
import com.example.epi.DataBase.User.UserRepository
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.Fragments.Reports.SendReport.Model.InfoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.jar.Manifest
import kotlin.contracts.contract

class SharedViewModel(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val context: Context,
    private val planValueRepository: PlanValueRepository,
    private val orderNumberRepository: OrderNumberRepository,
    private val factValueRepository: FactValueRepository
) : ViewModel() {

    // region Общие параметры
    companion object {
        private val TAG = "Tagg-SVM"
    }

    // Инициализация даты и времени
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val gson = Gson()

    // Инициализация extra_db.db
    private val extraDbHelper: ExtraDatabaseHelper by lazy {
        ExtraDatabaseHelper(context = context.applicationContext)
    }

    fun updateDateTime() {
        val now = Calendar.getInstance()
        _currentDate.postValue(dateFormat.format(now.time))
        _currentTime.postValue(timeFormat.format(now.time))
    }

    // endregion

    // region Данные общие для всех фрагментов

    // Поле для гарантии создания отчета и его обновления на каждом этапе заполнения
    // Это поле хранит ID отчета, созданного на экране ArrangementFragment
    private val _currentReportId = MutableLiveData<Long?>()
    val currentReportId: LiveData<Long?> get() = _currentReportId

    private val _currentDate = MutableLiveData<String>(dateFormat.format(Date()))
    val currentDate: LiveData<String> get() = _currentDate

    private val _currentTime = MutableLiveData<String>(timeFormat.format(Date()))
    val currentTime: LiveData<String> get() = _currentTime

    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    // Флаг для отслеживания сохраненного отчета
    private val _isReportSaved = MutableLiveData<Boolean>(false)
    val isReportSaved: LiveData<Boolean> get() = _isReportSaved

    // Метод для обновления выбранного комплекса (вызывается при выборе пользователем)
    // Используется в нескольких фрагментах, оставим в общем
    fun setSelectedComplex(complexName: String) {
        _selectedComplex.value = complexName
        loadTypesOfWork(complexName)
    }

    private val _selectedComplex = MutableLiveData<String>()
    val selectedComplex: LiveData<String> get() = _selectedComplex

    // endregion

    // region ArrangementFragment

    // -------- Договор СК --------
    private val _selectedContract = MutableLiveData<String?>()
    val selectedContract: LiveData<String?> get() = _selectedContract

    // -------- Заказчик --------
    private val _selectedCustomer = MutableLiveData<String?>()
    val selectedCustomer: LiveData<String?> get() = _selectedCustomer

    // -------- Объект --------
    private val _selectedObject = MutableLiveData<String?>()
    val selectedObject: LiveData<String?> get() = _selectedObject

    // -------- Участок --------
    private val _plotText = MutableLiveData<String?>()
    val plotText: LiveData<String?> get() = _plotText

    // Флаг для чекбокса "Объект не делится на участок"
    private val _isManualPlot = MutableLiveData(false)
    val isManualPlot: LiveData<Boolean> get() = _isManualPlot

    // -------- Генподрядчик --------
    private val _selectedContractor = MutableLiveData<String?>()
    val selectedContractor: LiveData<String?> get() = _selectedContractor

    // -------- Представитель Генподрядчика --------
    private val _selectedRepContractor = MutableLiveData<String?>()
    val selectedRepContractor: LiveData<String?> get() = _selectedRepContractor

    // -------- Представитель ССК ПО (ГП) --------
    private val _repSSKGpText = MutableLiveData<String?>()
    val repSSKGpText: LiveData<String?> get() = _repSSKGpText

    // -------- Субподрядчик --------
    private val _selectedSubContractor = MutableLiveData<String?>()
    val selectedSubContractor: LiveData<String?> get() = _selectedSubContractor

    // -------- Представитель субподрядчика --------
    private val _repSubContractorText = MutableLiveData<String?>()
    val repSubContractorText: LiveData<String?> get() = _repSubContractorText

    // -------- Представитель ССК ПО (Суб) --------
    private val _repSSKSubText = MutableLiveData<String?>()
    val repSSKSubText: LiveData<String?> get() = _repSSKSubText

    // Состояние чекбокса "Отсутствует субподрядчик"
    private val _isManualSubContractor = MutableLiveData<Boolean>(false)
    val isManualSubContractor: LiveData<Boolean> get() = _isManualSubContractor

    fun setIsManualSubContractor(isChecked: Boolean) {
        _isManualSubContractor.value = isChecked
        if (isChecked) {
            _selectedSubContractor.value = "Отсутствует субподрядчик"
            _repSubContractorText.value = "Отсутствует субподрядчик"
            _repSSKSubText.value = "Отсутствует субподрядчик"
        } else {
            _selectedSubContractor.value = null
            _repSubContractorText.value = null
            _repSSKSubText.value = null
        }
    }

    // Методы для установки значений
    fun setSelectedContract(value: String?) { _selectedContract.value = value }
    fun setSelectedCustomer(value: String?) { _selectedCustomer.value = value }
    fun setSelectedObject(value: String?) { _selectedObject.value = value }
    fun setPlotText(value: String?) { _plotText.value = value }
    fun setIsManualPlot(value: Boolean) { _isManualPlot.value = value }
    fun setSelectedContractor(value: String?) { _selectedContractor.value = value }
    fun setSelectedRepContractor(value: String?) { _selectedRepContractor.value = value }
    fun setRepSSKGpText(value: String?) { _repSSKGpText.value = value }
    fun setSelectedSubContractor(value: String?) { _selectedSubContractor.value = value }
    fun setRepSubContractorText(value: String?) { _repSubContractorText.value = value }
    fun setRepSSKSubText(value: String?) { _repSSKSubText.value = value }

    fun clearAllData() {
        _selectedContract.value = null
        _selectedCustomer.value = null
        _selectedObject.value = null
        _plotText.value = null
        _isManualPlot.value = false
        _selectedContractor.value = null
        _selectedRepContractor.value = null
        _repSSKGpText.value = null
        _selectedSubContractor.value = null
        _repSubContractorText.value = null
        _repSSKSubText.value = null
        _isManualSubContractor.value = false
        _transportExecutorName.value = null
        _transportContractTransport.value = null
        _transportStateNumber.value = null
        _transportStartDate.value = null
        _transportStartTime.value = null
        _transportEndDate.value = null
        _transportEndTime.value = null
        _orderNumber.value = null
        _isViolation.value = false
        _controlRows.value = emptyList()
        _fixRows.value = emptyList()
        _isTransportAbsent.value = false
        _currentReportId.value = null // Сбрасываем значение _currentReportId
        updateDateTime()
        _isReportSaved.postValue(false)
    }

    fun validateArrangementInputs(
        contract: String?,
        customers: String?,
        objects: String?,
        plotText: String?,
        contractors: String?,
        subContractors: String?,
        repSSKGpText: String?,
        repContractor: String?,
        repSubContractorText: String?,
        repSSKSubText: String?,
        isManualPlot: Boolean
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (contract.isNullOrBlank()) errors["contract"] = "Выберите договор"
        if (customers.isNullOrBlank()) errors["customers"] = "Выберите заказчика"
        if (objects.isNullOrBlank()) errors["objects"] = "Выберите объект"
        if (plotText.isNullOrBlank() && !isManualPlot) errors["plotText"] = "Выберите участок"
        if (contractors.isNullOrBlank()) errors["contractors"] = "Выберите генподрядчика"
        if (subContractors.isNullOrBlank() && subContractors != "Отсутствует субподрядчик") {
            errors["subContractors"] = "Выберите субподрядчика"
        }
        if (repSSKGpText.isNullOrBlank()) errors["repSSKGpText"] = "Введите представителя ССК ПО (ГП)"
        if (repContractor.isNullOrBlank() && repContractor != "Отсутствует субподрядчик") {
            errors["repContractor"] = "Выберите представителя генподрядчика"
        }
        if (repSubContractorText.isNullOrBlank() && repSubContractorText != "Отсутствует субподрядчик") {
            errors["repSubContractorText"] = "Введите представителя субподрядчика"
        }
        if (repSSKSubText.isNullOrBlank() && repSSKSubText != "Отсутствует субподрядчик") {
            errors["repSSKSubText"] = "Введите представителя ССК ПО (Суб)"
        }
        return errors
    }

    suspend fun saveArrangementData(): Long {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving report on thread: ${Thread.currentThread().name}")
            try {

                val arrangementErrors = validateArrangementInputs(
                    contract = _selectedContract.value,
                    customers = _selectedCustomer.value,
                    objects = _selectedObject.value,
                    plotText = if (_isManualPlot.value == true) "Объект не делится на участки" else _plotText.value ,
                    contractors = _selectedContractor.value,
                    subContractors = _selectedSubContractor.value,
                    repSSKGpText = _repSSKGpText.value,
                    repContractor = _selectedRepContractor.value,
                    repSubContractorText = _repSubContractorText.value,
                    repSSKSubText = _repSSKSubText.value,
                    isManualPlot = _isManualPlot.value ?: false
                )
                if (arrangementErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля заполнены корректно: ${arrangementErrors.values.joinToString()} ")
                        Log.d(TAG, "Arrangement validation errors: ${arrangementErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val employeeNumber = _currentEmployeeNumber.value ?: run {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Пользователь не авторизован")
                        Log.e(TAG, "No employeeNumber available")
                    }
                    return@withContext 0L
                }

                if (_selectedTypeOfWork.value.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Режим работы не выбран")
                        Log.e(TAG, "typeOfWork is not set")
                    }
                    return@withContext 0L
                }

                val report = Report(
                    userName = employeeNumber, // Сохраняем уникальный номер сотрудника
                    typeOfWork = _selectedTypeOfWork.value.orEmpty(), // Добавляем Режим работы
                    date = _currentDate.value.orEmpty(),
                    time = _currentTime.value.orEmpty(),
                    contract = _selectedContract.value.orEmpty(),
                    customer = _selectedCustomer.value.orEmpty(),
                    obj =  _selectedObject.value.orEmpty(),
                    plot = _plotText.value.orEmpty(),
                    genContractor = _selectedContractor.value.orEmpty(),
                    repGenContractor =_selectedRepContractor.value.orEmpty(),
                    repSSKGp = _repSSKGpText.value.orEmpty(),
                    subContractor = _selectedSubContractor.value.orEmpty(),
                    repSubContractor = _repSubContractorText.value.orEmpty(),
                    repSSKSub = _repSSKSubText.value.orEmpty(),
                    // Поля Transport, Control и FixVolumes остаются пустыми
                    executor = "",
                    contractTransport = "",
                    stateNumber = "",
                    startDate = "",
                    startTime = "",
                    endDate = "",
                    endTime = "",
                    orderNumber = "",
                    inViolation = false,
                    equipment = "",
                    complexWork = "",
                    report = "",
                    remarks = "",
                    controlRows = "",
                    fixVolumesRows = "",
                    isEmpty = false,
                    isSend = false,
                    isCompleted = false // Отчет незавершен
                )
//                Log.d(TAG, "UserName: ${report.userName}")
                Log.d(TAG, "Сохранение полного отчета: $report")
                val reportId = reportRepository.saveReport(report)
                Log.d(TAG, "Сохранение отчета с ID: $reportId")
                if (reportId > 0) {
                    withContext(Dispatchers.Main) {
                        _isReportSaved.postValue(true)
                        _currentReportId.postValue(reportId)
                        Log.d(TAG, "Отчет успешно сохранен, isReportSaved изменен на true")
                    }
                }
                reportId
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка сохранения отчета: ${e.message}, Thread: ${Thread.currentThread().name}, StackTrace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при сохранении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    fun loadPreviousReport() {
        viewModelScope.launch {
            try {
                val report = reportRepository.getLastUnsentReport()
                report?.let {
                    // ArrangementFragment
                    _selectedCustomer.value = it.customer
                    Log.d(TAG, "Предыдущий заказчик: ${it.customer}")

                    _selectedContract.value = it.contract
                    Log.d(TAG, "Предыдущий Договор СК: ${it.contract}")

                    _selectedObject.value = it.obj
                    Log.d(TAG, "Предыдущий объект: ${it.obj}")

                    _plotText.value = it.plot
                    Log.d(TAG, "Предыдущий участок: ${it.plot}")

                    _isManualPlot.value = it.plot == "Объект не делится на участки"

                    _selectedContractor.value = it.genContractor
                    Log.d(TAG, "Предыдущий генподрядчик: ${it.genContractor}")

                    _selectedRepContractor.value = it.repGenContractor
                    Log.d(TAG, "Предыдущий представитель генподрядчика: ${it.repGenContractor}")

                    _repSSKGpText.value = it.repSSKGp
                    Log.d(TAG, "Предыдущий ССК ПО (ГП): ${it.repSSKGp}")

                    _selectedSubContractor.value = it.subContractor
                    Log.d(TAG, "Предыдущий субподрядчик: ${it.subContractor}")

                    _repSubContractorText.value = it.repSubContractor
                    Log.d(TAG, "Предыдущий представитель субподрядчика: ${it.repSubContractor}")

                    _repSSKSubText.value = it.repSSKSub
                    Log.d(TAG, "Предыдущий ССК ПО (Суб): ${it.repSSKSub}")
                    _isManualSubContractor.value = it.subContractor == "Отсутствует субподрядчик"

                    // TransportFragment
                    _transportExecutorName.value = it.executor
                    Log.d(TAG, "Предыдущий исполнитель по транспорту: ${it.executor}")

                    _transportContractTransport.value = it.contractTransport

                    Log.d(TAG, "Предыдущий договор по транспорту: ${it.contractTransport}")

                    _transportStateNumber.value = it.stateNumber
                    Log.d(TAG, "Предыдущий госномер: ${it.stateNumber}")

                } ?: Log.d(TAG, "Предыдущий неотправленный отчет не найден.")
            } catch (e: Exception) {
                _errorEvent.postValue("Ошибка при загрузке предыдущего отчета: ${e.message}")
                Log.e(TAG, "Error loading previous report: ${e.message}", e)
            }
        }
    }

    // endregion ArrangementFragment

    // region TransportFragment

    // чекбокс Транспорт отсутствует
    private val _isTransportAbsent = MutableLiveData(false)
    val isTransportAbsent: LiveData<Boolean> get() = _isTransportAbsent

    // исполнитель по транспорту
    private val _transportExecutorName = MutableLiveData<String?>()
    val transportExecutorName: LiveData<String?> get() = _transportExecutorName


    // договор по транспорту
    private val _transportContractTransport = MutableLiveData<String?>()
    val transportContractTransport: LiveData<String?> get() = _transportContractTransport

    // госномер транспорта
    private val _transportStateNumber = MutableLiveData<String?>()
    val transportStateNumber: LiveData<String?> get() = _transportStateNumber

    // дата начала поездки
    private val _transportStartDate = MutableLiveData<String?>()
    val transportStartDate: LiveData<String?> get() = _transportStartDate

    // время начала поездки
    private val _transportStartTime = MutableLiveData<String?>()
    val transportStartTime: LiveData<String?> get() = _transportStartTime

    // дата завершения поездки
    private val _transportEndDate = MutableLiveData<String?>()
    val transportEndDate: LiveData<String?> get() = _transportEndDate

    // время завершения поездки
    private val _transportEndTime = MutableLiveData<String?>()
    val transportEndTime: LiveData<String?> get() = _transportEndTime

    // очистка полей экрана Транспорт
    private val _transportInClearing = MutableLiveData(false)
    val transportInClearing: LiveData<Boolean> get() = _transportInClearing

    // методы для TransportFragment
    fun setTransportAbsent(value: Boolean) { _isTransportAbsent.value = value }
    fun setTransportExecutorName(value: String) { _transportExecutorName.value = value.trim() }
    fun setTransportContractTransport(value: String) { _transportContractTransport.value = value.trim() }
    fun setTransportStateNumber(value: String) { _transportStateNumber.value = value.trim() }
    fun setTransportStartDate(value: String) { _transportStartDate.value = value.trim() }
    fun setTransportStartTime(value: String) { _transportStartTime.value = value.trim() }
    fun setTransportEndDate(value: String) { _transportEndDate.value = value.trim() }
    fun setTransportEndTime(value: String) { _transportEndTime.value = value.trim() }

    // функция для чекбокса Транспорт отсутствует (transportAbsent)
    fun clearTransport() {
        _transportInClearing.value = true
        _transportExecutorName.value = ""
        _transportContractTransport.value = ""
        _transportStateNumber.value = ""
        _transportStartDate.value = ""
        _transportStartTime.value = ""
        _transportEndDate.value = ""
        _transportEndTime.value = ""
        _transportInClearing.value = false
    }

    fun validateTransportInputs(
        isTransportAbsent: Boolean, // чекбокс
        executorName: String?, // исполнитель по транспорту
        contractTransport: String?, // договор по транспорту
        stateNumber: String?, // госномер
        startDate: String?, // дата начала поездки
        startTime: String?, // время начала поездки
        endDate: String?, // дата завершения поездки
        endTime: String? // время завершения поездки
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (isTransportAbsent) return errors

        // исполнитель по транспорту
        if (executorName.isNullOrBlank()) {
            errors["executorName"] = "Укажите исполнителя по транспорту"
        }
        // договор по транспорту
        if (contractTransport.isNullOrBlank()) {
            errors["contractTransport"] = "Укажите договор по транспорту"
        }
        // госномер
        if (stateNumber.isNullOrBlank()) {
            errors["stateNumber"] = "Укажите госномер"
        }
        // дата начала поездки
        if (startDate.isNullOrBlank()) {
            errors["startDate"] = "Укажите дату начала поездки"
        }
        // время начала поездки
        if (startTime.isNullOrBlank()) {
            errors["startTime"] = "Укажите время начала поездки"
        }
        // дата завершения поездки
        if (endDate.isNullOrBlank()) {
            errors["endDate"] = "Укажите дату завершения поездки"
        }
        // время завершения поездки
        if (endTime.isNullOrBlank()) {
            errors["endTime"] = "Укажите время завершения поездки"
        }
        if (!startDate.isNullOrBlank() && !startTime.isNullOrBlank() && !endDate.isNullOrBlank() && !endTime.isNullOrBlank()) {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val start = "${startDate.trim()} ${startTime.trim()}"
            val end = "${endDate.trim()} ${endTime.trim()}"
            try {
                val startParsed = format.parse(start)
                val endParsed = format.parse(end)
                if (startParsed != null && endParsed != null && startParsed.after(endParsed)) {
                    errors["endTime"] = "Окончание не может быть раньше начала"
                }
            } catch (e: Exception) {
                errors["startDate"] = "Ошибка формата даты/времени"
                errors["endDate"] = "Ошибка формата даты/времени"
            }
        }
        return errors
    }

    // Функция валидации госномера
    fun isValidStateNumber(number: String): Boolean {
        return number.matches(Regex("^[АВЕКМНОРСТУХ]\\s\\d{3}\\s[АВЕКМНОРСТУХ]{2}\\s\\d{2,3}$"))
    }

    suspend fun updateTransportReport(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val errors = validateTransportInputs(
                    isTransportAbsent = _isTransportAbsent.value ?: false,
                    executorName = _transportExecutorName.value,
                    contractTransport = _transportContractTransport.value,
                    stateNumber = _transportStateNumber.value,
                    startDate = _transportStartDate.value,
                    startTime = _transportStartTime.value,
                    endDate = _transportEndDate.value,
                    endTime = _transportEndTime.value
                )
                if (errors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля транспорта заполнены корректно: ${errors.values.joinToString()}")
                        Log.e(TAG, "Transport: Validation failed in updateTransportReport: $errors")
                    }
                    return@withContext 0L
                }
                val reportId = _currentReportId.value ?: run {
                    Log.e(TAG, "Transport: No report ID available")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: нет текущего отчета для обновления")
                    }
                    return@withContext 0L
                }
                val existingReport = reportRepository.getReportById(reportId)
                if (existingReport == null) {
                    Log.e(TAG, "Transport: No report found with ID: $reportId")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: отчет не найден")
                    }
                    return@withContext 0L
                }
                val updatedReport = existingReport.copy(
                    executor = if (_isTransportAbsent.value == true) "" else _transportExecutorName.value.orEmpty(),
                    contractTransport = if (_isTransportAbsent.value == true) "" else _transportContractTransport.value.orEmpty(),
                    stateNumber = if (_isTransportAbsent.value == true) "" else _transportStateNumber.value.orEmpty(),
                    startDate = if (_isTransportAbsent.value == true) "" else _transportStartDate.value.orEmpty(),
                    startTime = if (_isTransportAbsent.value == true) "" else _transportStartTime.value.orEmpty(),
                    endDate = if (_isTransportAbsent.value == true) "" else _transportEndDate.value.orEmpty(),
                    endTime = if (_isTransportAbsent.value == true) "" else _transportEndTime.value.orEmpty(),
                    isEmpty = _isTransportAbsent.value ?: false,
                    isCompleted = false // Отчет остается незавершенным
                )
                Log.d(TAG, "Transport: Updating Report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d(TAG, "Transport: Report updated successfully with ID: ${updatedReport.id}")
                updatedReport.id
            } catch (e: Exception) {
                Log.e(TAG, "Transport: Error in updateTransportReport: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    // endregion TransportFragment

    // region ControlFragment

    private var orderCounter = 1

    // номер предписания
    private val _orderNumber = MutableLiveData<String?>("")
    val orderNumber: LiveData<String?> get() = _orderNumber

    // чекбокс нарушение (есть или нет)
    private val _isViolation = MutableLiveData<Boolean>(false)
    val isViolation: LiveData<Boolean> get() = _isViolation

    // чекбокс оборудование (отсутсвует или нет)
    private val _isEquipmentAbsent = MutableLiveData<Boolean>(false)
    val isEquipmentAbsent: LiveData<Boolean> get() = _isEquipmentAbsent

    // строки для recyclerView
    private val _controlRows = MutableLiveData<List<ControlRow>>(emptyList())
    val controlRows: LiveData<List<ControlRow>> get() = _controlRows

    // список для выпадающего списка "наименование прибора/оборудования"
    val equipmentNames = MutableLiveData<List<String>>(
        listOf(
            "Прибор 1", "Прибор 2", "Прибор 3", "Прибор 4", "Прибор 5", "Прибор 6",
            "Прибор 7", "Прибор 8", "Прибор 9", "Прибор 10", "Прибор 11", "Прибор 12"
        )
    )

    fun setEquipmentAbsent(checked: Boolean) {
        _isEquipmentAbsent.value = checked
        if (checked) {
            equipmentNames.value = listOf("Оборудование отсутствует")
        } else {
            equipmentNames.value = listOf(
                "Прибор 1", "Прибор 2", "Прибор 3", "Прибор 4", "Прибор 5", "Прибор 6",
                "Прибор 7", "Прибор 8", "Прибор 9", "Прибор 10", "Прибор 11", "Прибор 12"
            )
        }
    }

    // данные из extra_db.db из таблицы ComplexOfWork
    private val _controlsComplexOfWork = MutableLiveData<List<String>>()
    val controlsComplexOfWork: LiveData<List<String>> get() = _controlsComplexOfWork

    private val _controlTypesOfWork = MutableLiveData<List<String>>()
    val controlTypesOfWork: LiveData<List<String>> get() = _controlTypesOfWork

    // Функция создания номера предписания
    fun generateOrderNumber() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_isViolation.value == true) {
                    withContext(Dispatchers.Main) {
                        _orderNumber.value = "Нет нарушения"
                    }
                    return@launch
                }

                val employeeNumber = _currentEmployeeNumber.value ?: run {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Пользователь не авторизован")
                        _orderNumber.value = "Ошибка: нет номера сотрудника"
                    }
                    return@launch
                }

                val dateStr = _currentDate.value.takeIf { !it.isNullOrBlank() }
                    ?: dateFormat.format(Date())
                val formatterInputDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val date = LocalDate.parse(dateStr, formatterInputDate)
                val formattedDate = date.format(DateTimeFormatter.ofPattern("MMdd"))

                // Получаем последний счётчик для сотрудника и даты
                val lastCounter = orderNumberRepository.getLastOrderCounter(employeeNumber, dateStr)
                val newCounter = (lastCounter ?: 0) + 1 // Используем 0, если lastCouner == null

                // Формируем новый номер предписания
                val generatedNumber = "$employeeNumber.$formattedDate.$newCounter"

                // Сохраняем новый номер в базу
                val orderNumber = OrderNumber(
                    employeeNumber = employeeNumber,
                    date = dateStr,
                    orderCounter = newCounter
                )
                orderNumberRepository.saveOrderNumber(orderNumber)

                withContext(Dispatchers.Main) {
                    _orderNumber.value = generatedNumber
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _orderNumber.value = "Ошибка генерации номера"
                    _errorEvent.postValue("Ошибка генерации номера: ${e.message}")
                }
                Log.e(TAG, "Ошибка при создании номера предписания: ${e.message}", e)
            }
        }
    }

    // Установка чекбокса "нарушение"
    fun setViolation(checked: Boolean) {
        _isViolation.value = checked
        _orderNumber.value = if (checked) "Нет нарушения" else ""
    }

    // Добавление строки
    fun addRow(row: ControlRow) {
        val currentList = _controlRows.value?.toMutableList() ?: mutableListOf()
        currentList.add(row)
        _controlRows.value = currentList
    }

    // Удаление строки
    fun removeRow(row: ControlRow) {
        _controlRows.value = _controlRows.value?.filterNot { it == row }
    }

    // Обновление строки
    fun updateRow(oldRow: ControlRow, newRow: ControlRow) {
        val currentList = _controlRows.value?.toMutableList() ?: return
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _controlRows.value = currentList
        } else {
            Log.w(TAG, "ControlFragment: Row not found: $oldRow")
        }
    }

    // Валидация строк для ControlFragment
    fun validateRowInput(input: RowInput): RowValidationResult {
        return when {
            input.equipmentName.isBlank() && !input.isEquipmentAbsent ->
                RowValidationResult.Invalid("Оборудование не указано")
            input.equipmentName == "Оборудование отсутствует" && !input.isEquipmentAbsent ->
                RowValidationResult.Invalid("Оборудование не указано, снимите галочку 'Оборудование отсутствует'")
            input.typeOfWork.isBlank() -> RowValidationResult.Invalid("Вид работ не указан")
            input.report.isBlank() -> RowValidationResult.Invalid("Отчет не заполнен")
            input.remarks.isBlank() -> RowValidationResult.Invalid("Примечание не заполнено")
            !input.isViolationChecked && input.orderNumber.isBlank() ->
                RowValidationResult.Invalid("Номер предписания обязателен")
            else -> RowValidationResult.Valid()
        }
    }

    fun validateControlInputs(
        isViolation: Boolean,
        orderNumber: String?,
        controlRows: List<ControlRow>?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (!isViolation && orderNumber.isNullOrBlank()) errors["orderNumber"] = "Укажите номер предписания"
        if (controlRows.isNullOrEmpty()) errors["controlRows"] = "Добавьте хотя бы одну строку контроля"
        else {
            controlRows.forEachIndexed { index, row ->
                val input = RowInput(
                    equipmentName = row.equipmentName,
                    complexOfWork = row.complexOfWork,
                    typeOfWork = row.typeOfWork,
                    report = row.report,
                    remarks = row.remarks,
                    orderNumber = row.orderNumber,
                    isViolationChecked = isViolation,
                    isEquipmentAbsent = row.isEquipmentAbsent // Используем состояние из строки
                )
                when (val result = validateRowInput(input)) {
                    is RowValidationResult.Invalid -> errors["row_$index"] = result.reason
                    else -> {}
                }
            }
        }
        return errors
    }

    suspend fun updateControlReport(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val errors = validateControlInputs(
                    isViolation = _isViolation.value ?: false,
                    orderNumber = _orderNumber.value,
                    controlRows = _controlRows.value
                )
                if (errors.isNotEmpty()) {
                    Log.e(TAG, "Control: Validation failed: $errors")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля заполнены корректно: ${errors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val reportId = _currentReportId.value ?: run {
                    Log.e(TAG, "Control: No report ID available")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: нет текущего отчета для обновления")
                    }
                    return@withContext 0L
                }

                val existingReport = reportRepository.getReportById(reportId)
                if (existingReport == null) {
                    Log.e(TAG, "Control: No report found with ID: $reportId")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: отчет не найден")
                    }
                    return@withContext 0L
                }

                val controlRowsJson = gson.toJson(_controlRows.value)
                val firstRow = _controlRows.value?.firstOrNull()

                val updatedReport = existingReport.copy(
                    orderNumber = if (_isViolation.value == true) "Нет нарушения" else _orderNumber.value.orEmpty(),
                    inViolation = _isViolation.value ?: false,
                    noEquipmentName = _isEquipmentAbsent.value ?: false,
                    equipment = firstRow?.equipmentName ?: "",
                    complexWork = firstRow?.typeOfWork ?: "",
                    report = firstRow?.report ?: "",
                    remarks = firstRow?.remarks ?: "",
                    controlRows = controlRowsJson,
                    isCompleted = false // Отчет остается незавершенным
                )
                Log.d(TAG, "Control: Updating Report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d(TAG, "Control: Report updated successfully with ID: ${updatedReport.id}")
                updatedReport.id
            } catch (e: Exception) {
                Log.e(TAG, "Control: Error in updateControlReport: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    //endregion

    //region FixVolumesFragment
    // -------- FixVolumesFragment --------
    // строки для recyclerView
    private val _fixRows = MutableLiveData<List<FixVolumesRow>>(emptyList())
    val fixRows: LiveData<List<FixVolumesRow>> get() = _fixRows

    // единицы измерения для выпадающего списка
    val fixMeasures = MutableLiveData<List<String>>(
        listOf(
            "-","м", "м2", "м3", "мм", "см", "т", "кг", "шт.", "п.м.", "л",
            "м/ч", "м/с", "градусы", "%", "МПа", "ч", "сут."
        )
    )

    // метод загрузки данных из extra_db из таблицы ComplexOfWork для выпадающего списка Комплекс работ
    fun loadComplexOfWorks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val complexOfWorks = extraDbHelper.getComplexOfWorks()
                withContext(Dispatchers.Main) {
                    _controlsComplexOfWork.value = complexOfWorks
                    Log.d(TAG, "Loaded ComplexOfWorks: $complexOfWorks")
                    if (complexOfWorks.isNotEmpty()) {
                        _selectedComplex.value = complexOfWorks[0] // Устанавливаем первый комплекс по умолчанию
                        loadTypesOfWork(complexOfWorks[0]) // Загружаем виды работ для первого комплекса
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ComplexOfWorks: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка загрузки данных из ComplexOfWork: ${e.message}")
                }
            }
        }
    }

    // метод загрузки данных из extra_db из таблицы TypesOfWork для выпадающего списка Вид работ
    fun loadTypesOfWork(complexOfWorkName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val typesOfWork = extraDbHelper.getTypesOfWork(complexOfWorkName)
                withContext(Dispatchers.Main) {
                    _controlTypesOfWork.value = typesOfWork // Обновляем список видов работ
                    _selectedComplex.value = complexOfWorkName // Синхронизируем выбранный комплекс
                    Log.d(TAG, "Loaded TypesOfWork for $complexOfWorkName: $typesOfWork")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading TypesOfWork: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка загрузки данных из TypesOfWork: ${e.message}")
                }
            }
        }
    }

    // Функция добавления данных в recyclerView
    fun addFixRow(fixRow: FixVolumesRow) {
        viewModelScope.launch {
            val result = validateAndCalculateRemainingVolume(fixRow)
            if (result is RowValidationResult.Valid) {
                val current = _fixRows.value?.toMutableList() ?: mutableListOf()
                current.add(fixRow.copy(result = result.remainingVolume?.toString() ?: "0.0"))
                _fixRows.value = recalculateFixRows(current)
            } else if (result is RowValidationResult.Invalid) {
                _errorEvent.postValue(result.reason)
            }
        }
    }

    // Функция удаления данных из recyclerView
    fun removeFixRow(fixRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: return
        current.remove(fixRow)
        _fixRows.value = recalculateFixRows(current)
    }

    // Функция обновления данных в строке в recyclerView
    fun updateFixRow(oldRow: FixVolumesRow, newRow: FixVolumesRow) {
        viewModelScope.launch {
            val result = validateAndCalculateRemainingVolume(newRow, oldRow)
            if (result is RowValidationResult.Valid) {
                val current = _fixRows.value?.toMutableList() ?: return@launch
                val index = current.indexOfFirst { it == oldRow }
                if (index != -1) {
                    current[index] = newRow.copy(result = result.remainingVolume?.toString() ?: "0.0")
                    _fixRows.value = recalculateFixRows(current)
                } else {
                    Log.w(TAG, "FixVolumesFragment: Row not found: $oldRow")
                    _errorEvent.postValue("Строка не найдена")
                }
            } else if (result is RowValidationResult.Invalid) {
                _errorEvent.postValue(result.reason)
            }
        }
    }

    // TODO - изменить логику функции
    private fun recalculateFixRows(rows: List<FixVolumesRow>): List<FixVolumesRow> {
        Log.d(TAG, "Recalculating rows: $rows")
        val result = rows.groupBy { Triple(it.projectWorkType, it.measure, it.plan) }
            .flatMap { (key, group) ->
                val totalFact = group.sumOf { it.fact.toDoubleOrNull() ?: 0.0 }
                val planValue = group.first().plan.toDoubleOrNull() ?: 0.0
                val remainingVolume = planValue - totalFact
                Log.d(TAG, "Group: $key, totalFact: $totalFact, planValue: $planValue, remainingVolume: $remainingVolume")
                group.map { row ->
                    row.copy(result = if (remainingVolume >= 0) remainingVolume.toString() else "0.0")
                }
            }
        Log.d(TAG, "Recalculated rows: $result")
        return result
    }

    // TODO - изменить логику функции
    fun validateAndCalculateRemainingVolume(input: FixVolumesRow, excludeRow: FixVolumesRow? = null): RowValidationResult {
        // Базовая валидация полей
        when {
            input.ID_object.isBlank() -> return RowValidationResult.Invalid("ID объекта не указан")
            _fixRows.value?.any { it.ID_object != input.ID_object && it != input && it != excludeRow } == true ->
                return RowValidationResult.Invalid("ID объекта должен совпадать для всех строк")
            input.projectWorkType.isBlank() -> return RowValidationResult.Invalid("Вид работ не указан")
            input.measure.isBlank() -> return RowValidationResult.Invalid("Единица измерения не указана")
            input.plan.isBlank() -> return RowValidationResult.Invalid("План не указан")
            input.fact.isBlank() -> return RowValidationResult.Invalid("Факт не указан")
            input.plan.toDoubleOrNull() == null -> return RowValidationResult.Invalid("План должен быть числом")
            input.fact.toDoubleOrNull() == null -> return RowValidationResult.Invalid("Факт должен быть числом")
        }

        // Проверка существования планового значения
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingPlanValues = planValueRepository.getPlanValuesByObjectIdAndComplexAndType(
                    objectId = input.ID_object,
                    complexWork = input.complexOfWork,
                    typeOfWork = input.projectWorkType
                )
                if (existingPlanValues.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Плановое значение для объекта ${input.ID_object}, комплекса ${input.complexOfWork} и вида работ ${input.projectWorkType} уже существует")
                    }
                    return@launch // Прерываем выполнение, если значение уже существует
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при проверке планового значения: ${e.message}")
                }
                Log.e(TAG, "Ошибка при проверке планового значения: ${e.message}", e)
                return@launch
            }
        }

        // Продолжаем валидацию и расчет остаточного объема
        val planValue = input.plan.toDouble()
        Log.d(TAG, "План: $planValue")
        val factValue = input.fact.toDouble()
        Log.d(TAG, "Факт: $factValue")

        val matchingRows = _fixRows.value?.filter {
            it != excludeRow &&
                    it.projectWorkType == input.projectWorkType &&
                    it.measure == input.measure &&
                    it.plan == input.plan
        } ?: emptyList()

        val totalFact = matchingRows.sumOf { it.fact.toDoubleOrNull() ?: 0.0 } + factValue
        val remainingVolume = planValue - totalFact
        Log.d(TAG, "Разница: $remainingVolume")

        if (remainingVolume < 0) {
            return RowValidationResult.Invalid("Сумма фактических значений превышает плановое")
        }

        return RowValidationResult.Valid(remainingVolume)
    }

    // Функция валидации полей ввода
    suspend fun validateFixVolumesInputs(fixRows: List<FixVolumesRow>?): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (fixRows.isNullOrEmpty()) {
            errors["fixRows"] = "Добавьте хотя бы одну строку фиксации объемов"
            return errors
        }

        fixRows.forEachIndexed { index, row ->
            val result = withContext(Dispatchers.IO) {
                validateAndCalculateRemainingVolume(row)
            }
            when (result) {
                is RowValidationResult.Invalid -> {
                    errors["row_$index"] = result.reason
                }
                else -> {}
            }
        }
        return errors
    }

    // Метод для проверки значение по введенному комплексу работ
    suspend fun getPlanValuesByObjectIdAndComplex(
        objectId: String,
        complexWork: String,
    ): List<PlanValue> { // Или PlanValue? в зависимости от DAO
        return withContext(Dispatchers.IO) {
            planValueRepository.getPlanValuesByObjectIdAndComplex(objectId, complexWork)
        }
    }


    // Метод для проверки значение по введенному комплексу работ и виду работы
    suspend fun getPlanValuesByObjectIdAndComplexAndType(
        objectId: String,
        complexWork: String,
        typeOfWork: String
    ): List<PlanValue> { // Или PlanValue? в зависимости от DAO
        return withContext(Dispatchers.IO) {
            planValueRepository.getPlanValuesByObjectIdAndComplexAndType(objectId, complexWork, typeOfWork)
        }
    }

    // Метод обновления отчета
    suspend fun updateFixVolumesReport(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val errors = validateFixVolumesInputs(fixRows = _fixRows.value)
                if (errors.isNotEmpty()) {
                    Log.e(TAG, "FixVolumes: Validation failed: $errors")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля объемов заполнены корректно: ${errors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val reportId = _currentReportId.value ?: run {
                    Log.e(TAG, "FixVolumes: No report ID available")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: нет текущего отчета для обновления")
                    }
                    return@withContext 0L
                }

                val existingReport = reportRepository.getReportById(reportId)
                if (existingReport == null) {
                    Log.e(TAG, "FixVolumes: No report found with ID: $reportId")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: отчет не найден")
                    }
                    return@withContext 0L
                }

                // Сохранение плановых значений в базу
                _fixRows.value?.forEach { row ->
                    val planValue = PlanValue(
                        objectId = row.ID_object,
                        complexWork = row.complexOfWork,
                        typeOfWork = row.projectWorkType,
                        planValue = row.plan.toDoubleOrNull() ?: 0.0,
                        measures = row.measure
                    )
                    planValueRepository.insert(planValue)
                }

                val fixRowsJson = gson.toJson(_fixRows.value)
                val updatedReport = existingReport.copy(
                    fixVolumesRows = fixRowsJson,
                    isCompleted = false // Отчет остается незавершенным
                )
                Log.d(TAG, "FixVolumes: Updating Report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d(TAG, "FixVolumes: Report updated successfully with ID: ${updatedReport.id}")
                updatedReport.id
            } catch (e: Exception) {
                Log.e(TAG, "FixVolumes: Error in updateFixVolumesReport: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    //endregion

    // region *PlanValue

    private val _planValues = MutableLiveData<List<PlanValue>>(emptyList())
    val planValues: LiveData<List<PlanValue>> get() = _planValues

    // Функция загрузки Плановых значений
    suspend fun loadPlanValues(objectId: String? = null)  {
        withContext(Dispatchers.IO) {
            val values = if (objectId != null) {
                planValueRepository.getPlanValuesByObjectId(objectId)
            } else {
                planValueRepository.getAllPlanValues().first() // Берем первое значение из потока
            }
            Log.d("DEBUG-VM", "Загружено ${values.size} записей для objectId=$objectId")
            withContext(Dispatchers.Main) {
                _planValues.value = values
            }
        }
    }

    // Функция очистки Плановых значений на экране
    fun clearPlanValues() {
        _planValues.value = emptyList()
        setSelectedObject(null)
    }

    // Функция добавления Плановых значений в таблицу plan_values в Room
    suspend fun addPlanValue(planValue: PlanValue) {
        withContext(Dispatchers.IO) {
            planValueRepository.insert(planValue)
            loadPlanValues(planValue.objectId)
        }
    }

    // --- Редактирование существующего PlanValue ---
    suspend fun updatePlanValue(updated: PlanValue) {
        withContext(Dispatchers.IO) {
            planValueRepository.update(updated)
            loadPlanValues(updated.objectId)
        }
    }

    // --- Удаление PlanValue ---
    suspend fun deletePlanValue(planValue: PlanValue) {
        withContext(Dispatchers.IO) {
            planValueRepository.delete(planValue)
            loadPlanValues(planValue.objectId)
        }
    }


    // endregion

    // region SendReport

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    fun exportDatabase(context: Context) {
        Log.d(TAG, "Начало экспорта базы данных")
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
            return
        }

        val dbName = "extra_db.db" // Изменено с "app_database" на "extra_db.db"
        val dbPath = context.getDatabasePath(dbName)
        Log.d(TAG, "Путь к базе данных: $dbPath, существует: ${dbPath.exists()}")
        if (!dbPath.exists()) {
            _errorEvent.postValue("База данных $dbName не найдена")
            Log.e(TAG, "База данных $dbName не найдена по пути: $dbPath")
            return
        }

        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (exportDir == null || !exportDir.exists()) {
            exportDir?.mkdirs()
            if (exportDir == null) {
                _errorEvent.postValue("Ошибка: не удалось создать директорию для экспорта")
                Log.e(TAG, "Не удалось создать директорию для экспорта")
                return
            }
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val outFile = File(exportDir, "$dbName${dateFormat.format(Date())}.db")
        try {
            FileInputStream(dbPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            _errorEvent.postValue("База данных экспортирована в ${outFile.absolutePath}")
            Log.d(TAG, "База данных экспортирована в ${outFile.absolutePath}")
        } catch (e: Exception) {
            _errorEvent.postValue("Ошибка экспорта базы данных: ${e.message}")
            Log.e(TAG, "Ошибка экспорта базы данных: ${e.message}", e)
        }
    }

    fun showAllEnteredDataAsList(): List<InfoItem> {
        val controlRows = _controlRows.value ?: emptyList()
        val fixVolumesRows = _fixRows.value ?: emptyList()

        Log.d(TAG, "ControlRows: $controlRows")
        Log.d(TAG, "FixVolumesRows: $fixVolumesRows")
        Log.d(TAG, "IsTransportAbsent: ${_isTransportAbsent.value}")

        val items = mutableListOf<InfoItem>().apply {
            add(InfoItem("Дата заполнения", _currentDate.value ?: ""))
            add(InfoItem("Начало заполнения", _currentTime.value ?: ""))
            add(InfoItem("Сотрудник", "${_currentUser.value?.secondName ?: ""} ${_currentUser.value?.firstName ?: ""} ${_currentUser.value?.thirdName ?: ""}"))
            add(InfoItem("Режим работы", _selectedTypeOfWork.value ?: ""))
            add(InfoItem("Заказчик", _selectedCustomer.value ?: ""))
            add(InfoItem("Договор СК", _selectedContract.value ?: ""))
            add(InfoItem("Объект", _selectedObject.value ?: ""))
            add(InfoItem("Участок", _plotText.value ?: ""))
            add(InfoItem("Генподрядчик", _selectedContractor.value ?: ""))
            add(InfoItem("Представитель Генподрядчика", _selectedRepContractor.value ?: ""))
            add(InfoItem("Представитель ССК ГП", _repSSKGpText.value ?: ""))
            add(InfoItem("Субподрядчик", _selectedSubContractor.value ?: ""))
            add(InfoItem("Представитель субподрядчика", _repSubContractorText.value ?: ""))
            add(InfoItem("Представитель ССК субподрядчика", _repSSKSubText.value ?: ""))

            // Проверяем состояние чекбокса "Транспорт отсутствует"
            if (_isTransportAbsent.value == true) {
                add(InfoItem("Транспорт", "Транспорт отсутствует"))
            } else {
                // Добавляем транспортные поля только если чекбокс не выбран
                add(InfoItem("Исполнитель по транспорту", _transportExecutorName.value ?: ""))
                add(InfoItem("Договор по транспорту", _transportContractTransport.value ?: ""))
                add(InfoItem("Госномер", _transportStateNumber.value ?: ""))
                add(InfoItem("Дата начала поездки", _transportStartDate.value ?: ""))
                add(InfoItem("Время начала поездки", _transportStartTime.value ?: ""))
                add(InfoItem("Дата окончания поездки", _transportEndDate.value ?: ""))
                add(InfoItem("Время окончания поездки", _transportEndTime.value ?: ""))
            }

            add(InfoItem("Полевой контроль", controlRows))
            add(InfoItem("Фиксация объема", fixVolumesRows))
        }

        Log.d(TAG, "Items before filtering: $items")
        return items.filter { it.value != null && (it.value !is List<*> || it.value.isNotEmpty()) }
    }

    // Метод обновления итогового отчета
    suspend fun saveOrUpdateReport(): Long {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving or updating report on thread: ${Thread.currentThread().name}")
            try {
                val employeeNumber = _currentEmployeeNumber.value ?: run {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Пользователь не авторизован")
                        Log.e(TAG, "No employeeNumber available")
                    }
                    return@withContext 0L
                }

                if (_selectedTypeOfWork.value.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Режим работы не выбран")
                        Log.e(TAG, "typeOfWork is not set")
                    }
                    return@withContext 0L
                }

                val arrangementErrors = validateArrangementInputs(
                    contract = _selectedContract.value,
                    customers = _selectedCustomer.value,
                    objects = _selectedObject.value,
                    plotText = if (_isManualPlot.value == true) "Объект не делится на участки" else _plotText.value,
                    contractors = _selectedContractor.value,
                    subContractors = _selectedSubContractor.value,
                    repSSKGpText = _repSSKGpText.value,
                    repContractor = _selectedRepContractor.value,
                    repSubContractorText = _repSubContractorText.value,
                    repSSKSubText = _repSSKSubText.value,
                    isManualPlot = _isManualPlot.value ?: false
                )
                if (arrangementErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля заполнены корректно: ${arrangementErrors.values.joinToString()}")
                        Log.d(TAG, "Arrangement validation errors: ${arrangementErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val transportErrors = validateTransportInputs(
                    isTransportAbsent = _isTransportAbsent.value ?: false,
                    executorName = _transportExecutorName.value,
                    contractTransport = _transportContractTransport.value,
                    stateNumber = _transportStateNumber.value,
                    startDate = _transportStartDate.value,
                    startTime = _transportStartTime.value,
                    endDate = _transportEndDate.value,
                    endTime = _transportEndTime.value
                )
                if (transportErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля транспорта заполнены корректно: ${transportErrors.values.joinToString()}")
                        Log.d(TAG, "Transport validation errors: ${transportErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val controlErrors = validateControlInputs(
                    isViolation = _isViolation.value ?: false,
                    orderNumber = _orderNumber.value,
                    controlRows = _controlRows.value
                )
                if (controlErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля контроля заполнены корректно: ${controlErrors.values.joinToString()}")
                        Log.d(TAG, "Control validation errors: ${controlErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val fixVolumesErrors = validateFixVolumesInputs(fixRows = _fixRows.value)
                if (fixVolumesErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля объемов заполнены корректно: ${fixVolumesErrors.values.joinToString()}")
                        Log.d(TAG, "FixVolumes validation errors: ${fixVolumesErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val reportId = _currentReportId.value ?: run {
                    Log.e(TAG, "No report ID available for update")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: нет текущего отчета для обновления")
                    }
                    return@withContext 0L
                }

                val existingReport = reportRepository.getReportById(reportId)
                if (existingReport == null) {
                    Log.e(TAG, "No report found with ID: $reportId")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: отчет не найден")
                    }
                    return@withContext 0L
                }

                val updatedReport = existingReport.copy(
                    userName = employeeNumber,
                    typeOfWork = _selectedTypeOfWork.value.orEmpty(),
                    date = _currentDate.value.orEmpty(),
                    time = _currentTime.value.orEmpty(),
                    contract = _selectedContract.value.orEmpty(),
                    customer = _selectedCustomer.value.orEmpty(),
                    obj = _selectedObject.value.orEmpty(),
                    plot = _plotText.value.orEmpty(),
                    genContractor = _selectedContractor.value.orEmpty(),
                    repGenContractor = _selectedRepContractor.value.orEmpty(),
                    repSSKGp = _repSSKGpText.value.orEmpty(),
                    subContractor = _selectedSubContractor.value.orEmpty(),
                    repSubContractor = _repSubContractorText.value.orEmpty(),
                    repSSKSub = _repSSKSubText.value.orEmpty(),
                    executor = _transportExecutorName.value.orEmpty(),
                    contractTransport = _transportContractTransport.value.orEmpty(),
                    stateNumber = _transportStateNumber.value.orEmpty(),
                    startDate = _transportStartDate.value.orEmpty(),
                    startTime = _transportStartTime.value.orEmpty(),
                    endDate = _transportEndDate.value.orEmpty(),
                    endTime = _transportEndTime.value.orEmpty(),
                    orderNumber = _orderNumber.value.orEmpty(),
                    inViolation = _isViolation.value ?: false,
                    noEquipmentName = _isEquipmentAbsent.value ?: false,
                    equipment = (_controlRows.value?.firstOrNull()?.equipmentName ?: ""),
                    complexWork = (_controlRows.value?.firstOrNull()?.typeOfWork ?: ""),
                    report = (_controlRows.value?.firstOrNull()?.report ?: ""),
                    remarks = (_controlRows.value?.firstOrNull()?.remarks ?: ""),
                    controlRows = gson.toJson(_controlRows.value),
                    fixVolumesRows = gson.toJson(_fixRows.value),
                    isEmpty = _isTransportAbsent.value ?: false,
                    isSend = false,
                    isCompleted = true // Финализируем отчет
                )
                Log.d(TAG, "Updating full report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d(TAG, "Report updated successfully with ID: $reportId")
                withContext(Dispatchers.Main) {
                    _isReportSaved.postValue(true)
                    Log.d(TAG, "Before resetting _currentReportId: ${_currentReportId.value}")
                    _currentReportId.postValue(null)
                    Log.d(TAG, "After resetting _currentReportId: ${_currentReportId.value}")
                    Log.d(TAG, "Report saved successfully, isReportSaved set to true")
                }
                reportId
            } catch (e: Exception) {
                Log.e(TAG, "Error updating report: ${e.message}, Thread: ${Thread.currentThread().name}, StackTrace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при сохранении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    // endregion

    // region ReportsFragment

    private val _userReports = MutableStateFlow<List<Report>>(emptyList())
    val userReports: StateFlow<List<Report>> = _userReports.asStateFlow()

    fun loadUserReports() {
        val employeeNumber = _currentEmployeeNumber.value ?: run {
            Log.e(TAG, "No employeeNumber available for loading user reports")
            _userReports.value = emptyList() // Пустой список, если не авторизован
            return
        }
        viewModelScope.launch {
            reportRepository.getReportsByUser(employeeNumber).collectLatest { reports ->
                _userReports.value = reports
                Log.d(TAG, "Loaded ${reports.size} reports for user: $employeeNumber")
            }
        }
    }

    // Фильтр по датам для пользователя (замените существующий filterReportsByDateRange)
    fun filterUserReportsByDateRange(startDate: String, endDate: String) {
        val employeeNumber = _currentEmployeeNumber.value ?: run {
            Log.e(TAG, "No employeeNumber available for filtering")
            _userReports.value = emptyList()
            return
        }
        viewModelScope.launch {
            reportRepository.getReportsByUserAndDateRange(employeeNumber, startDate, endDate).collectLatest { reports ->
                _userReports.value = reports
                Log.d(TAG, "Filtered ${reports.size} reports for date range $startDate - $endDate")
            }
        }
    }

    // Для экспорта (замените getReportsForExport)
    suspend fun getUserReportsForExport(startDate: String, endDate: String): List<Report> {
        val employeeNumber = _currentEmployeeNumber.value ?: run {
            Log.e(TAG, "No employeeNumber available for export")
            return emptyList()
        }
        return reportRepository.getUserReportsForExport(employeeNumber, startDate, endDate)
    }

    // endregion

    // region Auth
    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> get() = _authResult

    // Поле для хранения выбранного типа работы
    private val _selectedTypeOfWork = MutableLiveData<String?>()
    val selectedTypeOfWork: LiveData<String?> get() = _selectedTypeOfWork

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() && it.isLetter() } && // Минимум 1 заглавная буква
                password.any { it.isDigit() } && // Минимум 1 цифра
                password.all { it.isLetterOrDigit() } // Только цифры и буквы
    }

    fun validateAuthInputs(
        number: String?,
        password: String?,
        typeOfWork: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (number.isNullOrBlank()) {
            errors["number"] = "Введите уникальный номер сотрудника"
        } else if (!number.all { it.isDigit() } || number.length != 4) {
            errors["number"] = "Уникальный номер должен содержать ровно 4 цифры"
        }

        if (password.isNullOrBlank()) {
            errors["password"] = "Введите пароль"
        } else if (!isPasswordValid(password)) {
            errors["password"] = "Пароль должен содержать не менее 6 символов, " +
                    "хотя бы 1 заглавную букву, 1 цифру и только английские буквы/цифры"
        }

        if (typeOfWork.isNullOrBlank()) {
            errors["typeOfWork"] = "Выберите тип работы"
        }

        return errors
    }

    fun loginUser(employeeNumber: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting login for employeeNumber: $employeeNumber, password: $password")
                val user = userRepository.getUserByEmployeeNumber(employeeNumber)
                Log.d(TAG, "User found: $user")
                if (user != null && BCrypt.checkpw(password, user.password)) {
                    withContext(Dispatchers.Main) {
                        _currentUser.value = user
                        _currentEmployeeNumber.value = employeeNumber
                        _authResult.value = AuthResult.Success("Добро пожаловать, ${user.firstName} ${user.thirdName}!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _authResult.value = AuthResult.Error(
                            if (user == null) "Пользователь с таким номером не найден"
                            else "Неверный пароль"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _authResult.value = AuthResult.Error("Ошибка авторизации: ${e.message}")
                    Log.e(TAG, "Login error: ${e.message}", e)
                }
            }
        }
    }

    // region EmployeeNumber
    // Добавляем поле для хранения employeeNumber текущего пользователя
    private val _currentEmployeeNumber = MutableLiveData<String?>()
    val currentEmployeeNumber: LiveData<String?> get() = _currentEmployeeNumber
    // endregion

    fun setSelectedTypeOfWork(typeOfWork: String?) {
        _selectedTypeOfWork.value = typeOfWork
    }

    // Класс для результатов авторизации
    sealed class AuthResult {
        data class Success(val message: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
        data class RegistrationSuccess(val message: String) : AuthResult() // Для успешной регистрации
        data class RegistrationError(val message: String) : AuthResult() // Для ошибок регистрации
        object Idle : AuthResult() // Новое состояние для сброса
    }

    // endregion

    // region Registration
    fun validateRegistrationInputs(
        secondName: String?,
        firstName: String?,
        thirdName: String?,
        number: String?,
        branch: String?,
        pu: String?,
        password: String?,
        confirmPassword: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (secondName.isNullOrBlank()) errors["secondName"] = "Введите фамилию"
        if (firstName.isNullOrBlank()) errors["firstName"] = "Введите имя"
        if (thirdName.isNullOrBlank()) errors["thirdName"] = "Введите отчество"
        if (number.isNullOrBlank()) errors["number"] = "Введите табельный номер"
        else if (number.length != 4 || !number.all { it.isDigit() }) errors["number"] = "Табельный номер должен содержать ровно 4 цифры"

        if (branch.isNullOrBlank()) errors["branch"] = "Выберите филиал"
        if (pu.isNullOrBlank()) errors["pu"] = "Выберите ПУ"

        if (password.isNullOrBlank()) {
            errors["password"] = "Пароль не может быть пустым"
        } else if (!isPasswordValid(password)) {
            errors["password"] = "Пароль должен содержать не менее 6 символов, " +
                    "хотя бы 1 заглавную букву, 1 цифру и только английские буквы/цифры"
        }

        if (confirmPassword.isNullOrBlank()) {
            errors["confirmPassword"] = "Подтвердите пароль"
        } else if (password != confirmPassword) {
            errors["confirmPassword"] = "Пароли не совпадают"
        }

        return errors
    }

    fun registerUser(
        secondName: String,
        firstName: String,
        thirdName: String?,
        employeeNumber: String,
        branch: String,
        pu: String,
        password: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userRepository.isEmployeeNumberTaken(employeeNumber)) {
                    withContext(Dispatchers.Main) {
                        _authResult.value = AuthResult.RegistrationError("Табельный номер уже занят")
                    }
                } else {
                    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                    Log.d(TAG, "Registering user: $employeeNumber, hashedPassword: $hashedPassword")
                    val user = User(
                        id = 0,
                        secondName = secondName,
                        firstName = firstName,
                        thirdName = thirdName,
                        employeeNumber = employeeNumber,
                        branch = branch,
                        pu = pu,
                        password = hashedPassword
                    )
                    userRepository.insertUser(user)
                    withContext(Dispatchers.Main) {
                        _currentUser.value = user
                        _authResult.value = AuthResult.RegistrationSuccess("Регистрация успешна")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _authResult.value = AuthResult.RegistrationError("Ошибка регистрации: ${e.message}")
                    Log.e(TAG, "Registration error: ${e.message}", e)
                }
            }
        }
    }

    // LiveData для данных текущего пользователя
    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> get() = _currentUser

    // Очистка сессии (выход из аккаунта)
    fun logout() {
        _currentUser.value = null
        _authResult.value = AuthResult.Idle
        clearAllData()
    }

    // endregion

    init {
        //
        updateDateTime()
        loadComplexOfWorks()

        viewModelScope.launch {
            planValueRepository.getAllPlanValues().collect { values ->
                _planValues.value = values}
        }
    }

}

sealed class RowValidationResult {
    data class Valid(val remainingVolume: Double? = null): RowValidationResult()
    data class Invalid(val reason: String): RowValidationResult()
}