package com.example.epi.Fragments.Control

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ControlViewModel : ViewModel() {

    private var orderCounter = 1

    private val _orderNumber = MutableLiveData<String>("")
    val orderNumber: LiveData<String> get() = _orderNumber

    private val _isViolation = MutableLiveData<Boolean>(false)
    val isViolation: LiveData<Boolean> get() = _isViolation

    private val _rows = MutableLiveData<List<ControlRow>>(emptyList())
    val rows: LiveData<List<ControlRow>> get() = _rows

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    init {
        updateCurrentDate()
    }

    val equipmentNames = MutableLiveData<List<String>>(
        listOf(
            "Прибор 1", "Прибор 2", "Прибор 3",
            "Прибор 4", "Прибор 5", "Прибор 6",
            "Прибор 7", "Прибор 8", "Прибор 9",
            "Прибор 10", "Прибор 11", "Прибор 12"
        )
    )

    val workTypes = MutableLiveData<List<String>>(
        listOf(
            "Комплекс работ 1", "Комплекс работ 2", "Комплекс работ 3",
            "Комплекс работ 4", "Комплекс работ 5", "Комплекс работ 6",
            "Комплекс работ 7", "Комплекс работ 7", "Комплекс работ 9",
            "Комплекс работ 10", "Комплекс работ 11", "Комплекс работ 12"
        )
    )


    fun generateOrderNumber() {
        _orderNumber.value = "1234_${orderCounter++}"
    }

    fun setViolation(checked: Boolean) {
        _isViolation.value = checked
        _orderNumber.value = if (checked) "Нет нарушения" else ""
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
        val currentList = _rows.value?.toMutableList() ?: return
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _rows.value = currentList
        }
    }

    // Обновляем текущую дату
    fun updateCurrentDate() {
        val formatted = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        _currentDate.value = formatted
    }

    fun validateRowInput(input: RowInput): RowValidationResult {
        return when {
            input.equipmentName.isBlank() -> RowValidationResult.Invalid("Оборудование не указано")
            input.workType.isBlank() -> RowValidationResult.Invalid("Вид работ не указан")
            input.report.isBlank() -> RowValidationResult.Invalid("Отчет не заполнен")
            input.remarks.isBlank() -> RowValidationResult.Invalid("Примечание не заполнено")
            !input.isViolationChecked && input.orderNumber.isBlank() ->
                RowValidationResult.Invalid("Номер предписания обязателен")

            else -> RowValidationResult.Valid
        }
    }
}


sealed class RowValidationResult {
    object Valid: RowValidationResult()
    data class Invalid(val reason: String): RowValidationResult()
}
