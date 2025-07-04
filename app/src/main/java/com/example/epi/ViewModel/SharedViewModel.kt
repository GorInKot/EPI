package com.example.epi.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.Control.Model.RowInput
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class SharedViewModel : ViewModel() {

    // ---------- Общее ----------
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private fun now(): Date = Date()

    val arrangementIsClearing = MutableLiveData(false)

    // ---------- Дата и время ----------
    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> = _currentTime

    // ---------- Состояния чекбоксов ----------
    val isManualCustomer = MutableLiveData(false)
    val isManualObject = MutableLiveData(false)
    val isManualContractor = MutableLiveData(false)
    val isManualSubContractor = MutableLiveData(false)

    // ---------- Ручной ввод ----------
    val manualCustomer = MutableLiveData<String>()
    val manualObject = MutableLiveData<String>()
    val manualContractor = MutableLiveData<String>()
    val manualSubContractor = MutableLiveData<String>()

    // ---------- Списки выбора ----------
    val workTypes = listOf("Вахта", "Стандартный", "Суммированный")
    val customers = listOf("Заказчик 1", "Заказчик 2", "Заказчик 3", "Заказчик 4", "Заказчик 5")
    val objects = listOf("Объект 1", "Объект 2", "Объект 3", "Объект 4", "Объект 5")
    val contractors = listOf("Генподрядчик 1", "Генподрядчик 2", "Генподрядчик 3", "Генподрядчик 4", "Генподрядчик 5")
    val subContractors = listOf(
        "Представитель Генподрядчика 1", "Представитель Генподрядчика 2",
        "Представитель Генподрядчика 3", "Представитель Генподрядчика 4",
        "Представитель Генподрядчика 5"
    )

    // ---------- Текстовые поля ----------
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

    // ---------- Выпадающие списки ----------
    val selectedWorkType = MutableLiveData<String>()
    val selectedCustomer = MutableLiveData<String>()
    val selectedObject = MutableLiveData<String>()
    val selectedContractor = MutableLiveData<String>()
    val selectedSubContractor = MutableLiveData<String>()

    // ---------- Arrangement  ----------
    fun validateArrangementInputs(

        workTypes: String?,

        customers: String?,
        manualCustomer: String?,

        objects: String?,
        manualObject: String?,

        plotText: String?,

        contractors: String?,
        manualContractor: String?,

        subContractors: String?,
        manualSubContractor: String?,

        repSSKGpText: String?,
        subContractorText: String?,
        repSubcontractorText: String?,
        repSSKSubText: String?

    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        // Режим работы
        if (workTypes.isNullOrBlank()) {
            errors["workTypes"] = "Укажите режим работы"
        }
        // Заказчик
        if (customers.isNullOrBlank() && manualCustomer.isNullOrBlank()) {
            errors["customers"] = "Укажите заказчика"
        }
        // Объект
        if (objects.isNullOrBlank() && manualObject.isNullOrBlank()) {
            errors["objects"] = "Укажите Объект"
        }
        // Участок
        if (plotText.isNullOrBlank()) {
            errors["plotText"] = "Укажите Участок"
        }
        // Генподрядчика
        if (contractors.isNullOrBlank() && manualContractor.isNullOrBlank()) {
            errors["contractors"] = "Укажите Генподрядчика"
        }
        // Представитель Генподрядчика
        if (subContractors.isNullOrBlank() && manualSubContractor.isNullOrBlank()) {
            errors["subContractors"] = "Укажите Представитель Генподрядчика"
        }
        // Представитель ССК ПО (ГП)
        if (repSSKGpText.isNullOrBlank()) {
            errors["repSSKGpText"] = "Укажите Представителя ССК ПО (ГП)"
        }
        // Субподрядчик
        if (subContractorText.isNullOrBlank()) {
            errors["subContractorText"] = "Укажите Субподрядчика"
        }
        // Представитель Субподрядчика
        if (repSubcontractorText.isNullOrBlank()) {
            errors["repSubcontractorText"] = "Укажите Представителя Субподрядчика"
        }
        // Представитель ССК ПО (ГП)
        if (repSSKSubText.isNullOrBlank()) {
            errors["repSSKSubText"] = "Укажите Представителя ССК ПО (Суб)"
        }

        return errors
    }


    // ---------- ControlViewModel перенос ----------
    private var orderCounter = 1

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

    // ---------- FixVolumesViewModel перенос ----------
    private val _fixRows = MutableLiveData<List<FixVolumesRow>>(emptyList())
    val fixRows: LiveData<List<FixVolumesRow>> get() = _fixRows

    val fixWorkType = MutableLiveData<List<String>>(
        listOf(
            "Вид работ 1", "Вид работ 2", "Вид работ 3",
            "Вид работ 4", "Вид работ 5", "Вид работ 6",
            "Вид работ 7", "Вид работ 7", "Вид работ 9",
            "Вид работ 10", "Вид работ 11", "Вид работ 12"
        )
    )

    val fixMeasures = MutableLiveData<List<String>>(
        listOf(
            "м",        // метры (длина, высота, глубина)
            "м2",       // квадратные метры (площадь)
            "м3",       // кубические метры (объём)
            "мм",       // миллиметры (точные измерения)
            "см",       // сантиметры
            "т",        // тонны (масса)
            "кг",       // килограммы (масса)
            "шт.",      // штуки (штучные элементы)
            "п.м.",     // погонные метры (трубы, кабели, бордюры)
            "л",        // литры (жидкости)
            "м/ч",      // метры в час (производительность)
            "м/с",      // метры в секунду (скорость перемещения, например бетона)
            "градусы",        // градусы (углы, наклоны)
            "%",        // проценты (уклон, влажность, заполняемость)
            "МПа",      // мегапаскали (прочность бетона, давление)
            "ч",        // часы (время выполнения, сушки и т.д.)
            "сут.",     // сутки
        )
    )

    val measuresOld = MutableLiveData<List<String>>(
        listOf(
            "м",        // метры (длина, высота, глубина)
            "м²",       // квадратные метры (площадь)
            "м³",       // кубические метры (объём)
            "мм",       // миллиметры (точные измерения)
            "см",       // сантиметры
            "т",        // тонны (масса)
            "кг",       // килограммы (масса)
            "шт.",      // штуки (штучные элементы)
            "п.м.",     // погонные метры (трубы, кабели, бордюры)
            "л",        // литры (жидкости)
            "м/ч",      // метры в час (производительность)
            "м/с",      // метры в секунду (скорость перемещения, например бетона)
            "градусы (°)",        // градусы (углы, наклоны)
            "%",        // проценты (уклон, влажность, заполняемость)
            "МПа",      // мегапаскали (прочность бетона, давление)
            "ч",        // часы (время выполнения, сушки и т.д.)
            "сут.",     // сутки
        )
    )
    fun addFixRow(fixRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: mutableListOf()
        current.add(fixRow)
        _fixRows.value = current
    }

    fun removeFixRow(fixRows: FixVolumesRow) {
        _fixRows.value = _fixRows.value?.filterNot { it == fixRows }
    }

    fun updateFixRow(oldRow: FixVolumesRow, newRow: FixVolumesRow) {
        val current = _fixRows.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it == oldRow }
        if (index != -1) {
            current[index] = newRow
            _fixRows.value = current
        }
    }

    // ---------- Получение даты и времени начала поездки -------
    // ---------- для генерации номера предписания -------

    private var extraOrderNumber = 1
    fun generateOrderNumber() {

        try {
            val dateStr = startDate.value ?: return
            val timeStr = startTime.value ?: return

            val formatterInputDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val formatterInputTime = DateTimeFormatter.ofPattern("HH:mm")

            val date = LocalDate.parse(dateStr, formatterInputDate)
            val time = LocalTime.parse(timeStr, formatterInputTime)

            val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val formattedTime = time.format(DateTimeFormatter.ofPattern("HHmm"))

            // TODO - получение табельного номера
            val personNumber = "0000" // пока хардкод

            val generatedNumber = "$personNumber.$formattedDate.$formattedTime.${extraOrderNumber++}"

            _orderNumber.value = generatedNumber
        } catch (e: Exception) {
            _orderNumber.value = "Ошибка генерации номера"
            e.printStackTrace()
        }
    }


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
            && !endDate.isNullOrBlank() && !endTime.isNullOrBlank()) {

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

    fun validateTransportStartBeforeEnd(): String? {
        val start = startDate.value.orEmpty() + " " + startTime.value.orEmpty()
        val end = endDate.value.orEmpty() + " " + endTime.value.orEmpty()

        return try {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val startDateTime = format.parse(start)
            val endDateTime = format.parse(end)

            if (startDateTime != null && endDateTime != null && startDateTime.after(endDateTime)) {
                "Время окончания не может быть раньше времени начала"
            } else null
        } catch (e: Exception) {
            "Ошибка формата даты/времени"
        }
    }


    // ---------- Инициализация ----------
    init {
        val current = now()
        _currentDate.value = dateFormat.format(current)
        _currentTime.value = timeFormat.format(current)
    }

    fun updateDateTime() {
        val current = now()
        _currentDate.value = dateFormat.format(current)
        _currentTime.value = timeFormat.format(current)
    }

    // ---------- Текстовые обновления ----------
    fun onPlotChanged(newText: String) {
        _plotText.value = newText
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

    // ---------- Очистка ----------
    fun clearAll() {
        Log.d("ViewModel", "Start clearing")
        arrangementIsClearing.value = true

        selectedWorkType.value = ""
        selectedCustomer.value = ""
        selectedObject.value = ""
        selectedContractor.value = ""
        selectedSubContractor.value = ""

        manualCustomer.value = ""
        manualObject.value = ""
        manualContractor.value = ""
        manualSubContractor.value = ""

        isManualCustomer.value = false
        isManualObject.value = false
        isManualContractor.value = false
        isManualSubContractor.value = false

        _plotText.value = ""
        _repSSKGpText.value = ""
        _subContractorText.value = ""
        _repSubcontractorText.value = ""
        _repSSKSubText.value = ""

        _controlRows.value = emptyList()
        _orderNumber.value = ""
        _isViolation.value = false

        // Очистка данных транспорта
        _isTransportAbsent.value = false
        customerName.value = ""
        contractCustomer.value = ""
        executorName.value = ""
        contractTransport.value = ""
        stateNumber.value = ""
        startDate.value = ""
        startTime.value = ""
        endDate.value = ""
        endTime.value = ""

        arrangementIsClearing.value = false
        Log.d("ViewModel", "End clearing")
    }

}

sealed class RowValidationResult {
    object Valid: RowValidationResult()
    data class Invalid(val reason: String): RowValidationResult()
}
