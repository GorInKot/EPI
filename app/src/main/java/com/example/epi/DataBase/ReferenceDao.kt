package com.example.epi.DataBase

import androidx.room.*
import com.example.epi.DataBase.Entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {
    // region Report operations

    @Insert
    suspend fun insertReport(report: ReportEntity): Long

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Query("DELETE FROM Report")
    suspend fun deleteAllReports()

    @Query("SELECT * FROM Report WHERE is_send = 0 ORDER BY id DESC LIMIT 1")
    suspend fun getLastUnsentReport(): ReportEntity?

    @Query("SELECT * FROM Report ORDER BY date DESC, time DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM Report WHERE id = :report_id")
    fun getReportById(report_id: Long): Flow<ReportEntity?>

    @Transaction
    @Query("SELECT * FROM Report WHERE id = :report_id")
    suspend fun getFullReport(report_id: Long): FullReportData?

    @Transaction
    suspend fun saveFullReportWithRelations(
        report: ReportEntity,
        customer: CustomerEntity? = null,
        contractor: ContractorEntity? = null,
        workType: WorkTypeEntity? = null,
        obj: ObjectEntity? = null,
        plot: PlotEntity? = null,
        controlRows: List<ControlRowEntity>? = null
    ): Long {
        // Сохраняем связанные сущности и получаем их ID
        val customer_id = customer?.let { insertCustomer(it) }
        val contractor_id = contractor?.let { insertContractor(it) }
        val work_type_id = workType?.let { insertWorkType(it) }
        val object_id = obj?.let { insertObject(it) }
        val plot_id = plot?.let { insertPlot(it) }

        // Создаем обновленную версию отчета с новыми ID связей
        val reportToSave = report.copy(
            customer_id = customer_id ?: report.customer_id,
            contractor_id = contractor_id ?: report.contractor_id,
            work_type_id = work_type_id ?: report.work_type_id,
            object_id = object_id ?: report.object_id,
            plot_id = plot_id ?: report.plot_id
        )

        // Сохраняем отчет и получаем его ID
        val reportId = insertReport(reportToSave)

        return reportId
    }
    // endregion

    // region WorkType operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkType(workType: WorkTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkTypes(workTypes: List<WorkTypeEntity>)

    @Query("SELECT * FROM WorkType ORDER BY name ASC") // Удалено условие isActive
    fun getAllWorkTypes(): Flow<List<WorkTypeEntity>>

    @Query("SELECT * FROM WorkType WHERE id = :id")
    suspend fun getWorkTypeById(id: Long): WorkTypeEntity?
// endregion

    // region Customer operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM Customer WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM Customer ORDER BY name ASC") // Добавлена сортировка
    fun getAllCustomers(): Flow<List<CustomerEntity>>
// endregion

    // region Object operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObject(obj: ObjectEntity): Long

    @Update
    suspend fun updateObject(obj: ObjectEntity)

    @Query("SELECT * FROM Object WHERE id = :id")
    suspend fun getObjectById(id: Long): ObjectEntity?

    @Query("SELECT * FROM Object WHERE customer_id = :customerId")
    fun getObjectsForCustomer(customerId: Long): Flow<List<ObjectEntity>>

    @Query("SELECT * FROM Object")
    fun getAllObjects(): Flow<List<ObjectEntity>>
    // endregion

    // region Plot operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: PlotEntity): Long

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Query("SELECT * FROM Plot WHERE id = :id")
    suspend fun getPlotById(id: Long): PlotEntity?

    @Query("SELECT * FROM Plot WHERE object_id = :objectId")
    fun getPlotsForObject(objectId: Long): Flow<List<PlotEntity>>

    @Query("SELECT * FROM Plot")
    fun getAllPlots(): Flow<List<PlotEntity>>
    // endregion

    // region Contractor operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContractor(contractor: ContractorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContractors(contractors: List<ContractorEntity>)

    @Query("SELECT * FROM Contractor WHERE id = :id")
    suspend fun getContractorById(id: Long): ContractorEntity?

    @Query("SELECT * FROM Contractor ORDER BY name ASC") // Добавлена сортировка
    fun getAllContractors(): Flow<List<ContractorEntity>>
// endregion

    // region Complex operations
    @Transaction
    @Query("SELECT * FROM Customer WHERE id = :customerId")
    suspend fun getCustomerWithObjectsAndPlots(customerId: Long): CustomerWithObjectsAndPlots?

    @Transaction
    @Query("SELECT * FROM Object WHERE id = :objectId")
    suspend fun getObjectWithPlots(objectId: Long): ObjectWithPlots?

    @Transaction
    suspend fun replaceAllWorkTypes(newWorkTypes: List<WorkTypeEntity>) {
        deleteAllWorkTypes()
        insertWorkTypes(newWorkTypes)
    }

    @Query("DELETE FROM WorkType")
    suspend fun deleteAllWorkTypes()
    // endregion

    // SubContractor operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubContractor(subContractor: SubContractorEntity): Long

    @Update
    suspend fun updateSubContractor(subContractor: SubContractorEntity)

    @Query("SELECT * FROM SubContractor ORDER BY name ASC")
    fun getAllSubContractors(): Flow<List<SubContractorEntity>>

    @Query("SELECT * FROM SubContractor WHERE id = :id")
    suspend fun getSubContractorById(id: Long): SubContractorEntity?

}

// Data classes for complex queries
data class FullReportData(
    @Embedded val report: ReportEntity,
    @Relation(
        parentColumn = "customer_id",
        entityColumn = "id"
    )
    val customer: CustomerEntity?,
    @Relation(
        parentColumn = "contractor_id",
        entityColumn = "id"
    )
    val contractor: ContractorEntity?,
    @Relation(
        parentColumn = "work_type_id",
        entityColumn = "id"
    )
    val workType: WorkTypeEntity?,
    @Relation(
        parentColumn = "object_id",
        entityColumn = "id"
    )
    val obj: ObjectEntity?,
    @Relation(
        parentColumn = "plot_id",
        entityColumn = "id"
    )
    val plot: PlotEntity?
)

data class CustomerWithObjectsAndPlots(
    @Embedded val customer: CustomerEntity,
    @Relation(
        entity = ObjectEntity::class,
        parentColumn = "id",
        entityColumn = "customer_id"
    )
    val objects: List<ObjectWithPlots>
)

data class ObjectWithPlots(
    @Embedded val obj: ObjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "object_id"
    )
    val plots: List<PlotEntity>
)

