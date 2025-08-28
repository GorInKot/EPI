package com.example.epi.DataBase.FactValue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FactValueDao {
    @Insert
    suspend fun insert(factValue: FactValue)

    @Query("SELECT * FROM fact_value WHERE objectId = :objectId AND complexOfWork = :complexOfWork AND typeOfWork = :typeOfWork AND measures = :measures")
    suspend fun getFactValuesByParams(
        objectId: String,
        complexOfWork: String,
        typeOfWork: String,
        measures: String
    ): List<FactValue>

    @Query("SELECT SUM(factValue) FROM fact_value WHERE objectId = :objectId AND complexOfWork = :complexOfWork AND typeOfWork = :typeOfWork AND measures = :measures")
    suspend fun getTotalFactValueByParams(
        objectId: String,
        complexOfWork: String,
        typeOfWork: String,
        measures: String
    ): Double?

}