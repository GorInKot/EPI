package com.example.epi.Fragments.Reports.Reports

data class ParentItem(
    val date: String,
    val time: String,
    val obj: String,
    val children: List<ChildItem>,
    var isExpanded: Boolean = false
)

data class ChildItem(
    val customer: String, // заказчик
    val contract: String, // договор СК
    val genContractor: String, // генподрядчик
    val subContractor: String, // субподрядчик
    val transportCustomer: String, // договор по транспорту
)
