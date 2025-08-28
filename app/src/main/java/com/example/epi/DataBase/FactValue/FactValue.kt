package com.example.epi.DataBase.FactValue

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fact_value")
data class FactValue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val objectId: String,
    val complexOfWork: String,
    val typeOfWork: String,
    val factValue: String,
    val measures: String,
    val reportId: String
)
