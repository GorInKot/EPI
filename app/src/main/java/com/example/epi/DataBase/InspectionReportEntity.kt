package com.example.epi.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

/*

Здесь будет отражать все заполняемые поля для отчета:

Экраны:
    Расстановка (Arrangment):
        Заказчик - Customer
        Объект - Object
        Участок - Plot
        Генподрядчик - Contractor
        Представитель Генподрядчика - SubContractor
        Представитель ССК ПО (ГП) - RepSSK_GP
        Субподрядчик - SubContractor

 */
@Entity(tableName = "inspection_reports")
data class InspectionReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val inspectorName: String,
    val inspectedPersonName: String,
    val reportText: String,
    val timestamp: Long,
    val isSynced: Boolean = false // false по умолчанию, пока не отправлено
)

