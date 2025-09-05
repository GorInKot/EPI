package com.example.epi.DataBase.Report

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),

    val userName: String = " ", // Имя сотрудника
    val typeOfWork: String = "", // Режим работы

    // -------- Расстановка --------
    val customer: String = "", // Заказчик
    val contract: String = "", // Договор СК
    val obj: String = "", // Объект
    val isManualPlot: Boolean = false, // Флаг для чекбокса "Объект не делится на участок"
    val plot: String = "", // Участок
    val genContractor: String = "", // Генподрядчик
    val repGenContractor: String = "", // Представитель генподрядчика
    val repSSKGp: String = "", // Представитель ССК ПО (ГП)
    val subContractor: String = "", // Субподрядчик
    val repSubContractor: String = "", // Представитель Субподрядчика
    val repSSKSub: String = "", // Представитель ССК ПО (Суб)

    // -------- Транспорт --------
    val isEmpty: Boolean = false, // Транспорт отсутствует
    val executor: String = "", // Исполнитель по транспорту
    val contractTransport: String = "", // Договор по транспорту
    val stateNumber: String = "", // Госномер

    val startDate: String = "", // Дата начала поездки
    val startTime: String = "", // Время начала поездки
    val endDate: String = "", // Дата завершения поездки
    val endTime: String = "", // Время начала поездки

    // -------- Контроль --------
    val inViolation: Boolean = false, // Нарушение
    val noEquipmentName: Boolean = false, // Прибор/Оборудование отсутствует
    val equipment: String = "", // Название прибора / оборудования
    val complexWork: String = "",  // Комплекс работ
    val orderNumber: String = "", // Номер предписания
    val report: String = "", // Отчет о результатах инспекции
    val remarks: String = "", // Замечания к документации
    val controlRows: String = "", // Поле для хранения JSON списка ControlRow

    // -------- Фиксация объемов --------
    val fixVolumesRows: String = "", // Поле для хранения JSON списка FixVolumesRow

    val isSend: Boolean = false,

    val isCompleted: Boolean = false // Новое поле для проверки завершения отчета


)