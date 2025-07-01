package com.example.epi.Fragments.FixingVolumes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow

class FixVolumesViewModel:ViewModel() {

    private val _rows = MutableLiveData<List<FixVolumesRow>>(emptyList())
    val rows: LiveData<List<FixVolumesRow>> get() = _rows
    
     val  workType = MutableLiveData<List<String>>(
         listOf(
             "Вид работ 1", "Вид работ 2", "Вид работ 3",
             "Вид работ 4", "Вид работ 5", "Вид работ 6",
             "Вид работ 7", "Вид работ 7", "Вид работ 9",
             "Вид работ 10", "Вид работ 11", "Вид работ 12"
         )
     )

    fun updateRow(oldRow: FixVolumesRow, newRow: FixVolumesRow) {
        val currentList = _rows.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOf(oldRow)
        if (index != -1) {
            currentList[index] = newRow
            _rows.value = currentList
        }
    }

}