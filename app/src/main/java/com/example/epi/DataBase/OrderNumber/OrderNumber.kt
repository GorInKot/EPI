package com.example.epi.DataBase.OrderNumber

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_numbers")
data class OrderNumber(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeNumber: String, // Уникальный номер сотрудника
    val date: String, // Дата в формате dd.MM.yyyy
    val orderCounter: Int // Счетчик для данной даты и сотрудника
)
