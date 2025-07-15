package com.example.epi.DataBase

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
}