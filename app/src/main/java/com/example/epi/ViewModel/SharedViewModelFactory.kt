package com.example.epi.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.UserRepository
import com.example.epi.SharedViewModel

class SharedViewModelFactory(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(reportRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}