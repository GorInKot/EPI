package com.example.epi.Fragments.Transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.epi.DataBase.ReportRepository

class TransportViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}