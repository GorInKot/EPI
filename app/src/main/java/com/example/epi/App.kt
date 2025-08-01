package com.example.epi

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.epi.DataBase.AppDatabase
import com.example.epi.DataBase.ReportRepository

class App : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var reportRepository: ReportRepository
        private set

    override fun onCreate() {
        super.onCreate()

        Log.d("AppDatabase", "Инициализация базы данных...")

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "myapp_database"
        )
            .createFromAsset("databases/myapp_database")
            .build()

        reportRepository = ReportRepository(database.referenceDao())

        Log.d("AppDatabase", "Инициализация ReportRepository")

        // Проверка содержимого БД
        Thread {
            val db = database.openHelper.readableDatabase
            val tableNames = db.query(
                "SELECT name FROM sqlite_master WHERE type='table'"
            )
            while (tableNames.moveToNext()) {
                val tableName = tableNames.getString(0)
                val cursor = db.query("SELECT COUNT(*) FROM $tableName")
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    Log.d("AppDatabase", "Таблица '$tableName' содержит $count записей")
                }
                cursor.close()
            }
            tableNames.close()
        }.start()
    }
}
