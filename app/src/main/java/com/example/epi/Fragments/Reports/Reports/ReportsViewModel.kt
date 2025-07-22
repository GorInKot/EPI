package com.example.epi.Fragments.Reports.Reports

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.Report
import com.example.epi.DataBase.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReportsViewModel(val repository: ReportRepository):ViewModel() {

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports:StateFlow<List<Report>> = _reports

    init {
        viewModelScope.launch {
            repository.getAllReports().collectLatest { reports ->
                _reports.value = reports
            }
        }
    }

    fun filterReportsByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.getReportsByDateRange(startDate, endDate).collectLatest { reports ->
                _reports.value = reports
            }
        }
    }

    suspend fun getReportsForExport(startDate: String, endDate: String): List<Report> {
        return repository.getReportsByDateRange(startDate, endDate).first()
    }
}