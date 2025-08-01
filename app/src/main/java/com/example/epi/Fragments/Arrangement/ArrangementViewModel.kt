package com.example.epi.Fragments.Arrangement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.epi.DataBase.Entities.*
import com.example.epi.DataBase.ReportRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ArrangementViewModel(private val repository: ReportRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val isClearing = MutableLiveData(false)

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> = _currentTime

    val isManualCustomer = MutableLiveData(false)
    val isManualObject = MutableLiveData(false)
    val isManualPlot = MutableLiveData(false)
    val isManualContractor = MutableLiveData(false)
    val isManualSubContractor = MutableLiveData(false)

    val manualCustomer = MutableLiveData<String?>()
    val manualObject = MutableLiveData<String?>()
    val manualPlot = MutableLiveData<String?>()
    val manualContractor = MutableLiveData<String?>()
    val manualSubContractor = MutableLiveData<String?>()

    val customers: LiveData<List<CustomerEntity>> = repository.getAllCustomers().asLiveData()
    val workTypes: LiveData<List<WorkTypeEntity>> = repository.getAllWorkTypes().asLiveData()

    private val _contractors = MutableLiveData<List<ContractorEntity>>(emptyList())
    val contractors: LiveData<List<ContractorEntity>> get() = _contractors

    val selectedWorkType = MutableLiveData<WorkTypeEntity?>(null)
    val selectedCustomer = MutableLiveData<CustomerEntity?>(null)
    val selectedObject = MutableLiveData<ObjectEntity?>(null)
    val selectedContractor = MutableLiveData<ContractorEntity?>(null)
    val selectedSubContractor = MutableLiveData<SubContractorEntity?>(null)
    val selectedPlot = MutableLiveData<PlotEntity?>(null)

    private val _objects = MutableLiveData<List<ObjectEntity>>(emptyList())
    val objects: LiveData<List<ObjectEntity>> get() = _objects

    private val _plots = MutableLiveData<List<PlotEntity>>(emptyList())
    val plots: LiveData<List<PlotEntity>> get() = _plots

//    private val _plotText = MutableLiveData<String?>(null)
//    val plotText: LiveData<String?> get() = _plotText
    private val _repSSKGpText = MutableLiveData<String?>(null)
    val repSSKGpText: LiveData<String?> get() = _repSSKGpText
    private val _subContractorText = MutableLiveData<String?>(null)
    val subContractorText: LiveData<String?> get() = _subContractorText
    private val _repSubcontractorText = MutableLiveData<String?>(null)
    val repSubcontractorText: LiveData<String?> get() = _repSubcontractorText
    private val _repSSKSubText = MutableLiveData<String?>(null)
    val repSSKSubText: LiveData<String?> get() = _repSSKSubText

    private val _errorEvent = MutableLiveData<String?>(null)
    val errorEvent: LiveData<String?> get() = _errorEvent

    init {
        updateDateTime()
    }

    fun updateDateTime() {
        _currentDate.value = dateFormat.format(Date())
        _currentTime.value = timeFormat.format(Date())
    }

    fun loadContractorsForCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.getAllContractors().collect { contractors ->
                _contractors.value = contractors.filter { it.customer_id == customerId }
            }
        }
    }

    fun loadObjectsForCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.getAllObjects().collect { allObjects ->
                _objects.value = allObjects.filter { it.customer_id == customerId }
            }
        }
    }

    fun loadPlotsForObject(objectId: Long) {
        viewModelScope.launch {
            repository.getPlotsForObject(objectId).collect { filteredPlots ->
                _plots.value = filteredPlots
            }
        }
    }


    fun validateInputs(): Boolean {
        val errors = mutableListOf<String>()

        if (selectedWorkType.value == null) {
            errors.add("Укажите режим работы")
        }

        if (selectedCustomer.value == null && manualCustomer.value.isNullOrBlank()) {
            errors.add("Укажите заказчика")
        }

        if (selectedObject.value == null && manualObject.value.isNullOrBlank()) {
            errors.add("Укажите объект")
        }

        if (selectedPlot.value == null && manualPlot.value.isNullOrBlank()) {
            errors.add("Укажите участок")
        }

        if (selectedContractor.value == null && manualContractor.value.isNullOrBlank()) {
            errors.add("Укажите генподрядчика")
        }

        // Правильная проверка для selectedSubContractor (SubContractorEntity)
        if ((selectedSubContractor.value?.name.isNullOrBlank()) && manualSubContractor.value.isNullOrBlank()) {
            errors.add("Укажите представителя генподрядчика")
        }

        if (repSSKGpText.value.isNullOrBlank()) {
            errors.add("Укажите представителя ССК ПО (ГП)")
        }

        if (subContractorText.value.isNullOrBlank()) {
            errors.add("Укажите субподрядчика")
        }

        if (repSubcontractorText.value.isNullOrBlank()) {
            errors.add("Укажите представителя субподрядчика")
        }

        if (repSSKSubText.value.isNullOrBlank()) {
            errors.add("Укажите представителя ССК ПО (Суб)")
        }

        if (errors.isNotEmpty()) {
            _errorEvent.value = errors.joinToString("\n")
            return false
        }

        return true
    }

    suspend fun saveReport(): Long {
        return try {
            require(selectedWorkType.value != null) { "Не выбран тип работы" }
            require(selectedCustomer.value != null) { "Не выбран заказчик" }
            require(selectedObject.value != null) { "Не выбран объект" }

            val report = ReportEntity(
                date = currentDate.value ?: "",
                time = currentTime.value ?: "",
                work_type_id = selectedWorkType.value!!.id,
                customer_id = selectedCustomer.value!!.id,
                object_id = selectedObject.value!!.id,
                plot_id = selectedPlot.value?.id ?: 0L,
                contractor_id = selectedContractor.value?.id ?: 0L,
                sub_contractor_id = selectedSubContractor.value?.id ?: 0L,
                rep_contractor = selectedSubContractor.value?.name ?: "",
                rep_ssk_gp = repSSKGpText.value ?: "",
                rep_sub_contractor = repSubcontractorText.value ?: "",
                rep_ssk_sub = repSSKSubText.value ?: "",
                is_empty = false,
                executor = null,
                start_date = null,
                start_time = null,
                state_number = null,
                contract = null,
                contract_transport = null,
                end_date = null,
                end_time = null,
                in_violation = false,
                equipment = null,
                complex_work = null,
                order_number = null,
                report_text = null,
                remarks = null,
                is_send = false
            )

            repository.saveReport(report)
        } catch (e: IllegalArgumentException) {
            _errorEvent.value = e.message
            0L
        }
    }

    fun clearAll() {
        isClearing.value = true

        selectedWorkType.value = null
        selectedCustomer.value = null
        selectedObject.value = null
        selectedContractor.value = null
        selectedSubContractor.value = null
        selectedPlot.value = null

        manualCustomer.value = null
        manualObject.value = null
        manualContractor.value = null
        manualSubContractor.value = null

        isManualCustomer.value = false
        isManualObject.value = false
        isManualPlot.value = false
        isManualContractor.value = false
        isManualSubContractor.value = false

        _repSSKGpText.value = null
        _subContractorText.value = null
        _repSubcontractorText.value = null
        _repSSKSubText.value = null

        isClearing.value = false
    }

    fun loadPreviousReport() {
        viewModelScope.launch {
            val lastReport = repository.getLastUnsentReport()
            lastReport?.let { report ->
                selectedWorkType.value = report.work_type_id?.let { repository.getWorkTypeById(it) }
                selectedCustomer.value = report.customer_id?.let { repository.getCustomerById(it) }
                selectedObject.value = report.object_id?.let { repository.getObjectById(it) }
                selectedContractor.value = report.contractor_id?.let { repository.getContractorById(it) }
                selectedPlot.value = report.plot_id?.let { repository.getPlotById(it) }
                selectedSubContractor.value = report.sub_contractor_id?.let { repository.getSubContractorById(it) }

                _repSSKGpText.value = report.rep_ssk_gp
                _subContractorText.value = "" // или report.subContractorText
                _repSubcontractorText.value = report.rep_sub_contractor
                _repSSKSubText.value = report.rep_ssk_sub
            }
        }
    }

    fun onRepSSKGpChanged(text: String?) { _repSSKGpText.value = text }
    fun onSubContractorChanged(text: String?) { _subContractorText.value = text }
    fun onRepSubcontractorChanged(text: String?) { _repSubcontractorText.value = text }
    fun onRepSSKSubChanged(text: String?) { _repSSKSubText.value = text }
}
