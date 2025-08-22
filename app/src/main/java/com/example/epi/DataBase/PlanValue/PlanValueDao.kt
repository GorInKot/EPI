package com.example.epi.DataBase.PlanValue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanValueDao {
    @Query("SELECT * FROM plan_values")
    fun getAll(): Flow<List<PlanValue>>

    @Insert
    suspend fun insert(planValue: PlanValue)

    @Query("SELECT * FROM plan_values WHERE objectId = :objectId")
    suspend fun getByObjectId(objectId: String): List<PlanValue>
}