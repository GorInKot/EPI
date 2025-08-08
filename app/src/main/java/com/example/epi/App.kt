package com.example.epi

import android.app.Application
import com.example.epi.DataBase.NewAppDatabase
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.UserRepository

class App : Application() {

    val reportRepository: ReportRepository by lazy {
        ReportRepository(NewAppDatabase.getInstance(this).reportDao())
    }

    val userRepository: UserRepository by lazy {
        UserRepository(NewAppDatabase.getInstance(this).userDao())
    }
}
