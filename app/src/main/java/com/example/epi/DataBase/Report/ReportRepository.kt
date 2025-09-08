package com.example.epi.DataBase.Report

import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {

    // сохраняет новый отчет и возвращает его ID
    suspend fun saveReport(report: Report): Long {
        return reportDao.insert(report)
    }

    // получает последний неотправленный отчет
    suspend fun getLastUnsentReport(): Report? {
        return reportDao.getLastUnsentReport()
    }

    // возвращает все отчеты в виде Flow
    fun getAllReports(): Flow<List<Report>> {
        return reportDao.getAllReports()
    }

    // возвращает отчеты за заданный диапазон дат
    fun getReportsByDateRange(startDate: String, endDate: String):
            Flow<List<Report>> {
        return reportDao.getReportsByDateRange(startDate, endDate)
    }

    // обновляет существующий отчет
    suspend fun updateReport(report: Report) {
        reportDao.update(report)
    }

    // удаляет все отчеты из базы данных
    suspend fun clearAllReports() {
        reportDao.deleteAll()
    }

    // получаем отчет по его ID
    suspend fun getReportById(reportId: Long): Report? {
        return reportDao.getReportById(reportId)
    }

    // возвращает список завершенных отчетов
    suspend fun getCompletedReports(): List<Report> {
        return reportDao.getCompletedReports()
    }

    // Удаляет незавершенные отчеты
    suspend fun deleteIncompleteReports() {
        reportDao.deleteIncompleteReports()
    }

    // Новые методы для отчётов пользователя
    fun getReportsByUser(userName: String): Flow<List<Report>> {
        return reportDao.getReportsByUserName(userName)
    }

    fun getReportsByUserAndDateRange(userName: String, startDate: String, endDate: String): Flow<List<Report>> {
        return reportDao.getReportsByUserAndDateRange(userName, startDate, endDate)
    }

    // Для экспорта (suspend, возвращает List сразу)
    suspend fun getUserReportsForExport(userName: String, startDate: String, endDate: String): List<Report> {
        return reportDao.getReportsByUserAndDateRangeSync(userName, startDate, endDate)
    }

}