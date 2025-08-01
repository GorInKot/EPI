package com.example.epi.DataBase.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WorkType")
data class WorkTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)