package com.example.epi.Fragments.FixingVolumes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.epi.DataBase.ReportRepository

class FixVolumesViewModelFactory(private val reportRepository: ReportRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FixVolumesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FixVolumesViewModel(reportRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}