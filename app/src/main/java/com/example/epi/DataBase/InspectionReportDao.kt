//package com.example.epi.DataBase
//
//import androidx.room.*
//
//@Dao
//interface InspectionReportDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertReport(report: InspectionReportEntity)
//
//    @Query("SELECT * FROM inspection_reports ORDER BY timestamp DESC")
//    suspend fun getAllReports(): List<InspectionReportEntity>
//
//    @Query("SELECT * FROM inspection_reports WHERE isSynced = 0")
//    suspend fun getUnsyncedReports(): List<InspectionReportEntity>
//
//    @Update
//    suspend fun updateReport(report: InspectionReportEntity)
//
//    @Delete
//    suspend fun deleteReport(report: InspectionReportEntity)
//
//    @Query("DELETE FROM inspection_reports WHERE isSynced = 1")
//    suspend fun deleteSyncedReports()
//}
