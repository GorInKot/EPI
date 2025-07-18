package com.example.epi.Fragments.Control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.epi.DataBase.ReportRepository

class ControlViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ControlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}