package com.example.epi.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.epi.DataBase.PlanValue.PlanValueRepository
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.UserRepository
import com.example.epi.SharedViewModel

class SharedViewModelFactory(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val context: Context,
    private val planValueRepository: PlanValueRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel(reportRepository, userRepository, context, planValueRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}