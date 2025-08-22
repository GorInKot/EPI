package com.example.epi.DataBase.PlanValue

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_values")
data class PlanValue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val objectId: String, // Ссылка на объект из ArrangementFragment (selectedObject в sharedViewModel)
    val complexWork: String, // Комплекс работ
    val typeOfWork: String, // Вид работ
    val planValue: Double // Значение План
)
