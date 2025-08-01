package com.example.epi.DataBase.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Plot",
    foreignKeys = [ForeignKey(
        entity = ObjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["object_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("object_id")]
)
data class PlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val object_id: Long,
)