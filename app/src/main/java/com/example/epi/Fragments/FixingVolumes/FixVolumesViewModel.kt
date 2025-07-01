package com.example.epi.Fragments.FixingVolumes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow

class FixVolumesViewModel:ViewModel() {

    private val _rows = MutableLiveData<List<FixVolumesRow>>(emptyList())
    val rows: LiveData<List<FixVolumesRow>> get() = _rows

    fun updateRow(oldRow: FixVolumesRow, newRow: FixVolumesRow) {
        val currentList = _rows.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _rows.value = currentList
        }
    }

}