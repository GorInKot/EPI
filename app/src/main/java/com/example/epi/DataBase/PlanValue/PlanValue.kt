package com.example.epi.DataBase.PlanValue

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "plan_values")
data class PlanValue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val objectId: String, // Ссылка на объект из ArrangementFragment (selectedObject в sharedViewModel)
    val complexWork: String, // Комплекс работ
    val typeOfWork: String, // Вид работ
    var planValue: Double, // Значение План
    val measures: String
    ) : Parcelable
