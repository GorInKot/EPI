package com.example.epi.Fragments.Transport

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.DataBase.ReportRepository
import java.text.SimpleDateFormat
import java.util.Locale

class TransportViewModel(val repository: ReportRepository): ViewModel() {

    // ---------- TransportViewModel data and functions ----------

    // Чекбокс: транспорт отсутствует
    private val _isTransportAbsent = MutableLiveData(false)
    val isTransportAbsent: LiveData<Boolean> get() = _isTransportAbsent

    fun setTransportAbsent(value: Boolean) {
        _isTransportAbsent.value = value
    }

    // Поля формы транспорта
    val customerName = MutableLiveData("")
    val contractCustomer = MutableLiveData("")
    val executorName = MutableLiveData("")
    val contractTransport = MutableLiveData("")
    val stateNumber = MutableLiveData("")
    val startDate = MutableLiveData("") // формат: dd.MM.yyyy
    val startTime = MutableLiveData("") // формат: HH:mm
    val endDate = MutableLiveData("")
    val endTime = MutableLiveData("")

    val transportInClearing = MutableLiveData(false)

    // ---------- Событие ошибки ----------
    private val _errorEvent = MutableLiveData<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    fun clearTransport() {

        transportInClearing.value = true

        customerName.value = ""
        contractCustomer.value = ""
        executorName.value = ""
        contractTransport.value = ""
        stateNumber.value = ""
        startDate.value = ""
        startTime.value = ""
        endTime.value = ""
        endDate.value = ""

        transportInClearing.value = false
    }

    // Валидация транспорта
    fun validateTransportInputs(
        _isTransportAbsent: Boolean,
        customerName: String?,
        contractCustomer: String?,
        executorName: String?,
        contractTransport: String?,
        stateNumber: String?,
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?,
    ): Map<String, String?> {

        val errors = mutableMapOf<String, String?>()

        if (_isTransportAbsent) return errors

        if (customerName.isNullOrBlank()) {
            errors["customerName"] = "Укажите Заказчика"
        }
        if (contractCustomer.isNullOrBlank()) {
            errors["contractCustomer"] = "Укажите договор СК"
        }
        if (executorName.isNullOrBlank()) {
            errors["executorName"] = "Укажите исполнителя по транспорту"
        }
        if (contractTransport.isNullOrBlank()) {
            errors["contractTransport"] = "Укажите договор по транспорту"
        }
        if (stateNumber.isNullOrBlank()) {
            errors["stateNumber"] = "Укажите госномер"
        }
        if (startDate.isNullOrBlank()) {
            errors["startDate"] = "Укажите дату начала поездки"
        }
        if (startTime.isNullOrBlank()) {
            errors["startTime"] = "Укажите время начала поездки"
        }
        if (endDate.isNullOrBlank()) {
            errors["endDate"] = "кажите дату завершения поездки"
        }
        if (endTime.isNullOrBlank()) {
            errors["endTime"] = "Укажите время завершения поездки"
        }
        if (!startDate.isNullOrBlank() && !startTime.isNullOrBlank()
            && !endDate.isNullOrBlank() && !endTime.isNullOrBlank()
        ) {

            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val start = "${startDate.trim()} ${startTime.trim()}"
            val end = "${endDate.trim()} ${endTime.trim()}"

            try {
                val startParsed = format.parse(start)
                val endParsed = format.parse(end)

                if (startParsed != null && endParsed != null && startParsed.after(endParsed)) {
                    errors["endTime"] = "Окончание не может быть раньше начала"
                }
            } catch (e: Exception) {
                errors["startDate"] = "Ошибка формата даты/времени"
                errors["endDate"] = "Ошибка формата даты/времени"
            }
        }

        return errors
    }

    // Валидация гос номера
    fun isValidStateNumber(number: String): Boolean {
        // Формат: А 123 БВ 45 или А 123 БВ 456
        // А, Б, В — допустимые кириллические буквы (АВЕКМНОРСТУХ)
        return number.matches(Regex("^[АВЕКМНОРСТУХ]\\s\\d{3}\\s[АВЕКМНОРСТУХ]{2}\\s\\d{2,3}$"))
    }

    suspend fun updateTransportReport(): Long {
        try {
            val errors = validateTransportInputs(
                _isTransportAbsent = isTransportAbsent.value ?: false,
                customerName = customerName.value,
                contractCustomer = contractCustomer.value,
                executorName = executorName.value,
                contractTransport = contractTransport.value,
                stateNumber = stateNumber.value,
                startDate = startDate.value,
                startTime = startTime.value,
                endDate = endDate.value,
                endTime = endTime.value
            )
            if (errors.isNotEmpty()) {
                Log.e("Tagg", "Transport: Validation failed in saveReport: $errors")
                _errorEvent.postValue("Не все поля заполнены корректно")
                return 0L
            }

            val existingReport = repository.getLastUnsentReport()
            if (existingReport == null) {
                Log.e("Tagg", "Transport: No unsent report found to update")
                _errorEvent.postValue("Ошибка: нет незавершенного отчета для обновления")
                return 0L
            }

            // Обновляем отчет данными из TransportFragment
            val updatedReport = existingReport.copy(
                executor = if (isTransportAbsent.value == true) "" else executorName.value.orEmpty(),
                start_date = if (isTransportAbsent.value == true) "" else startDate.value.orEmpty(),
                start_time = if (isTransportAbsent.value == true) "" else startTime.value.orEmpty(),
                state_number = if (isTransportAbsent.value == true) "" else stateNumber.value.orEmpty(), // <-- тут
                contract = if (isTransportAbsent.value == true) "" else contractCustomer.value.orEmpty(),
                contract_transport = if (isTransportAbsent.value == true) "" else contractTransport.value.orEmpty(),
                end_date = if (isTransportAbsent.value == true) "" else endDate.value.orEmpty(),
                end_time = if (isTransportAbsent.value == true) "" else endTime.value.orEmpty(),
                is_empty = isTransportAbsent.value ?: false
            )

            Log.d("Tagg", "Transport: Updating Report: $updatedReport")
            repository.updateReport(updatedReport)
            Log.d("Tagg", "Transport: Report updated successfully with ID: ${updatedReport.id}")
            return updatedReport.id
        } catch (e: Exception) {
            Log.e("Tagg", "Transport: Error in updateTransportReport: ${e.message}", e)
            _errorEvent.postValue("Ошибка при обновлении отчета: ${e.message}")
            return 0L
        }
    }

}