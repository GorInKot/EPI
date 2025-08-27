package com.example.epi.Fragments.Control.Model

data class ControlRow(
    val equipmentName: String,
    val workType: String,
    val orderNumber: String,
    val report: String,
    val remarks: String,
    val isEquipmentAbsent: Boolean = false // Новое поле для хранения состояния чекбокса для каждой строки
)

