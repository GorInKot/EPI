package com.example.epi.DataBase.ExtraDatabase.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SubContractor",
    foreignKeys = [ForeignKey(
        entity = ContractorEntity::class,  // если есть связь с подрядчиком
        parentColumns = ["id"],
        childColumns = ["contractor_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("contractor_id")]
)
data class SubContractorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contractor_id: Long
)
