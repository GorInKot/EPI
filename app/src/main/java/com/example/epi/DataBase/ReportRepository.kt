package com.example.epi.DataBase

import com.example.epi.DataBase.Entities.*
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val dao: ReferenceDao) {

    // Report operations
    suspend fun saveReport(report: ReportEntity): Long {
        return dao.insertReport(report)
    }

    suspend fun updateReport(report: ReportEntity) {
        dao.updateReport(report)
    }

    suspend fun getLastUnsentReport(): ReportEntity? {
        return dao.getLastUnsentReport()
    }

    // WorkType operations
    fun getAllWorkTypes(): Flow<List<WorkTypeEntity>> = dao.getAllWorkTypes()
    suspend fun getWorkTypeById(id: Long): WorkTypeEntity? = dao.getWorkTypeById(id)

    // Customer operations
    fun getAllCustomers(): Flow<List<CustomerEntity>> = dao.getAllCustomers()
    suspend fun getCustomerById(id: Long): CustomerEntity? = dao.getCustomerById(id)

    // Object operations
    fun getObjectsForCustomer(customerId: Long): Flow<List<ObjectEntity>> = dao.getObjectsForCustomer(customerId)
    suspend fun getObjectById(id: Long): ObjectEntity? = dao.getObjectById(id)
    fun getAllObjects(): Flow<List<ObjectEntity>> = dao.getAllObjects()

    // Plot operations
    fun getPlotsForObject(objectId: Long): Flow<List<PlotEntity>> = dao.getPlotsForObject(objectId)
    suspend fun getPlotById(id: Long): PlotEntity? = dao.getPlotById(id)

    // Contractor operations
    fun getAllContractors(): Flow<List<ContractorEntity>> = dao.getAllContractors()
    suspend fun getContractorById(id: Long): ContractorEntity? = dao.getContractorById(id)

    // SubContractor operations — добавляем сюда:
    fun getAllSubContractors(): Flow<List<SubContractorEntity>> = dao.getAllSubContractors()
    suspend fun getSubContractorById(id: Long): SubContractorEntity? = dao.getSubContractorById(id)



}
