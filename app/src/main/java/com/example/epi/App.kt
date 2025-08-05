package com.example.epi

import android.app.Application
import com.example.epi.DataBase.AppDatabase
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.UserRepository

class App : Application() {

    val reportRepository: ReportRepository by lazy {
        ReportRepository(AppDatabase.getInstance(this).reportDao())
    }

    val userRepository: UserRepository by lazy {
        UserRepository(AppDatabase.getInstance(this).userDao())
    }
}
