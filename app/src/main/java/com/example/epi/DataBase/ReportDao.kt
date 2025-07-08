package com.example.epi.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY id DESC")
    suspend fun getAllReports(): List<ReportEntity>

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("DELETE FROM reports")
    suspend fun clearAll()
}