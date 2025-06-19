package com.example.epi.Fragments.Arrangement

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArrangementViewModel : ViewModel() {

    // --------------------------
    // Дата и время
    // --------------------------
    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> = _currentTime

    // --------------------------
    // Списки для выбора
    // --------------------------
    val workTypes = listOf("Вахта", "Стандартный", "Суммированный")
    val customers = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3", "Заказчик 4", "Заказчик 5")
    val objects = listOf("Объект 1", "Объект 2", "Объект 3", "Объект 4", "Объект 5")
    val contractors = listOf("Генподрядчик 1", "Генподрядчик 2", "Генподрядчик 3", "Генподрядчик 4", "Генподрядчик 5")
    val subContractors = listOf(
        "Представитель Генподрядчика 1", "Представитель Генподрядчика 2",
        "Представитель Генподрядчика 3", "Представитель Генподрядчика 4",
        "Представитель Генподрядчика 5"
    )

    // --------------------------
    //  Текстовые поля (ввод)
    // --------------------------
    private val _plotText = MutableLiveData<String>()
    val plotText: LiveData<String> get() = _plotText

    private val _repSSKGpText = MutableLiveData<String>()
    val repSSKGpText: LiveData<String> get() = _repSSKGpText

    private val _subContractorText = MutableLiveData<String>()
    val subContractorText: LiveData<String> get() = _subContractorText

    private val _repSubcontractorText = MutableLiveData<String>()
    val repSubcontractorText: LiveData<String> get() = _repSubcontractorText

    private val _repSSKSubText = MutableLiveData<String>()
    val repSSKSubText: LiveData<String> get() = _repSSKSubText

    // --------------------------
    // Выбранные значения из выпадающих списков
    // --------------------------
    val selectedWorkType = MutableLiveData<String>()
    val selectedCustomer = MutableLiveData<String>()
    val selectedObject = MutableLiveData<String>()
    val selectedContractor = MutableLiveData<String>()
    val selectedSubContractor = MutableLiveData<String>()

    // --------------------------
    // Инициализация даты и времени
    // --------------------------
    init {
        val now = Date()
        _currentDate.value = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(now)
        _currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    }

    // --------------------------
    // Обновление текстов
    // --------------------------
    fun onPlotChanged(newText: String) {
        _plotText.value = newText
        Log.d("ViewModel", "plotText изменилось: $newText")
    }

    fun onRepSSKGpChanged(newText: String) {
        _repSSKGpText.value = newText
    }

    fun onSubContractorChanged(newText: String) {
        _subContractorText.value = newText
    }

    fun onRepSubcontractorChanged(newText: String) {
        _repSubcontractorText.value = newText
    }

    fun onRepSSKSubChanged(newText: String) {
        _repSSKSubText.value = newText
    }


    // --------------------------
    //  Полная очистка формы
    // --------------------------
    fun clearAll() {
        selectedWorkType.value = ""
        selectedCustomer.value = ""
        selectedObject.value = ""
        selectedContractor.value = ""
        selectedSubContractor.value = ""

        _plotText.value = ""
        _repSSKGpText.value = ""
        _subContractorText.value = ""
        _repSubcontractorText.value = ""
        _repSSKSubText.value = ""
    }
}
