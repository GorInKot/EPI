package com.example.epi.Fragments.Control

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.ReportRepository
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.RowValidationResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ControlViewModel(val repository: ReportRepository): ViewModel() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val gson = Gson()

    // ---------- ControlViewModel перенос ----------
    private var orderCounter = 1
    private var extraOrderNumber = 1

    private val _orderNumber = MutableLiveData<String>("")
    val orderNumber: LiveData<String> get() = _orderNumber

    private val _isViolation = MutableLiveData<Boolean>(false)
    val isViolation: LiveData<Boolean> get() = _isViolation

    private val _controlRows = MutableLiveData<List<ControlRow>>(emptyList())
    val controlRow: LiveData<List<ControlRow>> get() = _controlRows

    // Добавляем поля для даты и времени
    private val _currentDate = MutableLiveData<String>(dateFormat.format(System.currentTimeMillis()))
    val currentDate: LiveData<String> get() = _currentDate

    private val _startDate = MutableLiveData<String>("")
    val startDate: LiveData<String> get() = _startDate

    private val _startTime = MutableLiveData<String>("")
    val startTime: LiveData<String> get() = _startTime

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

    // ---------- Событие ошибки ----------
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    fun generateOrderNumber() {

        try {
            // Используем startDate, если задано, иначе текущую дату
            val dateStr = _startDate.value.takeIf { !it.isNullOrBlank() }
                ?: dateFormat.format(System.currentTimeMillis())
            val formatterInputDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date = LocalDate.parse(dateStr, formatterInputDate)
            val formattedDate = date.format(DateTimeFormatter.ofPattern("MMdd"))

            val personNumber = "0000"

            val generatedNumber = "$personNumber.$formattedDate.$orderCounter"

            // Инкрементируем порядковый номер
            orderCounter++

            // Проверяем, включено ли нарушение
            _orderNumber.value = if (_isViolation.value == true) "Нет нарушения" else generatedNumber

        } catch (e: Exception) {
            _orderNumber.value = "Ошибка генерации номера"
            e.printStackTrace()
        }
    }

    // Методы для установки startDate и startTime (если пользователь задает их)
    fun setStartDate(date: String) {
        _startDate.value = date
    }

    fun setStartTime(time: String) {
        _startTime.value = time
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
        val currentList = _controlRows.value?.toMutableList() ?: return
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _controlRows.value = currentList
        } else {
            // Логирование для отладки
            android.util.Log.w("ControlViewModel", "Row not found: $oldRow")
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

    fun validateControlInputs(
        isViolation: Boolean,
        orderNumber: String?,
        startDate: String?,
        startTime: String?,
        controlRows: List<ControlRow>?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (!isViolation && orderNumber.isNullOrBlank()) {
            errors["orderNumber"] = "Укажите номер предписания"
        }
        if (startDate.isNullOrBlank()) {
            errors["startDate"] = "Укажите дату начала"
        }
        if (startTime.isNullOrBlank()) {
            errors["startTime"] = "Укажите время начала"
        }
        if (controlRows.isNullOrEmpty()) {
            errors["controlRows"] = "Добавьте хотя бы одну строку контроля"
        } else {
            controlRows.forEachIndexed { index, row ->
                val input = RowInput(
                    equipmentName = row.equipmentName,
                    workType = row.workType,
                    report = row.report,
                    remarks = row.remarks,
                    orderNumber = row.orderNumber,
                    isViolationChecked = isViolation
                )
                when (val result = validateRowInput(input)) {
                    is RowValidationResult.Invalid -> {
                        errors["row_$index"] = result.reason
                    }
                    else -> {}
                }
            }
        }
        return errors
    }

    suspend fun updateControlReport(): Long {
        try {
            val errors = validateControlInputs(
                isViolation = _isViolation.value ?: false,
                orderNumber = _orderNumber.value,
                startDate = _startDate.value,
                startTime = _startTime.value,
                controlRows = _controlRows.value
            )
            if (errors.isNotEmpty()) {
                Log.e("Tagg", "Control: Validation failed: $errors")
                _errorEvent.postValue("Не все поля заполнены корректно")
                return 0L
            }

            val existingReport = repository.getLastUnsentReport()
            if (existingReport == null) {
                Log.e("Tagg", "Control: No unsent report found")
                _errorEvent.postValue("Ошибка: нет незавершенного отчета")
                return 0L
            }

            // Сериализуем controlRows в JSON
            val controlRowsJson = gson.toJson(_controlRows.value)

            // Получаем данные первой строки для обратной совместимости
            val firstRow = _controlRows.value?.firstOrNull()

            val updatedReport = existingReport.copy(
                orderNumber = if (_isViolation.value == true) "Нет нарушения" else _orderNumber.value.orEmpty(),
                inViolation = _isViolation.value ?: false,
                startDate = _startDate.value.orEmpty(),
                startTime = _startTime.value.orEmpty(),
                equipment = firstRow?.equipmentName ?: "",
                complexWork = firstRow?.workType ?: "",
                report = firstRow?.report ?: "",
                remarks = firstRow?.remarks ?: "",
                controlRows = controlRowsJson
            )

            Log.d("Tagg", "Control: Updating Report: $updatedReport")
            repository.updateReport(updatedReport)
            Log.d("Tagg", "Control: Report updated successfully with ID: ${updatedReport.id}")
            return updatedReport.id
        } catch (e: Exception) {
            Log.e("Tagg", "Control: Error in updateControlReport: ${e.message}", e)
            _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
            return 0L
        }
    }

    fun loadPreviousReport() {
        viewModelScope.launch {
            try {
                val report = repository.getLastUnsentReport()
                report?.let {
                    _orderNumber.value = it.orderNumber
                    _isViolation.value = it.inViolation
                    _startDate.value = it.startDate
                    _startTime.value = it.startTime
                    // Десериализуем controlRows из JSON
                    val controlRows = if (it.controlRows.isNotBlank()) {
                        try {
                            val type = object : TypeToken<List<ControlRow>>() {}.type
                            gson.fromJson(it.controlRows, type) ?: emptyList()
                        } catch (e: Exception) {
                            Log.e("Tagg", "Control: Error parsing controlRows JSON: ${e.message}")
                            emptyList<ControlRow>()
                        }
                    } else {
                        // Обратная совместимость: если controlRows пустое, используем одиночные поля
                        if (it.equipment.isNotBlank()) {
                            listOf(
                                ControlRow(
                                    equipmentName = it.equipment,
                                    workType = it.complexWork,
                                    report = it.report,
                                    remarks = it.remarks,
                                    orderNumber = it.orderNumber
                                )
                            )
                        } else {
                            emptyList()
                        }
                    }
                    _controlRows.value = controlRows
                }
            } catch (e: Exception) {
                _errorEvent.postValue("Ошибка при загрузке отчета: ${e.message}")
            }
        }
    }

    fun clearControl() {
        _orderNumber.value = ""
        _isViolation.value = false
        _startDate.value = ""
        _startTime.value = ""
        _controlRows.value = emptyList()
    }
}

