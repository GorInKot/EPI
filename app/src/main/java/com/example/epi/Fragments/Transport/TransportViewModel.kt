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
    fun validateInputs(): Boolean {
        if(_isTransportAbsent.value == true) return true

        return listOf(
            customerName.value,
            contractCustomer.value,
            executorName.value,
            contractTransport.value,
            stateNumber.value,
            startDate.value,
            startTime.value,
            endDate.value,
            endTime.value
        ).all { !it.isNullOrBlank() }
    }
}
