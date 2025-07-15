package com.example.epi.Fragments.Arrangement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.epi.DataBase.ReportRepository

class ArrangementViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArrangementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArrangementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}