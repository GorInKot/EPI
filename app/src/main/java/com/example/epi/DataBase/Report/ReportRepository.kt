package com.example.epi.DataBase.Report

import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {

    suspend fun saveReport(report: Report): Long {
        return reportDao.insert(report)
    }

    suspend fun getLastUnsentReport(): Report? {
        return reportDao.getLastUnsentReport()
    }

    fun getAllReports(): Flow<List<Report>> {
        return reportDao.getAllReports()
    }

    fun getReportsByDateRange(startDate: String, endDate: String):
            Flow<List<Report>> {
        return reportDao.getReportsByDateRange(startDate, endDate)
    }

    suspend fun updateReport(report: Report) {
        reportDao.update(report)
    }

    suspend fun clearAllReports() {
        reportDao.deleteAll()
    }
}