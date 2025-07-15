package com.example.epi.Fragments.Transport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class TransportVIewModel: ViewModel() {

    // ---------- TransportViewModel data and functions ----------

    // Чекбокс: транспорт отсутствует
    private val _isTransportAbsent = MutableLiveData(false)
    val isTransportAbsent: LiveData<Boolean> get() = _isTransportAbsent

    fun setTransportAbsent(value: Boolean) {
        _isTransportAbsent.value = value
    }

    // Поля формы транспорта
    val customerName = MutableLiveData("")
    val contractCustomer = MutableLiveData("")
    val executorName = MutableLiveData("")
    val contractTransport = MutableLiveData("")
    val stateNumber = MutableLiveData("")
    val startDate = MutableLiveData("") // формат: dd.MM.yyyy
    val startTime = MutableLiveData("") // формат: HH:mm
    val endDate = MutableLiveData("")
    val endTime = MutableLiveData("")

    val transportInClearing = MutableLiveData(false)

    fun clearTransport() {

        transportInClearing.value = true

        customerName.value = ""
        contractCustomer.value = ""
        executorName.value = ""
        contractTransport.value = ""
        stateNumber.value = ""
        startDate.value = ""
        startTime.value = ""
        endTime.value = ""
        endDate.value = ""

        transportInClearing.value = false
    }

    // Валидация транспорта
    fun validateTransportInputs(
        _isTransportAbsent: Boolean,
        customerName: String?,
        contractCustomer: String?,
        executorName: String?,
        contractTransport: String?,
        stateNumber: String?,
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?,
    ): Map<String, String?> {

        val errors = mutableMapOf<String, String?>()

        if (_isTransportAbsent) return errors

        if (customerName.isNullOrBlank()) {
            errors["customerName"] = "Укажите Заказчика"
        }
        if (contractCustomer.isNullOrBlank()) {
            errors["contractCustomer"] = "Укажите договор СК"
        }
        if (executorName.isNullOrBlank()) {
            errors["executorName"] = "Укажите исполнителя по транспорту"
        }
        if (contractTransport.isNullOrBlank()) {
            errors["contractTransport"] = "Укажите договор по транспорту"
        }
        if (stateNumber.isNullOrBlank()) {
            errors["stateNumber"] = "Укажите госномер"
        }
        if (startDate.isNullOrBlank()) {
            errors["startDate"] = "Укажите дату начала поездки"
        }
        if (startTime.isNullOrBlank()) {
            errors["startTime"] = "Укажите время начала поездки"
        }
        if (endDate.isNullOrBlank()) {
            errors["endDate"] = "кажите дату завершения поездки"
        }
        if (endTime.isNullOrBlank()) {
            errors["endTime"] = "Укажите время завершения поездки"
        }
        if (!startDate.isNullOrBlank() && !startTime.isNullOrBlank()
            && !endDate.isNullOrBlank() && !endTime.isNullOrBlank()
        ) {

            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val start = "${startDate.trim()} ${startTime.trim()}"
            val end = "${endDate.trim()} ${endTime.trim()}"

            try {
                val startParsed = format.parse(start)
                val endParsed = format.parse(end)

                if (startParsed != null && endParsed != null && startParsed.after(endParsed)) {
                    errors["endTime"] = "Окончание не может быть раньше начала"
                }
            } catch (e: Exception) {
                errors["startDate"] = "Ошибка формата даты/времени"
                errors["endDate"] = "Ошибка формата даты/времени"
            }
        }

        return errors
    }

    fun validateTransportStartBeforeEnd(): String? {
        val start = startDate.value.orEmpty() + " " + startTime.value.orEmpty()
        val end = endDate.value.orEmpty() + " " + endTime.value.orEmpty()

        return try {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val startDateTime = format.parse(start)
            val endDateTime = format.parse(end)

            if (startDateTime != null && endDateTime != null && startDateTime.after(endDateTime)) {
                "Время окончания не может быть раньше времени начала"
            } else null
        } catch (e: Exception) {
            "Ошибка формата даты/времени"
        }
    }

}