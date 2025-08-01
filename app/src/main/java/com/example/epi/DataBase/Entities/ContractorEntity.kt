package com.example.epi.DataBase.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Contractor",
    foreignKeys = [ForeignKey(
        entity = CustomerEntity::class,
        parentColumns = ["id"],
        childColumns = ["customer_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("customer_id")]
)
data class ContractorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val customer_id: Long,
)