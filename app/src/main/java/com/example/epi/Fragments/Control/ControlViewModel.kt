package com.example.epi.Fragments.Control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.ViewModel.RowValidationResult
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ControlViewModel: ViewModel() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // ---------- ControlViewModel перенос ----------
    private var orderCounter = 1
    private var extraOrderNumber = 1

    private val _orderNumber = MutableLiveData<String>("")
    val orderNumber: LiveData<String> get() = _orderNumber

    private val _isViolation = MutableLiveData<Boolean>(false)
    val isViolation: LiveData<Boolean> get() = _isViolation

    private val _controlRows = MutableLiveData<List<ControlRow>>(emptyList())
    val controlRow: LiveData<List<ControlRow>> get() = _controlRows

    val equipmentNames = MutableLiveData<List<String>>(
        listOf(
            "Прибор 1", "Прибор 2", "Прибор 3",
            "Прибор 4", "Прибор 5", "Прибор 6",
            "Прибор 7", "Прибор 8", "Прибор 9",
            "Прибор 10", "Прибор 11", "Прибор 12"
        )
    )

    val controlWorkTypes = MutableLiveData<List<String>>(
        listOf(
            "Комплекс работ 1", "Комплекс работ 2", "Комплекс работ 3",
            "Комплекс работ 4", "Комплекс работ 5", "Комплекс работ 6",
            "Комплекс работ 7", "Комплекс работ 7", "Комплекс работ 9",
            "Комплекс работ 10", "Комплекс работ 11", "Комплекс работ 12"
        )
    )

    // ---- Получение даты и времени начала поездки ----
    // ---- для генерации номера предписания ----


//    fun generateOrderNumber() {
//
//        try {
//            val dateStr = startDate.value ?: return
//
//            val formatterInputDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
//
//            val date = LocalDate.parse(dateStr, formatterInputDate)
//
//
//            val formattedDate = date.format(DateTimeFormatter.ofPattern("MMdd"))
//
//            // TODO - получение табельного номера
//            val personNumber = "0000" // пока хардкод
//
//            val generatedNumber = "$personNumber.$formattedDate.${extraOrderNumber++}"
//
//            _orderNumber.value = generatedNumber
//        } catch (e: Exception) {
//            _orderNumber.value = "Ошибка генерации номера"
//            e.printStackTrace()
//        }
//    }

    fun setViolation(checked: Boolean) {
        _isViolation.value = checked
        _orderNumber.value = if (checked) "Нет нарушения" else ""
    }

    fun addRow(row: ControlRow) {
        _controlRows.value = _controlRows.value?.plus(row)
    }

    fun removeRow(row: ControlRow) {
        _controlRows.value = _controlRows.value?.filterNot { it == row }
    }

    fun updateRow(oldRow: ControlRow, newRow: ControlRow) {
        val currentList = _controlRows.value?.toMutableList() ?: return
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _controlRows.value = currentList
        }
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