package com.example.epi.DataBase.ExtraDatabase.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Object",
    foreignKeys = [ForeignKey(
        entity = CustomerEntity::class,
        parentColumns = ["id"],
        childColumns = ["customer_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("customer_id")]
)
data class ObjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val customer_id: Long,

    )
