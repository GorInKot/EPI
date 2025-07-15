package com.example.epi

import android.app.Application
import androidx.room.Room
import com.example.epi.DataBase.AppDatabase
import com.example.epi.DataBase.ReportRepository
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class App : Application() {
    val reportRepository: ReportRepository by lazy {
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration() // For testing only
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    Log.d("Tagg", "Database created")
                }
                override fun onOpen(db: SupportSQLiteDatabase) {
                    Log.d("Tagg", "Database opened")
                }
            })
            .build()
        ReportRepository(database.reportDao())
    }
}