package com.example.epi

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.Report.Report
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.User
import com.example.epi.DataBase.User.UserRepository
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SharedViewModel(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val gson = Gson()

    // Флаг для отслеживания сохраненного отчета
    private val _isReportSaved = MutableLiveData<Boolean>(false)
    val isReportSaved: LiveData<Boolean> get() = _isReportSaved

    // Данные из ArrangementFragment
    private val _currentDate = MutableLiveData<String>(dateFormat.format(Date()))
    val currentDate: LiveData<String> get() = _currentDate

    private val _currentTime = MutableLiveData<String>(timeFormat.format(Date()))
    val currentTime: LiveData<String> get() = _currentTime

    private val _isManualCustomer = MutableLiveData(false)
    val isManualCustomer: LiveData<Boolean> get() = _isManualCustomer

    private val _isManualObject = MutableLiveData(false)
    val isManualObject: LiveData<Boolean> get() = _isManualObject

    private val _isManualContractor = MutableLiveData(false)
    val isManualContractor: LiveData<Boolean> get() = _isManualContractor

    private val _isManualSubContractor = MutableLiveData(false)
    val isManualSubContractor: LiveData<Boolean> get() = _isManualSubContractor

    private val _manualCustomer = MutableLiveData<String>()
    val manualCustomer: LiveData<String> get() = _manualCustomer

    private val _manualObject = MutableLiveData<String>()
    val manualObject: LiveData<String> get() = _manualObject

    private val _manualContractor = MutableLiveData<String>()
    val manualContractor: LiveData<String> get() = _manualContractor

    private val _manualSubContractor = MutableLiveData<String>()
    val manualSubContractor: LiveData<String> get() = _manualSubContractor

    private val _plotText = MutableLiveData<String>()
    val plotText: LiveData<String> get() = _plotText

    private val _repSSKGpText = MutableLiveData<String>()
    val repSSKGpText: LiveData<String> get() = _repSSKGpText

    private val _subContractorText = MutableLiveData<String>()
    val subContractorText: LiveData<String> get() = _subContractorText

    private val _repSubcontractorText = MutableLiveData<String>()
    val repSubcontractorText: LiveData<String> get() = _repSubcontractorText

    private val _repSSKSubText = MutableLiveData<String>()
    val repSSKSubText: LiveData<String> get() = _repSSKSubText

    private val _selectedWorkType = MutableLiveData<String>()
    val selectedWorkType: LiveData<String> get() = _selectedWorkType

    private val _selectedCustomer = MutableLiveData<String>()
    val selectedCustomer: LiveData<String> get() = _selectedCustomer

    private val _selectedObject = MutableLiveData<String>()
    val selectedObject: LiveData<String> get() = _selectedObject

    private val _selectedContractor = MutableLiveData<String>()
    val selectedContractor: LiveData<String> get() = _selectedContractor

    private val _selectedSubContractor = MutableLiveData<String>()
    val selectedSubContractor: LiveData<String> get() = _selectedSubContractor

    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    // Списки выбора
    companion object {
        val workTypes = listOf("Вахта", "Стандартный", "Суммированный")
        val customers = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3", "Заказчик 4", "Заказчик 5")
        val objects = listOf("Объект 1", "Объект 2", "Объект 3", "Объект 4", "Объект 5")
        val contractors = listOf("Генподрядчик 1", "Генподрядчик 2", "Генподрядчик 3", "Генподрядчик 4", "Генподрядчик 5")
        val subContractors = listOf("Представитель Генподрядчика 1", "Представитель Генподрядчика 2",
            "Представитель Генподрядчика 3", "Представитель Генподрядчика 4", "Представитель Генподрядчика 5")
    }

    // Данные из TransportFragment
    private val _isTransportAbsent = MutableLiveData(false)
    val isTransportAbsent: LiveData<Boolean> get() = _isTransportAbsent

    private val _transportContractCustomer = MutableLiveData<String>()
    val transportContractCustomer: LiveData<String> get() = _transportContractCustomer

    private val _transportExecutorName = MutableLiveData<String>()
    val transportExecutorName: LiveData<String> get() = _transportExecutorName

    private val _transportContractTransport = MutableLiveData<String>()
    val transportContractTransport: LiveData<String> get() = _transportContractTransport

    private val _transportStateNumber = MutableLiveData<String>()
    val transportStateNumber: LiveData<String> get() = _transportStateNumber

    private val _transportStartDate = MutableLiveData<String>()
    val transportStartDate: LiveData<String> get() = _transportStartDate

    private val _transportStartTime = MutableLiveData<String>()
    val transportStartTime: LiveData<String> get() = _transportStartTime

    private val _transportEndDate = MutableLiveData<String>()
    val transportEndDate: LiveData<String> get() = _transportEndDate

    private val _transportEndTime = MutableLiveData<String>()
    val transportEndTime: LiveData<String> get() = _transportEndTime

    private val _transportInClearing = MutableLiveData(false)
    val transportInClearing: LiveData<Boolean> get() = _transportInClearing

    // Данные из ControlFragment
    private var orderCounter = 1
//    private var extraOrderNumber = 1

    private val _orderNumber = MutableLiveData<String>("")
    val orderNumber: LiveData<String> get() = _orderNumber

    private val _isViolation = MutableLiveData<Boolean>(false)
    val isViolation: LiveData<Boolean> get() = _isViolation

    private val _controlRows = MutableLiveData<List<ControlRow>>(emptyList())
    val controlRows: LiveData<List<ControlRow>> get() = _controlRows

    private val _controlStartDate = MutableLiveData<String>()
    val controlStartDate: LiveData<String> get() = _controlStartDate

    private val _controlStartTime = MutableLiveData<String>()
    val controlStartTime: LiveData<String> get() = _controlStartTime

    val equipmentNames = MutableLiveData<List<String>>(
        listOf(
            "Прибор 1", "Прибор 2", "Прибор 3", "Прибор 4", "Прибор 5", "Прибор 6",
            "Прибор 7", "Прибор 8", "Прибор 9", "Прибор 10", "Прибор 11", "Прибор 12"
        )
    )

    val controlWorkTypes = MutableLiveData<List<String>>(
        listOf(
            "Комплекс работ 1", "Комплекс работ 2", "Комплекс работ 3",
            "Комплекс работ 4", "Комплекс работ 5", "Комплекс работ 6",
            "Комплекс работ 7", "Комплекс работ 7", "Комплекс работ 9",
            "Комплекс работ 10", "Комплекс работ 11", "Комплекс работ 12"
        )
    )

    // Данные из FixVolumesFragment
    private val _fixRows = MutableLiveData<List<FixVolumesRow>>(emptyList())
    val fixRows: LiveData<List<FixVolumesRow>> get() = _fixRows

    val fixWorkType = MutableLiveData<List<String>>(
        listOf(
            "Вид работ 1", "Вид работ 2", "Вид работ 3",
            "Вид работ 4", "Вид работ 5", "Вид работ 6",
            "Вид работ 7", "Вид работ 8", "Вид работ 9",
            "Вид работ 10", "Вид работ 11", "Вид работ 12"
        )
    )

    val fixMeasures = MutableLiveData<List<String>>(
        listOf(
            "м", "м2", "м3", "мм", "см", "т", "кг", "шт.", "п.м.", "л",
            "м/ч", "м/с", "градусы", "%", "МПа", "ч", "сут."
        )
    )

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    init {
        //
        updateDateTime()

        // загрузка всех отчетов
        viewModelScope.launch {
            reportRepository.getAllReports().collectLatest { reports ->
                _reports.value = reports
            }
        }
    }

    fun updateDateTime() {
        val now = Calendar.getInstance()
        _currentDate.postValue(dateFormat.format(now.time))
        _currentTime.postValue(timeFormat.format(now.time))
    }

    // Методы для ArrangementFragment
    fun setIsManualCustomer(value: Boolean) { _isManualCustomer.value = value }
    fun setIsManualObject(value: Boolean) { _isManualObject.value = value }
    fun setIsManualContractor(value: Boolean) { _isManualContractor.value = value }
    fun setIsManualSubContractor(value: Boolean) { _isManualSubContractor.value = value }

    fun setManualCustomer(value: String) { _manualCustomer.value = value }
    fun setManualObject(value: String) { _manualObject.value = value }
    fun setManualContractor(value: String) { _manualContractor.value = value }
    fun setManualSubContractor(value: String) { _manualSubContractor.value = value }

    fun setPlotText(value: String) { _plotText.value = value }
    fun setRepSSKGpText(value: String) { _repSSKGpText.value = value }
    fun setSubContractorText(value: String) { _subContractorText.value = value }
    fun setRepSubcontractorText(value: String) { _repSubcontractorText.value = value }
    fun setRepSSKSubText(value: String) { _repSSKSubText.value = value }

    fun setSelectedWorkType(value: String) { _selectedWorkType.value = value }
    fun setSelectedCustomer(value: String) { _selectedCustomer.value = value }
    fun setSelectedObject(value: String) { _selectedObject.value = value }
    fun setSelectedContractor(value: String) { _selectedContractor.value = value }
    fun setSelectedSubContractor(value: String) { _selectedSubContractor.value = value }

    fun validateArrangementInputs(
        workType: String?,
        customer: String?,
        manualCustomer: String?,
        objectId: String?,
        manualObject: String?,
        plotText: String?,
        contractor: String?,
        manualContractor: String?,
        subContractor: String?,
        manualSubContractor: String?,
        repSSKGpText: String?,
        subContractorText: String?,
        repSubcontractorText: String?,
        repSSKSubText: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (workType.isNullOrBlank()) errors["workType"] = "Укажите режим работы"
        if (customer.isNullOrBlank() && manualCustomer.isNullOrBlank()) errors["customer"] = "Укажите заказчика"
        if (objectId.isNullOrBlank() && manualObject.isNullOrBlank()) errors["object"] = "Укажите объект"
        if (plotText.isNullOrBlank()) errors["plotText"] = "Укажите участок"
        if (contractor.isNullOrBlank() && manualContractor.isNullOrBlank()) errors["contractor"] = "Укажите генподрядчика"
        if (subContractor.isNullOrBlank() && manualSubContractor.isNullOrBlank()) errors["subContractor"] = "Укажите представителя генподрядчика"
        if (repSSKGpText.isNullOrBlank()) errors["repSSKGpText"] = "Укажите представителя ССК ПО (ГП)"
        if (subContractorText.isNullOrBlank()) errors["subContractorText"] = "Укажите субподрядчика"
        if (repSubcontractorText.isNullOrBlank()) errors["repSubcontractorText"] = "Укажите представителя субподрядчика"
        if (repSSKSubText.isNullOrBlank()) errors["repSSKSubText"] = "Укажите представителя ССК ПО (Суб)"
        return errors
    }

    // Новое
    suspend fun saveArrangementData(): Long {
        return withContext(Dispatchers.IO) {
            Log.d("Tagg", "Saving report on thread: ${Thread.currentThread().name}")
            try {
                val arrangementErrors = validateArrangementInputs(
                    workType = _selectedWorkType.value,
                    customer = _selectedCustomer.value,
                    manualCustomer = _manualCustomer.value,
                    objectId = _selectedObject.value,
                    manualObject = _manualObject.value,
                    plotText = _plotText.value,
                    contractor = _selectedContractor.value,
                    manualContractor = _manualContractor.value,
                    subContractor = _selectedSubContractor.value,
                    manualSubContractor = _manualSubContractor.value,
                    repSSKGpText = _repSSKGpText.value,
                    subContractorText = _subContractorText.value,
                    repSubcontractorText = _repSubcontractorText.value,
                    repSSKSubText = _repSSKSubText.value
                )
                if (arrangementErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля заполнены корректно: ${arrangementErrors.values.joinToString()} ")
                        Log.d("Taag", "Arrangement validation errors: ${arrangementErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val report = Report(
                    date = _currentDate.value.orEmpty(),
                    time = _currentTime.value.orEmpty(),
                    workType = _selectedWorkType.value.orEmpty(),
                    customer = if (_isManualCustomer.value == true) _manualCustomer.value.orEmpty() else _selectedCustomer.value.orEmpty(),
                    obj = if (_isManualObject.value == true) _manualObject.value.orEmpty() else _selectedObject.value.orEmpty(),
                    plot = _plotText.value.orEmpty(),
                    contractor = if (_isManualContractor.value == true) _manualContractor.value.orEmpty() else _selectedContractor.value.orEmpty(),
                    repContractor = if (_isManualSubContractor.value == true) _manualSubContractor.value.orEmpty() else _selectedSubContractor.value.orEmpty(),
                    repSSKGp = _repSSKGpText.value.orEmpty(),
                    subContractor = _subContractorText.value.orEmpty(),
                    repSubContractor = _repSubcontractorText.value.orEmpty(),
                    repSSKSub = _repSSKSubText.value.orEmpty(),
                    // Поля Transport, Control и FixVolumes остаются пустыми
                    contract = "",
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
                    isEmpty = false
                )
                Log.d("Tagg", "Saving full report: $report")
                val reportId = reportRepository.saveReport(report)
                Log.d("Tagg", "Saved report ID: $reportId")
                if (reportId > 0) {
                    withContext(Dispatchers.Main) {
                        _isReportSaved.postValue(true)
                        Log.d("Tagg", "Report saved successfully, isReportSaved set to true")
                    }
                }
                reportId
            } catch (e: Exception) {
                Log.e("Tagg", "Error saving report: ${e.message}, Thread: ${Thread.currentThread().name}, StackTrace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при сохранении отчета: ${e.message}")
                }
                0L
            }
        }
    }


    // Старое
    suspend fun saveOrUpdateReport(): Long {
        return withContext(Dispatchers.IO) {
            Log.d("Tagg", "Saving report on thread: ${Thread.currentThread().name}")
            try {
                val arrangementErrors = validateArrangementInputs(
                    workType = _selectedWorkType.value,
                    customer = _selectedCustomer.value,
                    manualCustomer = _manualCustomer.value,
                    objectId = _selectedObject.value,
                    manualObject = _manualObject.value,
                    plotText = _plotText.value,
                    contractor = _selectedContractor.value,
                    manualContractor = _manualContractor.value,
                    subContractor = _selectedSubContractor.value,
                    manualSubContractor = _manualSubContractor.value,
                    repSSKGpText = _repSSKGpText.value,
                    subContractorText = _subContractorText.value,
                    repSubcontractorText = _repSubcontractorText.value,
                    repSSKSubText = _repSSKSubText.value
                )
                if (arrangementErrors.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Не все поля заполнены корректно: ${arrangementErrors.values.joinToString()} ")
                        Log.d("Taag", "Arrangement validation errors: ${arrangementErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val transportErrors = validateTransportInputs(
                    isTransportAbsent = _isTransportAbsent.value ?: false,
                    contractCustomer = _transportContractCustomer.value,
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
                        Log.d("Tagg", "Transport validation errors: ${transportErrors.values.joinToString()}")
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
                        Log.d("Tagg", "Control validation errors: ${controlErrors.values.joinToString()}")
                    }
                    return@withContext 0L
                }

                val report = Report(
                    date = _currentDate.value.orEmpty(),
                    time = _currentTime.value.orEmpty(),
                    workType = _selectedWorkType.value.orEmpty(),
                    customer = if (_isManualCustomer.value == true) _manualCustomer.value.orEmpty() else _selectedCustomer.value.orEmpty(),
                    obj = if (_isManualObject.value == true) _manualObject.value.orEmpty() else _selectedObject.value.orEmpty(),
                    plot = _plotText.value.orEmpty(),
                    contractor = if (_isManualContractor.value == true) _manualContractor.value.orEmpty() else _selectedContractor.value.orEmpty(),
                    repContractor = if (_isManualSubContractor.value == true) _manualSubContractor.value.orEmpty() else _selectedSubContractor.value.orEmpty(),
                    repSSKGp = _repSSKGpText.value.orEmpty(),
                    subContractor = _subContractorText.value.orEmpty(),
                    repSubContractor = _repSubcontractorText.value.orEmpty(),
                    repSSKSub = _repSSKSubText.value.orEmpty(),
                    contract = _transportContractCustomer.value.orEmpty(),
                    executor = _transportExecutorName.value.orEmpty(),
                    contractTransport = _transportContractTransport.value.orEmpty(),
                    stateNumber = _transportStateNumber.value.orEmpty(),
                    startDate = _transportStartDate.value.orEmpty(),
                    startTime = _transportStartTime.value.orEmpty(),
                    endDate = _transportEndDate.value.orEmpty(),
                    endTime = _transportEndTime.value.orEmpty(),
                    orderNumber = _orderNumber.value.orEmpty(),
                    inViolation = _isViolation.value ?: false,
                    equipment = (_controlRows.value?.firstOrNull()?.equipmentName ?: ""),
                    complexWork = (_controlRows.value?.firstOrNull()?.workType ?: ""),
                    report = (_controlRows.value?.firstOrNull()?.report ?: ""),
                    remarks = (_controlRows.value?.firstOrNull()?.remarks ?: ""),
                    controlRows = gson.toJson(_controlRows.value),
                    fixVolumesRows = gson.toJson(_fixRows.value),
                    isEmpty = _isTransportAbsent.value ?: false
                )
                Log.d("Tagg", "Saving full report: $report")
                val reportId = reportRepository.saveReport(report)
                Log.d("Tagg", "Saved report ID: $reportId")
                if (reportId > 0) {
                    withContext(Dispatchers.Main) {
                        _isReportSaved.postValue(true)
                        Log.d("Tagg", "Report saved successfully, isReportSaved set to true")
                    }
                }
                reportId
            } catch (e: Exception) {
                Log.e("Tagg", "Error saving report: ${e.message}, Thread: ${Thread.currentThread().name}, StackTrace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при сохранении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    // Методы для TransportFragment
    fun setTransportAbsent(value: Boolean) { _isTransportAbsent.value = value }
    fun setTransportContractCustomer(value: String) { _transportContractCustomer.value = value.trim() }
    fun setTransportExecutorName(value: String) { _transportExecutorName.value = value.trim() }
    fun setTransportContractTransport(value: String) { _transportContractTransport.value = value.trim() }
    fun setTransportStateNumber(value: String) { _transportStateNumber.value = value.trim() }
    fun setTransportStartDate(value: String) { _transportStartDate.value = value.trim() }
    fun setTransportStartTime(value: String) { _transportStartTime.value = value.trim() }
    fun setTransportEndDate(value: String) { _transportEndDate.value = value.trim() }
    fun setTransportEndTime(value: String) { _transportEndTime.value = value.trim() }

    fun clearTransport() {
        _transportInClearing.value = true
        _transportContractCustomer.value = ""
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
        isTransportAbsent: Boolean,
        contractCustomer: String?,
        executorName: String?,
        contractTransport: String?,
        stateNumber: String?,
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (isTransportAbsent) return errors
        if (contractCustomer.isNullOrBlank()) {
            errors["contractCustomer"] = "Укажите договор СК"
        }
        if (executorName.isNullOrBlank()) {
            errors["executorName"] = "Укажите исполнителя по транспорту"
        }
        if (contractTransport.isNullOrBlank()) {
            errors["contractTransport"] = "Укажите договор по транспорту"
        }
        if (stateNumber.isNullOrBlank()) {
            errors["stateNumber"] = "Укажите госномер"
        }
        if (startDate.isNullOrBlank()) {
            errors["startDate"] = "Укажите дату начала поездки"
        }
        if (startTime.isNullOrBlank()) {
            errors["startTime"] = "Укажите время начала поездки"
        }
        if (endDate.isNullOrBlank()) {
            errors["endDate"] = "Укажите дату завершения поездки"
        }
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

    fun isValidStateNumber(number: String): Boolean {
        return number.matches(Regex("^[АВЕКМНОРСТУХ]\\s\\d{3}\\s[АВЕКМНОРСТУХ]{2}\\s\\d{2,3}$"))
    }

    suspend fun updateTransportReport(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val errors = validateTransportInputs(
                    isTransportAbsent = _isTransportAbsent.value ?: false,
                    contractCustomer = _transportContractCustomer.value,
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
                        Log.e("Tagg", "Transport: Validation failed in updateTransportReport: $errors")
                    }
                    return@withContext 0L
                }
                val existingReport = reportRepository.getLastUnsentReport()
                if (existingReport == null) {
                    Log.e("Tagg", "Transport: No unsent report found to update")
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Ошибка: нет незавершенного отчета для обновления")
                    }
                    return@withContext 0L
                }
                val updatedReport = existingReport.copy(
                    contract = if (_isTransportAbsent.value == true) "" else _transportContractCustomer.value.orEmpty(),
                    executor = if (_isTransportAbsent.value == true) "" else _transportExecutorName.value.orEmpty(),
                    contractTransport = if (_isTransportAbsent.value == true) "" else _transportContractTransport.value.orEmpty(),
                    stateNumber = if (_isTransportAbsent.value == true) "" else _transportStateNumber.value.orEmpty(),
                    startDate = if (_isTransportAbsent.value == true) "" else _transportStartDate.value.orEmpty(),
                    startTime = if (_isTransportAbsent.value == true) "" else _transportStartTime.value.orEmpty(),
                    endDate = if (_isTransportAbsent.value == true) "" else _transportEndDate.value.orEmpty(),
                    endTime = if (_isTransportAbsent.value == true) "" else _transportEndTime.value.orEmpty(),
                    isEmpty = _isTransportAbsent.value ?: false
                )
                Log.d("Tagg", "Transport: Updating Report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d("Tagg", "Transport: Report updated successfully with ID: ${updatedReport.id}")
                updatedReport.id
            } catch (e: Exception) {
                Log.e("Tagg", "Transport: Error in updateTransportReport: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
                }
                0L
            }
        }
    }

    // Методы для ControlFragment
    fun generateOrderNumber() {
        try {
            val dateStr = _controlStartDate.value.takeIf { !it.isNullOrBlank() }
                ?: dateFormat.format(Date())
            val formatterInputDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date = LocalDate.parse(dateStr, formatterInputDate)
            val formattedDate = date.format(DateTimeFormatter.ofPattern("MMdd"))

            val personNumber = "0000"
            val generatedNumber = "$personNumber.$formattedDate.$orderCounter"
            orderCounter++

            _orderNumber.value = if (_isViolation.value == true) "Нет нарушения" else generatedNumber
        } catch (e: Exception) {
            _orderNumber.value = "Ошибка генерации номера"
            e.printStackTrace()
        }
    }

//    fun setControlStartDate(date: String) { _controlStartDate.value = date.trim() }
//    fun setControlStartTime(time: String) { _controlStartTime.value = time.trim() }
    fun setViolation(checked: Boolean) {
        _isViolation.value = checked
        _orderNumber.value = if (checked) "Нет нарушения" else ""
    }

    fun addRow(row: ControlRow) {
        val currentList = _controlRows.value?.toMutableList() ?: mutableListOf()
        currentList.add(row)
        _controlRows.value = currentList
    }

    fun removeRow(row: ControlRow) {
        _controlRows.value = _controlRows.value?.filterNot { it == row }
    }

    fun updateRow(oldRow: ControlRow, newRow: ControlRow) {
        val currentList = _controlRows.value?.toMutableList() ?: return
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _controlRows.value = currentList
        } else {
            Log.w("Tagg", "ControlFragment: Row not found: $oldRow")
        }
    }

    fun validateRowInput(input: RowInput): RowValidationResult {
        return when {
            input.equipmentName.isBlank() -> RowValidationResult.Invalid("Оборудование не указано")
            input.workType.isBlank() -> RowValidationResult.Invalid("Вид работ не указан")
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
                    workType = row.workType,
                    report = row.report,
                    remarks = row.remarks,
                    orderNumber = row.orderNumber,
                    isViolationChecked = isViolation
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
                    Log.e("Tagg", "Control: Validation failed: $errors")
                    _errorEvent.postValue("Не все поля заполнены корректно")
                    return@withContext 0L
                }

                val existingReport = reportRepository.getLastUnsentReport()
                if (existingReport == null) {
                    Log.e("Tagg", "Control: No unsent report found")
                    _errorEvent.postValue("Ошибка: нет незавершенного отчета")
                    return@withContext 0L
                }

                val controlRowsJson = gson.toJson(_controlRows.value)
                val firstRow = _controlRows.value?.firstOrNull()

                val updatedReport = existingReport.copy(
                    orderNumber = if (_isViolation.value == true) "Нет нарушения" else _orderNumber.value.orEmpty(),
                    inViolation = _isViolation.value ?: false,
                    startDate = _controlStartDate.value.orEmpty(),
                    startTime = _controlStartTime.value.orEmpty(),
                    equipment = firstRow?.equipmentName ?: "",
                    complexWork = firstRow?.workType ?: "",
                    report = firstRow?.report ?: "",
                    remarks = firstRow?.remarks ?: "",
                    controlRows = controlRowsJson
                )
                Log.d("Tagg", "Control: Updating Report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d("Tagg", "Control: Report updated successfully with ID: ${updatedReport.id}")
                updatedReport.id
            } catch (e: Exception) {
                Log.e("Tagg", "Control: Error in updateControlReport: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
                }
                0L
            }
        }
    }

//    fun clearControl() {
//        _orderNumber.value = ""
//        _isViolation.value = false
//        _controlStartDate.value = ""
//        _controlStartTime.value = ""
//        _controlRows.value = emptyList()
//    }

    // Методы для FixVolumesFragment
    fun addFixRow(fixRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: mutableListOf()
        current.add(fixRow)
        _fixRows.value = recalculateFixRows(current)
    }

    fun removeFixRow(fixRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: return
        current.remove(fixRow)
        _fixRows.value = recalculateFixRows(current)
    }

    fun updateFixRow(oldRow: FixVolumesRow, newRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it == oldRow }
        if (index != -1) {
            current[index] = newRow
            _fixRows.value = current
        } else {
            Log.w("Tagg", "FixVolumesFragment:Row not found: $oldRow")
        }
    }

    private fun recalculateFixRows(rows: List<FixVolumesRow>): List<FixVolumesRow> {
        Log.d("Tagg", "Recalculating rows: $rows")
        val result = rows.groupBy { Triple(it.projectWorkType, it.measure, it.plan) }
            .flatMap { (key, group) ->
                val totalFact = group.sumOf { it.fact.toDoubleOrNull() ?: 0.0 }
                val planValue = group.first().plan.toDoubleOrNull() ?: 0.0
                val remainingVolume = planValue - totalFact
                Log.d("Tagg", "Group: $key, totalFact: $totalFact, planValue: $planValue, remainingVolume: $remainingVolume")
                group.map { row ->
                    row.copy(result = if (remainingVolume >= 0) remainingVolume.toString() else "0.0")
                }
            }
        Log.d("Tagg", "Recalculated rows: $result")
        return result
    }

    fun validateAndCalculateRemainingVolume(input: FixVolumesRow, excludeRow: FixVolumesRow? = null): RowValidationResult {
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

        val planValue = input.plan.toDouble()
        val factValue = input.fact.toDouble()

        val matchingRows = _fixRows.value?.filter {
            it != excludeRow &&
                    it.projectWorkType == input.projectWorkType &&
                    it.measure == input.measure &&
                    it.plan == input.plan
        } ?: emptyList()

        val totalFact = matchingRows.sumOf { it.fact.toDoubleOrNull() ?: 0.0 } + factValue
        val remainingVolume = planValue - totalFact

        if (remainingVolume < 0) {
            return RowValidationResult.Invalid("Сумма фактических значений превышает плановое")
        }

        return RowValidationResult.Valid(remainingVolume)
    }

    fun validateFixVolumesInputs(fixRows: List<FixVolumesRow>?): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (fixRows.isNullOrEmpty()) {
            errors["fixRows"] = "Добавьте хотя бы одну строку фиксации объемов"
        } else {
            fixRows.forEachIndexed { index, row ->
                when (val result = validateAndCalculateRemainingVolume(row)) {
                    is RowValidationResult.Invalid -> {
                        errors["row_$index"] = result.reason
                    }
                    else -> {}
                }
            }
        }
        return errors
    }

    suspend fun updateFixVolumesReport(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val errors = validateFixVolumesInputs(fixRows = _fixRows.value)
                if (errors.isNotEmpty()) {
                    Log.e("Tagg", "FixVolumes: Validation failed: $errors")
                    _errorEvent.postValue("Не все поля заполнены корректно")
                    return@withContext 0L
                }
                val existingReport = reportRepository.getLastUnsentReport()
                if (existingReport == null) {
                    Log.e("Tagg", "FixVolumes: No unsent report found")
                    _errorEvent.postValue("Ошибка: нет незавершенного отчета")
                    return@withContext 0L
                }
                val fixRowsJson = gson.toJson(_fixRows.value)
                val updatedReport = existingReport.copy(
                    fixVolumesRows = fixRowsJson
                )
                Log.d("Tagg", "FixVolumes: Updating Report: $updatedReport")
                reportRepository.updateReport(updatedReport)
                Log.d(
                    "Tagg",
                    "FixVolumes: Report updated successfully with ID: ${updatedReport.id}"
                )
                updatedReport.id
            } catch (e: Exception) {
                Log.e("Tagg", "FixVolumes: Error in updateFixVolumesReport: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
                }
                0L
            }
        }
    }

//    fun clearFixVolumes() { _fixRows.value = emptyList() }

    // Методы для SendReportFragment
    fun exportDatabase(context: Context) {
        val dbName = "app_database"
        val dbPath = context.getDatabasePath(dbName)
        val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "")
        if (!exportDir.exists()) exportDir.mkdirs()
        val outFile = File(exportDir, "$dbName${dateFormat.format(Date())}.db")
        try {
            FileInputStream(dbPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            _errorEvent.postValue("Ошибка экспорта базы данных: ${e.message}")
        }
    }

    fun showAllEnteredData(): String {
        return """
        Дата: ${_currentDate.value}
        Время: ${_currentTime.value}
        Тип работ: ${_selectedWorkType.value}
        Заказчик: ${if (_isManualCustomer.value == true) _manualCustomer.value else _selectedCustomer.value}
        Объект: ${if (_isManualObject.value == true) _manualObject.value else _selectedObject.value}
        Участок: ${_plotText.value}
        Подрядчик: ${if (_isManualContractor.value == true) _manualContractor.value else _selectedContractor.value}
        Субподрядчик: ${if (_isManualSubContractor.value == true) _manualSubContractor.value else _selectedSubContractor.value}
        Представитель ССК ГП: ${_repSSKGpText.value}
        Текст субподрядчика: ${_subContractorText.value}
        Представитель субподрядчика: ${_repSubcontractorText.value}
        Представитель ССК субподрядчика: ${_repSSKSubText.value}
        Договор транспорта: ${_transportContractCustomer.value}
        Исполнитель по транспорту: ${_transportExecutorName.value}
        Транспорт по договору: ${_transportContractTransport.value}
        
        Госномер транспорта: ${_transportStateNumber.value}
        Дата начала поездки: ${_transportStartDate.value}
        Время начала поездки: ${_transportStartTime.value}
        Дата окончания поездки: ${_transportEndDate.value}
        Время окончания поездки: ${_transportEndTime.value}
        Номер заказа: ${_orderNumber.value}
        Нарушение: ${_isViolation.value}
        
        Контрольные строки: ${gson.toJson(_controlRows.value)}
        
        Строки фиксации: ${gson.toJson(_fixRows.value)}
    """.trimIndent()
    }

    fun filterReportsByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            reportRepository.getReportsByDateRange(startDate, endDate).collectLatest { reports ->
                _reports.value = reports
            }
        }
    }

    suspend fun getReportsForExport(startDate: String, endDate: String): List<Report> {
        return reportRepository.getReportsByDateRange(startDate, endDate).first()
    }

    fun loadPreviousReport() {
        viewModelScope.launch {
            try {
                val report = reportRepository.getLastUnsentReport()
                report?.let {
                    _selectedWorkType.value = it.workType
                    if (it.customer in customers) {
                        _selectedCustomer.value = it.customer
                        _isManualCustomer.value = false
                    } else {
                        _manualCustomer.value = it.customer
                        _isManualCustomer.value = true
                    }
                    if (it.obj in objects) {
                        _selectedObject.value = it.obj
                        _isManualObject.value = false
                    } else {
                        _manualObject.value = it.obj
                        _isManualObject.value = true
                    }
                    _plotText.value = it.plot
                    if (it.contractor in contractors) {
                        _selectedContractor.value = it.contractor
                        _isManualContractor.value = false
                    } else {
                        _manualContractor.value = it.contractor
                        _isManualContractor.value = true
                    }
                    if (it.repContractor in subContractors) {
                        _selectedSubContractor.value = it.repContractor
                        _isManualSubContractor.value = false
                    } else {
                        _manualSubContractor.value = it.repContractor
                        _isManualSubContractor.value = true
                    }
                    _repSSKGpText.value = it.repSSKGp
                    _subContractorText.value = it.subContractor
                    _repSubcontractorText.value = it.repSubContractor
                    _repSSKSubText.value = it.repSSKSub
                    _transportContractCustomer.value = it.contract
                    _transportExecutorName.value = it.executor
                    _transportContractTransport.value = it.contractTransport
                    _transportStateNumber.value = it.stateNumber

                }
            } catch (e: Exception) {
                _errorEvent.postValue("Ошибка при загрузке предыдущего отчета")
            }
        }
    }

    fun clearAllData() {
        _selectedWorkType.value = ""
        _selectedCustomer.value = ""
        _selectedObject.value = ""
        _selectedContractor.value = ""
        _selectedSubContractor.value = ""
        _manualCustomer.value = ""
        _manualObject.value = ""
        _manualContractor.value = ""
        _manualSubContractor.value = ""
        _isManualCustomer.value = false
        _isManualObject.value = false
        _isManualContractor.value = false
        _isManualSubContractor.value = false
        _plotText.value = ""
        _repSSKGpText.value = ""
        _subContractorText.value = ""
        _repSubcontractorText.value = ""
        _repSSKSubText.value = ""
        _transportContractCustomer.value = ""
        _transportExecutorName.value = ""
        _transportContractTransport.value = ""
        _transportStateNumber.value = ""
        _transportStartDate.value = ""
        _transportStartTime.value = ""
        _transportEndDate.value = ""
        _transportEndTime.value = ""
        _orderNumber.value = ""
        _isViolation.value = false
        _controlStartDate.value = ""
        _controlStartTime.value = ""
        _controlRows.value = emptyList()
        _fixRows.value = emptyList()
        _isTransportAbsent.value = false
        updateDateTime()
        _isReportSaved.postValue(false) // Используем postValue для безопасности
    }

    // LiveData для данных текущего пользователя
    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> get() = _currentUser

    // Загрузка данных пользователя
    fun loadCurrentUser(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Получаем employeeNumber из SharedPreferences
                val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val employeeNumber = sharedPreferences.getString("current_user", null)
                if (employeeNumber != null) {
                    val user = userRepository.getUserByEmployeeNumber(employeeNumber)
                    withContext(Dispatchers.Main) {
                        _currentUser.value = user
                        Log.d("SharedViewModel", "Loaded user: $user")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _errorEvent.postValue("Пользователь не авторизован")
                        Log.e("SharedViewModel", "No employeeNumber found in SharedPreferences")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorEvent.postValue("Ошибка загрузки данных пользователя: ${e.message}")
                    Log.e("SharedViewModel", "Error loading user: ${e.message}", e)
                }
            }
        }
    }
}

sealed class RowValidationResult {
    data class Valid(val remainingVolume: Double? = null): RowValidationResult()
    data class Invalid(val reason: String): RowValidationResult()
}