package com.example.epi

import android.app.Application
import androidx.room.Room
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.epi.DataBase.AppDatabase
import com.example.epi.DataBase.Report.ReportRepository
import com.example.epi.DataBase.User.UserRepository

class App : Application() {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_5_6 // Миграция для таблицы users
            )
//            .fallbackToDestructiveMigration() // Для тестирования (удалите в продакшене)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    Log.d("Tagg", "Database created")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    Log.d("Tagg", "Database opened")
                    val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='users'")
                    if (cursor.moveToFirst()) {
                        Log.d("Tagg", "Table users exists")
                    } else {
                        Log.e("Tagg", "Table users does NOT exist")
                    }
                    cursor.close()
                }
            })
            .build()
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(database.reportDao())
    }

    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }
}
