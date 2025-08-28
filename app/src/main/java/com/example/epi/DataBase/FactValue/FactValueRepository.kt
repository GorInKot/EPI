package com.example.epi.DataBase.FactValue

class FactValueRepository(
    private val factValueDao: FactValueDao
) {
    suspend fun insert(factValue: FactValue) {
        factValueDao.insert(factValue)
    }

    suspend fun getFactValueByParams(
        objectId: String,
        complexOfWork: String,
        typeOfWork: String,
        measures: String
    ): List<FactValue> {
        return factValueDao.getFactValuesByParams(objectId, complexOfWork, typeOfWork, measures)
    }

    suspend fun getTotalFactValue(
        objectId: String,
        complexOfWork: String,
        typeOfWork: String,
        measures: String
    ): Double {
        return factValueDao.getTotalFactValueByParams(objectId, complexOfWork, typeOfWork, measures) ?: 0.0

    }
}