package com.example.epi.Fragments.Reports.Reports

data class ParentItem(
    val date: String,
    val time: String,
    val obj: String,
    val children: List<ChildItem>,
    var isExpanded: Boolean = false
)

data class ChildItem(
    val workType: String,
    val customer: String,
    val contractor: String,
    val transportCustomer: String
)
