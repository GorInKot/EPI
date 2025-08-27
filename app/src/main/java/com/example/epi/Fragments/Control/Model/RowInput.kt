package com.example.epi.Fragments.Control.Model

data class RowInput(
    val equipmentName: String,
    val workType: String,
    val orderNumber: String,
    val report: String,
    val remarks: String,
    val isViolationChecked: Boolean,
    val isEquipmentAbsent: Boolean = false // Новое поле (аналогичное, как для ControlRow)
)

