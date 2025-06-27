package com.example.epi.Fragments.Transport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

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

        return when {
            customerName.value.isNullOrBlank() -> "Введите заказчика"
            contractCustomer.value.isNullOrBlank() -> "Введите договор СК"
            executorName.value.isNullOrBlank() -> "Введите исполнителя по транспорту"
            contractTransport.value.isNullOrBlank() -> "Введите договор по транспорту"
            startDate.value.isNullOrBlank() -> "Введите дату начала"
            startTime.value.isNullOrBlank() -> "Введите время начала"
            stateNumber.value.isNullOrBlank() -> "Введите гос. номер"
            endDate.value.isNullOrBlank() -> "Введите дату окончания"
            endTime.value.isNullOrBlank() -> "Введите время окончания"
            else -> null
        }
    }
}
