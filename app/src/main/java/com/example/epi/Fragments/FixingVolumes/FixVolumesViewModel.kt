package com.example.epi.Fragments.FixingVolumes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.ReportRepository
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.ViewModel.RowValidationResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class FixVolumesViewModel(private val repository: ReportRepository) : ViewModel() {

    private val gson = Gson()

    // ---------- FixVolumesViewModel data ----------
    private val _fixRows = MutableLiveData<List<FixVolumesRow>>(emptyList())
    val fixRows: LiveData<List<FixVolumesRow>> get() = _fixRows

    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    val fixWorkType = MutableLiveData<List<String>>(
        listOf(
            "Вид работ 1", "Вид работ 2", "Вид работ 3",
            "Вид работ 4", "Вид работ 5", "Вид работ 6",
            "Вид работ 7", "Вид работ 8", "Вид работ 9",
            "Вид работ 10", "Вид работ 11", "Вид работ 12"
        )
    )

    val fixMeasures = MutableLiveData<List<String>>(
        listOf(
            "м", "м2", "м3", "мм", "см", "т", "кг", "шт.", "п.м.", "л",
            "м/ч", "м/с", "градусы", "%", "МПа", "ч", "сут."
        )
    )

    fun addFixRow(fixRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: mutableListOf()
        current.add(fixRow)
        _fixRows.value = current
    }

    fun removeFixRow(fixRow: FixVolumesRow) {
        _fixRows.value = _fixRows.value?.filterNot { it == fixRow }
    }

    fun updateFixRow(oldRow: FixVolumesRow, newRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it == oldRow }
        if (index != -1) {
            current[index] = newRow
            _fixRows.value = current
        } else {
            Log.w("FixVolumesViewModel", "Row not found: $oldRow")
        }
    }

    // Валидация одной строки FixVolumesRow
    fun validateFixRowInput(input: FixVolumesRow): RowValidationResult {
        return when {
            input.ID_object.isBlank() -> RowValidationResult.Invalid("ID объекта не указан")
            _fixRows.value?.any { it.ID_object != input.ID_object && it != input } == true ->
                RowValidationResult.Invalid("ID объекта должен совпадать для всех строк")
            input.projectWorkType.isBlank() -> RowValidationResult.Invalid("Вид работ не указан")
            input.measure.isBlank() -> RowValidationResult.Invalid("Единица измерения не указана")
            input.plan.isBlank() -> RowValidationResult.Invalid("План не указан")
            input.fact.isBlank() -> RowValidationResult.Invalid("Факт не указан")
            input.plan.toDoubleOrNull() == null -> RowValidationResult.Invalid("План должен быть числом")
            input.fact.toDoubleOrNull() == null -> RowValidationResult.Invalid("Факт должен быть числом")
            input.fact.toDouble() > input.plan.toDouble() -> RowValidationResult.Invalid("Факт не может превышать План")
            else -> RowValidationResult.Valid
        }
    }

    // Валидация всех данных FixVolumesFragment
    fun validateFixVolumesInputs(fixRows: List<FixVolumesRow>?): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (fixRows.isNullOrEmpty()) {
            errors["fixRows"] = "Добавьте хотя бы одну строку фиксации объемов"
        } else {
            fixRows.forEachIndexed { index, row ->
                when (val result = validateFixRowInput(row)) {
                    is RowValidationResult.Invalid -> {
                        errors["row_$index"] = result.reason
                    }
                    else -> {}
                }
            }
        }
        return errors
    }

    // Сохранение данных в базу данных Room
    suspend fun updateFixVolumesReport(): Long {
        try {
            val errors = validateFixVolumesInputs(fixRows = _fixRows.value)
            if (errors.isNotEmpty()) {
                Log.e("Tagg", "FixVolumes: Validation failed: $errors")
                _errorEvent.postValue("Не все поля заполнены корректно")
                return 0L
            }

            val existingReport = repository.getLastUnsentReport()
            if (existingReport == null) {
                Log.e("Tagg", "FixVolumes: No unsent report found")
                _errorEvent.postValue("Ошибка: нет незавершенного отчета")
                return 0L
            }

            // Сериализуем fixRows в JSON
            val fixRowsJson = gson.toJson(_fixRows.value)

            val updatedReport = existingReport.copy(
                fixVolumesRows = fixRowsJson
            )

            Log.d("Tagg", "FixVolumes: Updating Report: $updatedReport")
            repository.updateReport(updatedReport)
            Log.d("Tagg", "FixVolumes: Report updated successfully with ID: ${updatedReport.id}")
            return updatedReport.id
        } catch (e: Exception) {
            Log.e("Tagg", "FixVolumes: Error in updateFixVolumesReport: ${e.message}", e)
            _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
            return 0L
        }
    }

    // Загрузка данных из последнего незавершенного отчета
    fun loadPreviousReport() {
        viewModelScope.launch {
            try {
                val report = repository.getLastUnsentReport()
                report?.let {
                    // Десериализуем fixVolumesRows из JSON
                    val fixRows = if (it.fixVolumesRows.isNotBlank()) {
                        try {
                            val type = object : TypeToken<List<FixVolumesRow>>() {}.type
                            gson.fromJson(it.fixVolumesRows, type) ?: emptyList()
                        } catch (e: Exception) {
                            Log.e("Tagg", "FixVolumes: Error parsing fixVolumesRows JSON: ${e.message}")
                            emptyList<FixVolumesRow>()
                        }
                    } else {
                        emptyList()
                    }
                    _fixRows.value = fixRows
                }
            } catch (e: Exception) {
                _errorEvent.postValue("Ошибка при загрузке отчета: ${e.message}")
            }
        }
    }

    // Очистка всех полей
    fun clearFixVolumes() {
        _fixRows.value = emptyList()
    }
}