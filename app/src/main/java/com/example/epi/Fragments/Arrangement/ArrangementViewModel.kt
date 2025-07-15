package com.example.epi.Fragments.Arrangement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.Report
import com.example.epi.DataBase.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class ArrangementViewModel(private val repository: ReportRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val arrangementIsClearing = MutableLiveData(false)

    // ---------- Дата и время ----------
    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> = _currentTime

    // ---------- Состояния чекбоксов ----------
    val isManualCustomer = MutableLiveData(false)
    val isManualObject = MutableLiveData(false)
    val isManualContractor = MutableLiveData(false)
    val isManualSubContractor = MutableLiveData(false)

    // ---------- Ручной ввод ----------
    val manualCustomer = MutableLiveData<String>()
    val manualObject = MutableLiveData<String>()
    val manualContractor = MutableLiveData<String>()
    val manualSubContractor = MutableLiveData<String>()

    // ---------- Списки выбора ----------
    val workTypes = listOf("Вахта", "Стандартный", "Суммированный")
    val customers = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3", "Заказчик 4", "Заказчик 5")
    val objects = listOf("Объект 1", "Объект 2", "Объект 3", "Объект 4", "Объект 5")
    val contractors = listOf(
        "Генподрядчик 1", "Генподрядчик 2",
        "Генподрядчик 3", "Генподрядчик 4", "Генподрядчик 5"
    )
    val subContractors = listOf(
        "Представитель Генподрядчика 1", "Представитель Генподрядчика 2",
        "Представитель Генподрядчика 3", "Представитель Генподрядчика 4",
        "Представитель Генподрядчика 5"
    )

    // ---------- Текстовые поля ----------
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

    // ---------- Выпадающие списки ----------
    val selectedWorkType = MutableLiveData<String>()
    val selectedCustomer = MutableLiveData<String>()
    val selectedObject = MutableLiveData<String>()
    val selectedContractor = MutableLiveData<String>()
    val selectedSubContractor = MutableLiveData<String>()

    // ---------- Событие ошибки ----------
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    init {
        updateDateTime()
    }

    fun updateDateTime() {
        _currentDate.value = dateFormat.format(Date())
        _currentTime.value = timeFormat.format(Date())
    }

    fun validateArrangementInputs(
        workTypes: String?,
        customers: String?,
        manualCustomer: String?,
        objects: String?,
        manualObject: String?,
        plotText: String?,
        contractors: String?,
        manualContractor: String?,
        subContractors: String?,
        manualSubContractor: String?,
        repSSKGpText: String?,
        subContractorText: String?,
        repSubcontractorText: String?,
        repSSKSubText: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (workTypes.isNullOrBlank()) {
            errors["workTypes"] = "Укажите режим работы"
        }
        if (customers.isNullOrBlank() && manualCustomer.isNullOrBlank()) {
            errors["customers"] = "Укажите заказчика"
        }
        if (objects.isNullOrBlank() && manualObject.isNullOrBlank()) {
            errors["objects"] = "Укажите объект"
        }
        if (plotText.isNullOrBlank()) {
            errors["plotText"] = "Укажите участок"
        }
        if (contractors.isNullOrBlank() && manualContractor.isNullOrBlank()) {
            errors["contractors"] = "Укажите генподрядчика"
        }
        if (subContractors.isNullOrBlank() && manualSubContractor.isNullOrBlank()) {
            errors["subContractors"] = "Укажите представителя генподрядчика"
        }
        if (repSSKGpText.isNullOrBlank()) {
            errors["repSSKGpText"] = "Укажите представителя ССК ПО (ГП)"
        }
        if (subContractorText.isNullOrBlank()) {
            errors["subContractorText"] = "Укажите субподрядчика"
        }
        if (repSubcontractorText.isNullOrBlank()) {
            errors["repSubcontractorText"] = "Укажите представителя субподрядчика"
        }
        if (repSSKSubText.isNullOrBlank()) {
            errors["repSSKSubText"] = "Укажите представителя ССК ПО (Суб)"
        }

        return errors
    }

    suspend fun saveReport(): Long {
        try {
            // Проверяем валидацию
            val errors = validateArrangementInputs(
                workTypes = selectedWorkType.value,
                customers = selectedCustomer.value,
                manualCustomer = manualCustomer.value,
                objects = selectedObject.value,
                manualObject = manualObject.value,
                plotText = plotText.value,
                contractors = selectedContractor.value,
                manualContractor = manualContractor.value,
                subContractors = selectedSubContractor.value,
                manualSubContractor = manualSubContractor.value,
                repSSKGpText = repSSKGpText.value,
                subContractorText = subContractorText.value,
                repSubcontractorText = repSubcontractorText.value,
                repSSKSubText = repSSKSubText.value
            )
            if (errors.isNotEmpty()) {
                Log.e("Tagg", "Validation failed in saveReport: $errors")
                _errorEvent.postValue("Не все поля заполнены корректно")
                return 0L
            }

            val report = Report(
                date = currentDate.value.orEmpty(),
                time = currentTime.value.orEmpty(),
                workType = selectedWorkType.value.orEmpty(),
                customer = if (isManualCustomer.value == true) manualCustomer.value.orEmpty() else selectedCustomer.value.orEmpty(),
                obj = if (isManualObject.value == true) manualObject.value.orEmpty() else selectedObject.value.orEmpty(),
                plot = plotText.value.orEmpty(),
                contractor = if (isManualContractor.value == true) manualContractor.value.orEmpty() else selectedContractor.value.orEmpty(),
                repContractor = if (isManualSubContractor.value == true) manualSubContractor.value.orEmpty() else selectedSubContractor.value.orEmpty(),
                repSSKGp = repSSKGpText.value.orEmpty(),
                subContractor = subContractorText.value.orEmpty(),
                repSubContractor = repSubcontractorText.value.orEmpty(),
                repSSKSub = repSSKSubText.value.orEmpty()
                // Остальные поля остаются пустыми (значения по умолчанию)
            )
            Log.d("Tagg", "Saving Report: $report")
            val reportId = repository.saveReport(report)
            Log.d("Tagg", "Repository returned ID: $reportId")
            if (reportId > 0) {
                Log.d("Tagg", "Report saved successfully with ID: $reportId")
                return reportId
            } else {
                Log.e("Tagg", "Failed to save report: Invalid ID returned")
                _errorEvent.postValue("Ошибка при сохранении отчета: недопустимый ID")
                return 0L
            }
        } catch (e: Exception) {
            Log.e("Tagg", "Error in saveReport: ${e.message}", e)
            _errorEvent.postValue("Ошибка при сохранении отчета: ${e.message}")
            return 0L
        }
    }

    fun loadPreviousReport() {
        viewModelScope.launch {
            try {
                val report = repository.getLastUnsentReport()
                report?.let {
                    selectedWorkType.value = it.workType
                    if (it.customer in customers) {
                        selectedCustomer.value = it.customer
                        isManualCustomer.value = false
                    } else {
                        manualCustomer.value = it.customer
                        isManualCustomer.value = true
                    }
                    if (it.obj in objects) {
                        selectedObject.value = it.obj
                        isManualObject.value = false
                    } else {
                        manualObject.value = it.obj
                        isManualObject.value = true
                    }
                    _plotText.value = it.plot
                    if (it.contractor in contractors) {
                        selectedContractor.value = it.contractor
                        isManualContractor.value = false
                    } else {
                        manualContractor.value = it.contractor
                        isManualContractor.value = true
                    }
                    if (it.repContractor in subContractors) {
                        selectedSubContractor.value = it.repContractor
                        isManualSubContractor.value = false
                    } else {
                        manualSubContractor.value = it.repContractor
                        isManualSubContractor.value = true
                    }
                    _repSSKGpText.value = it.repSSKGp
                    _subContractorText.value = it.subContractor
                    _repSubcontractorText.value = it.repSubContractor
                    _repSSKSubText.value = it.repSSKSub
                }
            } catch (e: Exception) {
                _errorEvent.postValue("Ошибка при загрузке предыдущего отчета")
            }
        }
    }

    fun onPlotChanged(newText: String) {
        _plotText.value = newText.trim()
    }

    fun onRepSSKGpChanged(newText: String) {
        _repSSKGpText.value = newText.trim()
    }

    fun onSubContractorChanged(newText: String) {
        _subContractorText.value = newText.trim()
    }

    fun onRepSubcontractorChanged(newText: String) {
        _repSubcontractorText.value = newText.trim()
    }

    fun onRepSSKSubChanged(newText: String) {
        _repSSKSubText.value = newText.trim()
    }

    fun clearAll() {
        arrangementIsClearing.value = true

        selectedWorkType.value = ""
        selectedCustomer.value = ""
        selectedObject.value = ""
        selectedContractor.value = ""
        selectedSubContractor.value = ""

        manualCustomer.value = ""
        manualObject.value = ""
        manualContractor.value = ""
        manualSubContractor.value = ""

        isManualCustomer.value = false
        isManualObject.value = false
        isManualContractor.value = false
        isManualSubContractor.value = false

        _plotText.value = ""
        _repSSKGpText.value = ""
        _subContractorText.value = ""
        _repSubcontractorText.value = ""
        _repSSKSubText.value = ""

        arrangementIsClearing.value = false
    }

    fun getAllReports(): Flow<List<Report>> {
        return repository.getAllReports()
    }

    suspend fun updateReport(report: Report) {
        repository.updateReport(report)
    }

    suspend fun clearAllReports() {
        repository.clearAllReports()
    }
}