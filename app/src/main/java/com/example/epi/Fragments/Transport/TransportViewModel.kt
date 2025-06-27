package com.example.epi.Fragments.Transport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransportViewModel : ViewModel() {

    // Чекбокс: транспорт отсутствует
    private val _isTransportAbsent = MutableLiveData(false)
    val isTransportAbsent: LiveData<Boolean> get() = _isTransportAbsent

    fun setTransportAbsent(value: Boolean) {
        _isTransportAbsent.value = value
    }

    // Поля формы
    val customerName = MutableLiveData("")
    val contractCustomer = MutableLiveData("")
    val executorName = MutableLiveData("")
    val contractTransport = MutableLiveData("")
    val stateNumber = MutableLiveData("")
    val startDate = MutableLiveData("") // формат: dd.MM.yyyy
    val startTime = MutableLiveData("") // формат: HH:mm
    val endDate = MutableLiveData("")
    val endTime = MutableLiveData("")

    // Валидация
    fun validateInputs(): String? {
        if (_isTransportAbsent.value == true) return null

        val fields = listOf(
            customerName.value,
            contractCustomer.value,
            executorName.value,
            contractTransport.value,
            stateNumber.value,
            startDate.value,
            startTime.value,
            endDate.value,
            endTime.value
        )

        if (fields.any { it.isNullOrBlank() }) {
            return "Заполните все поля"
        }

        return validateStartBeforeEnd()
    }


    fun validateStartBeforeEnd(): String? {
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
