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
    val repGenContractor: String, // представитель генподрядчика
    val repSSKGp: String, // представитель ССК ПО (ГП)
    val subContractor: String, // субподрядчик
    val repSubContractor: String, // представитель субподрядчика
    val repSSKSub: String, // представитель ССК ПО (Суб)
    val transportCustomer: String, // договор по транспорту
    val transportExecutor: String, // исполнитель по транспорту
    val stateNumber: String, // госномер
    val startDate: String, // Дата начала поездки
    val startTime: String, // Время начала поездки
    val endDate: String, // Дата завершения поездки
    val endTime: String // Время завершения поездки
)
