package com.example.epi.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),

    // Расстановка
    val workType: String = "",
    val customer: String = "",
    val obj: String = "",
    val plot: String = "",
    val contractor: String = "",
    val repContractor: String = "",
    val repSSKGp: String = "",
    val subContractor: String = "",
    val repSubContractor: String = "",
    val repSSKSub: String = "",

    // Транспорт
    val isEmpty: Boolean = false,
    val executor: String = "",
    val startDate: String = "",
    val startTime: String = "",
    val stateNumber: String = "",
    val contract: String = "",
    val contractTransport: String = "",
    val endDate: String = "",
    val endTime: String = "",
    val inViolation: Boolean = false,

    // Контроль
    val equipment: String = "",
    val complexWork: String = "",
    val orderNumber: String = "",
    val report: String = "",
    val remarks: String = "",
    val controlRows: String = "", // Поле для хранения JSON списка ControlRow

    // Объемы
    val fixVolumesRows: String = "", // Поле для хранения JSON списка FixVolumesRow

    val isSend: Boolean = false


)