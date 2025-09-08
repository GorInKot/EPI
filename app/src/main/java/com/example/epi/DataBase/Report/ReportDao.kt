package com.example.epi.DataBase.Report

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert
    suspend fun insert(report: Report): Long

    @Query("SELECT * FROM reports WHERE isSend = 0 ORDER BY id DESC LIMIT 1")
    suspend fun getLastUnsentReport(): Report?

    @Query("SELECT * FROM reports")
    fun getAllReports(): Flow<List<Report>>

    @Update
    suspend fun update(report: Report)

    @Query("DELETE FROM reports")
    suspend fun deleteAll()

    @Query("SELECT * FROM reports WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getReportsByDateRange(startDate: String, endDate: String): Flow<List<Report>>

    @Query("DELETE FROM reports WHERE isCompleted = 0")
    suspend fun deleteIncompleteReports()

    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportById(reportId: Long): Report?

    @Query("SELECT * FROM reports WHERE isCompleted = 1")
    suspend fun getCompletedReports(): List<Report>

    // Новые методы для фильтрации по userName
    @Query("SELECT * FROM reports WHERE userName = :userName ORDER BY date DESC, time DESC")
    fun getReportsByUserName(userName: String): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE userName = :userName AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    fun getReportsByUserAndDateRange(userName: String, startDate: String, endDate: String): Flow<List<Report>>

    // Если нужно для экспорта (не-Flow версия для suspend)
    @Query("SELECT * FROM reports WHERE userName = :userName AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    suspend fun getReportsByUserAndDateRangeSync(userName: String, startDate: String, endDate: String): List<Report>

}