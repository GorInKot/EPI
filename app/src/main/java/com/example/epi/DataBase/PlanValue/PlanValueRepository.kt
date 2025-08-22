package com.example.epi.DataBase.PlanValue

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class PlanValueRepository(private val planValueDao: PlanValueDao) {

    fun getAllPlanValues(): Flow<List<PlanValue>> = planValueDao.getAll()

    @WorkerThread
    suspend fun insert(planValue: PlanValue) {
        planValueDao.insert(planValue)
    }

    @WorkerThread
    suspend fun getPlanValuesByObjectId(objectId: String): List<PlanValue> {
        return planValueDao.getByObjectId(objectId)
    }
}