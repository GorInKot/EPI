package com.example.epi.DataBase.ExtraDatabase.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Contract")
class ContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)