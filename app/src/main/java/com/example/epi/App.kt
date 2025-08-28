package com.example.epi

import android.app.Application
import com.example.epi.DataBase.FactValue.FactValueRepository
import com.example.epi.DataBase.NewAppDatabase
import com.example.epi.DataBase.OrderNumber.OrderNumberRepository
import com.example.epi.DataBase.PlanValue.PlanValueRepository
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.UserRepository

class App : Application() {

    val reportRepository: ReportRepository by lazy {
        ReportRepository(NewAppDatabase.getInstance(this).reportDao())
    }

    val userRepository: UserRepository by lazy {
        UserRepository(NewAppDatabase.getInstance(this).userDao())
    }

    val planValueRepository: PlanValueRepository by lazy {
        PlanValueRepository(NewAppDatabase.getInstance(this).planValueDao())
    }

    val orderNumberRepository: OrderNumberRepository by lazy {
        OrderNumberRepository(NewAppDatabase.getInstance(this).orderNumberDao())
    }

    val factValueRepository: FactValueRepository by lazy {
        FactValueRepository(NewAppDatabase.getInstance(this).factValueDao())
    }
}
