package com.example.epi.Fragments.Control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.Entities.ReportEntity
import com.example.epi.DataBase.ReportRepository
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.ViewModel.RowValidationResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ControlViewModel(private val repository: ReportRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val gson = Gson()

    // LiveData для UI
    private val _orderNumber = MutableLiveData("")
    val orderNumber: LiveData<String> = _orderNumber

    private val _isViolation = MutableLiveData(false)
    val isViolation: LiveData<Boolean> = _isViolation

    private val _controlRows = MutableLiveData<List<ControlRow>>(emptyList())
    val controlRows: LiveData<List<ControlRow>> = _controlRows

    private val _currentDate = MutableLiveData(dateFormat.format(Date()))
    val currentDate: LiveData<String> = _currentDate

    private val _startDate = MutableLiveData("")
    val startDate: LiveData<String> = _startDate

    private val _startTime = MutableLiveData("")
    val startTime: LiveData<String> = _startTime

    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> = _errorEvent

    // Списки для выбора
    val equipmentNames = listOf(
        "Прибор 1", "Прибор 2", "Прибор 3",
        "Прибор 4", "Прибор 5", "Прибор 6",
        "Прибор 7", "Прибор 8", "Прибор 9",
        "Прибор 10", "Прибор 11", "Прибор 12"
    )

    val controlWorkTypes = listOf(
        "Комплекс работ 1", "Комплекс работ 2", "Комплекс работ 3",
        "Комплекс работ 4", "Комплекс работ 5", "Комплекс работ 6",
        "Комплекс работ 7", "Комплекс работ 8", "Комплекс работ 9",
        "Комплекс работ 10", "Комплекс работ 11", "Комплекс работ 12"
    )

    fun generateOrderNumber() {
        try {
            val dateStr = _startDate.value?.takeIf { it.isNotBlank() } ?: dateFormat.format(Date())
            val dateParts = dateStr.split(".")
            if (dateParts.size == 3) {
                val month = dateParts[1].padStart(2, '0')
                val day = dateParts[0].padStart(2, '0')
                val orderNum = (_controlRows.value?.size ?: 0) + 1
                _orderNumber.value = "0000.$month$day.$orderNum"
            }
        } catch (e: Exception) {
            _errorEvent.value = "Ошибка генерации номера"
        }
    }

    fun setViolation(checked: Boolean) {
        _isViolation.value = checked
        _orderNumber.value = if (checked) "Нет нарушения" else ""
    }

    fun addRow(row: ControlRow) {
        val currentList = _controlRows.value?.toMutableList() ?: mutableListOf()
        currentList.add(row)
        _controlRows.value = currentList
    }

    fun removeRow(row: ControlRow) {
        _controlRows.value = _controlRows.value?.filterNot { it == row }
    }

    fun updateRow(oldRow: ControlRow, newRow: ControlRow) {
        _controlRows.value = _controlRows.value?.map { if (it == oldRow) newRow else it }
    }

    fun validateRowInput(input: RowInput): RowValidationResult {
        return when {
            input.equipmentName.isBlank() -> RowValidationResult.Invalid("Укажите оборудование")
            input.workType.isBlank() -> RowValidationResult.Invalid("Укажите вид работ")
            input.report.isBlank() -> RowValidationResult.Invalid("Заполните отчет")
            input.remarks.isBlank() -> RowValidationResult.Invalid("Добавьте примечание")
            !input.isViolationChecked && input.orderNumber.isBlank() ->
                RowValidationResult.Invalid("Укажите номер предписания")
            else -> RowValidationResult.Valid
        }
    }

    suspend fun updateControlReport(): Long {
        return try {
            val existingReport = repository.getLastUnsentReport() ?: run {
                _errorEvent.value = "Не найден отчет для обновления"
                return 0L
            }

            val rows = _controlRows.value ?: run {
                _errorEvent.value = "Добавьте хотя бы одну строку контроля"
                return 0L
            }

            val updatedReport = existingReport.copy(
                order_number = _orderNumber.value ?: "",
                in_violation = _isViolation.value ?: false,
                start_date = _startDate.value ?: "",
                start_time = _startTime.value ?: "",
                equipment = rows.firstOrNull()?.equipmentName ?: "",
                complex_work = rows.firstOrNull()?.workType ?: "",
                report_text = rows.firstOrNull()?.report ?: "",
                remarks = rows.firstOrNull()?.remarks ?: "",
                controlRows = gson.toJson(rows)
            )

            repository.updateReport(updatedReport)
            updatedReport.id
        } catch (e: Exception) {
            _errorEvent.value = "Ошибка сохранения: ${e.message}"
            0L
        }
    }

    fun loadPreviousReport() {
        viewModelScope.launch {
            try {
                val reportEntity = repository.getLastUnsentReport() ?: return@launch

                _orderNumber.value = reportEntity.order_number
                _isViolation.value = reportEntity.in_violation
                _startDate.value = reportEntity.start_date
                _startTime.value = reportEntity.start_time

                val rows = reportEntity.controlRows?.let { json ->
                    if (json.isNotBlank()) {
                        try {
                            gson.fromJson<List<ControlRow>>(
                                json,
                                object : TypeToken<List<ControlRow>>() {}.type
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                } ?: if (reportEntity.equipment?.isNotBlank() == true) {
                    listOf(
                        ControlRow(
                            reportEntity.equipment ?: "",
                            reportEntity.complex_work ?: "",
                            reportEntity.order_number ?: "",
                            reportEntity.report_text ?: "",  // Используем правильное имя поля
                            reportEntity.remarks ?: ""
                        )
                    )
                } else {
                    emptyList()
                }

                _controlRows.value = rows ?: emptyList()
            } catch (e: Exception) {
                _errorEvent.value = "Ошибка загрузки: ${e.message}"
            }
        }
    }
}