package com.example.epi.DataBase.ExtraDatabase.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_Customer")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)
