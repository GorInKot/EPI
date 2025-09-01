package com.example.epi.DataBase.PlanValue

import androidx.annotation.WorkerThread
import com.example.epi.DataBase.PlanValue.PlanValue
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

    suspend fun getPlanValuesByObjectIdAndComplexAndType(
        objectId: String,
        complexWork: String,
        typeOfWork: String
    ): List<PlanValue> {
        return planValueDao.getByObjectIdAndComplexAndType(objectId, complexWork, typeOfWork)
    }

    suspend fun update(planValue: PlanValue) = planValueDao.update(planValue)

    suspend fun delete(planValue: PlanValue) = planValueDao.delete(planValue)
}