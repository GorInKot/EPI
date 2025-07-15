package com.example.epi.Fragments.FixingVolumes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import java.text.SimpleDateFormat
import java.util.Locale

class FixVolumesViewModel: ViewModel() {

    // ---------- FixVolumesFragment ----------
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



}