package com.example.epi.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.ReportDao
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class SharedViewModel(private val reportDao: ReportDao) : ViewModel() {
//
//    // ---------- Общее ----------
//    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//    private fun now(): Date = Date()
//
//    // ---------- Инициализация ----------
//
//    // -------- Работа с БД --------
//    private val _currentReport = MutableLiveData<ReportEntity?>()
//    val currentReport: LiveData<ReportEntity?> = _currentReport
//
//
//    private var currentReportId: Long? = null
//
//    init {
//        viewModelScope.launch {
//            // При инициализации пробуем загрузить незавершенный отчет
//            val report = reportDao.getLastUnsentReport()
//            if (report != null) {
//                currentReportId = report.id
//                _currentReport.postValue(report)
//            } else {
//                createNewReport()
//            }
//        }
//    }
//
//    // ---------- Текстовые обновления ----------
//
//    private fun updateCurrentReport(update: (ReportEntity) -> ReportEntity) {
//        currentReport.value?.let { current ->
//            val updated = update(current)
//            _currentReport.value = updated
//            saveReportToDb(updated)
//        }
//    }
//
//    // selectedWorkType
//
//
//
//
//
//
//    fun createNewReport() {
//        val newReport = ReportEntity(
//            id = 0L,
//            date = "",
//            time = "",
//            workType = null,
//            customer = null,
//            obj = null,
//            plot = null,
//            contractor = null,
//            repContractor = null,
//            repSSKGp = null,
//            subContractor = null,
//            repSubContractor = null,
//            repSskSub = null,
//
//            isTransportAbsent = false,
//            transportCustomer = null,
//            transportContract = null,
//            transportExecutor = null,
//            transportContractNumber = null,
//            startDate = null,
//            startTime = null,
//            stateNumber = null,
//            endDate = null,
//            endTime = null,
//            isViolation = false,
//
//            control = null,
//            fixVolumes = null,
//            isSend = false
//        )
//
//        currentReportId = null
//        _currentReport.postValue(newReport)
//    }
//
//
//    fun updateReport(updatedReport: ReportEntity) {
//        _currentReport.value = updatedReport
//        saveReportToDb(updatedReport)
//    }
//
//    fun saveReportToDb(report: ReportEntity) {
//        viewModelScope.launch {
//            if (currentReportId == null) {
//                val id = reportDao.insertReport(report) // insert и получить id
//                currentReportId = id
//            } else {
//                reportDao.updateReport(report)
//            }
//        }
//    }
//
//    suspend fun finishReport() {
//        _currentReport.value?.let { report ->
//            val finalReport = report.copy(isSend = true)  // пометить как отправленный
//            reportDao.updateReport(finalReport)
//            createNewReport()  // подготовить новый пустой отчёт
//        }
//    }
}

sealed class RowValidationResult {
    object Valid: RowValidationResult()
    data class Invalid(val reason: String): RowValidationResult()
}
