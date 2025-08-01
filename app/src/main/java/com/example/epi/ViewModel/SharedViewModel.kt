package com.example.epi.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.Entities.ReportEntity
import com.example.epi.DataBase.ReportRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedViewModel(
    private val reportRepository: ReportRepository
) : ViewModel() {

    // Форматтеры даты и времени
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Текущий отчет
    private val _currentReport = MutableLiveData<ReportEntity>()
    val currentReport: LiveData<ReportEntity> = _currentReport

    // Состояние загрузки/ошибки
    private val _uiState = MutableLiveData<ReportUiState>(ReportUiState.Loading)
    val uiState: LiveData<ReportUiState> = _uiState

    init {
        loadOrCreateReport()
    }

    private fun loadOrCreateReport() {
        viewModelScope.launch {
            try {
                _uiState.value = ReportUiState.Loading

                reportRepository.getLastUnsentReport()?.let { report ->
                    _currentReport.value = report
                } ?: createNewReport()

                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createNewReport() {
        val newReport = ReportEntity(
            date = getCurrentDate(),
            time = getCurrentTime(),
            is_send = false,
            plot_id = 0L,
            work_type_id = 0L,
            customer_id = 0L,
            object_id = 0L,
            state_number = "",
            contract = "",
            contract_transport = ""
        )
        _currentReport.value = newReport
    }


    fun updateReport(update: (ReportEntity) -> ReportEntity) {
        _currentReport.value?.let { current ->
            val updated = update(current)
            _currentReport.value = updated
            saveReport(updated)
        }
    }

    private fun saveReport(report: ReportEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = ReportUiState.Saving
                if (report.id == 0L) {
                    val id = reportRepository.saveReport(report)
                    _currentReport.value = report.copy(id = id)
                } else {
                    reportRepository.updateReport(report)
                }
                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error("Failed to save report")
            }
        }
    }

    fun submitReport() {
        viewModelScope.launch {
            try {
                _uiState.value = ReportUiState.Submitting
                _currentReport.value?.let { report ->
                    reportRepository.updateReport(report.copy(is_send = true))
                    createNewReport()
                    _uiState.value = ReportUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error("Failed to submit report")
            }
        }
    }

    private fun getCurrentDate(): String = dateFormat.format(Date())
    private fun getCurrentTime(): String = timeFormat.format(Date())
}

sealed class ReportUiState {
    object Loading : ReportUiState()
    object Saving : ReportUiState()
    object Submitting : ReportUiState()
    object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

sealed class RowValidationResult {
    object Valid : RowValidationResult()
    data class Invalid(val reason: String) : RowValidationResult()
}