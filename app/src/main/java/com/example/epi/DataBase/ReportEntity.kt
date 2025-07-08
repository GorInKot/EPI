package com.example.epi.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    // Расстановка
    val date: String,
    val time: String,
    val workType: String?,
    val customer: String?,
    val obj: String?,
    val plot: String?,
    val contractor: String?,
    val repContractor: String?,
    val repSSKGp: String?,
    val subContractor: String?,
    val repSubContractor: String?,
    val repSskSub: String?,

    // Транспорт
    val isTransportAbsent: Boolean,
    val transportCustomer: String?,
    val transportContract: String?,
    val transportExecutor: String?,
    val transportContractNumber: String?,
    val startDate: String?,
    val startTime: String?,
    val stateNumber: String?,
    val endDate: String?,
    val endTime: String?,
    val isViolation: Boolean,

    // Конроль
    val control: String?,

    // Объемы
    val fixVolumes: String?,

    // Отправлено
    val isSend: Boolean = false


)
