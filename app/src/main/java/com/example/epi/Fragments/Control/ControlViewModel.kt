package com.example.epi.Fragments.Control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.Control.Model.ControlRow

class ControlViewModel : ViewModel() {

    private var orderCounter = 1

    private val _orderNumber = MutableLiveData<String>("")
    val orderNumber: LiveData<String> get() = _orderNumber

    private val _isViolation = MutableLiveData<Boolean>(false)
    val isViolation: LiveData<Boolean> get() = _isViolation

    private val _rows = MutableLiveData<List<ControlRow>>(emptyList())
    val rows: LiveData<List<ControlRow>> get() = _rows

    fun generateOrderNumber() {
        _orderNumber.value = "1234_${orderCounter++}"
    }

    fun setViolation(checked: Boolean) {
        _isViolation.value = checked
        if (checked) {
            _orderNumber.value = "Нет нарушения"
        } else {
            _orderNumber.value = ""
        }
    }

    fun addRow(row: ControlRow) {
        _rows.value = _rows.value?.plus(row)
    }

    fun removeRow(row: ControlRow) {
        _rows.value = _rows.value?.filterNot {
            it == row
        }
    }

    fun updateRow(oldRow: ControlRow, newRow: ControlRow) {
        val currentList = _rows.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _rows.value = currentList
        }
    }
}
