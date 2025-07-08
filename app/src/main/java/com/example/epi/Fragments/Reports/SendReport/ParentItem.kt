package com.example.epi.Fragments.Reports.SendReport

data class ParentItem(
    val date: String,
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
