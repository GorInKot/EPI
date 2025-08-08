package com.example.epi.DataBase.ExtraDatabase.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Report",
    foreignKeys = [
        ForeignKey(
            entity = WorkTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["work_type_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = ObjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["object_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = PlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["plot_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = ContractorEntity::class,
            parentColumns = ["id"],
            childColumns = ["contractor_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = SubContractorEntity::class, // если есть такая сущность
            parentColumns = ["id"],
            childColumns = ["sub_contractor_id"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ],
    indices = [
        Index("work_type_id"),
        Index("customer_id"),
        Index("object_id"),
        Index("plot_id"),
        Index("contractor_id"),
        Index("sub_contractor_id"),
    ]
)
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val work_type_id: Long,
    val customer_id: Long,
    val object_id: Long,
    val plot_id: Long = 0L,
    val contractor_id: Long = 0L,
    val rep_contractor: String? = null,
    val rep_ssk_gp: String? = null,
    val sub_contractor_id: Long = 0L,
    val rep_sub_contractor: String? = null,
    val rep_ssk_sub: String? = null,
    val is_empty: Boolean = false,
    val executor: String? = null,
    val start_date: String? = null,
    val start_time: String? = null,
    val end_date: String? = null,
    val end_time: String? = null,
    val state_number: String?,
    val contract: String?,
    val contract_transport: String?,
    val in_violation: Boolean = false,
    val equipment: String? = null,
    val complex_work: String? = null,
    val order_number: String? = null,
    val report_text: String? = null,
    val remarks: String? = null,
    val is_send: Boolean = false,
    val controlRows: String? = null
)
