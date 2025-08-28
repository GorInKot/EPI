package com.example.epi.DataBase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log
import com.example.epi.DataBase.FactValue.FactValue
import com.example.epi.DataBase.FactValue.FactValueDao
import com.example.epi.DataBase.OrderNumber.OrderNumber
import com.example.epi.DataBase.OrderNumber.OrderNumberDao
import com.example.epi.DataBase.PlanValue.PlanValue
import com.example.epi.DataBase.PlanValue.PlanValueDao
import com.example.epi.DataBase.Report.Report
import com.example.epi.DataBase.Report.ReportDao
import com.example.epi.DataBase.User.User
import com.example.epi.DataBase.User.UserDao

@Database(entities = [Report::class, User::class, PlanValue::class, OrderNumber::class, FactValue::class], version = 9, exportSchema = true)
abstract class NewAppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao
    abstract fun planValueDao(): PlanValueDao
    abstract fun orderNumberDao(): OrderNumberDao

    abstract fun factValueDao(): FactValueDao

    companion object {
        @Volatile
        private var INSTANCE: NewAppDatabase? = null

        private val TAG = "Tagg-AppDatabase"

        fun getInstance(context: android.content.Context): NewAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = context.getDatabasePath("new_app_database.db")
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    NewAppDatabase::class.java,
                    "new_app_database.db"
                )
                    .createFromAsset("myapp_database.db") // берем из assets
                    .fallbackToDestructiveMigration()

                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            Log.d(TAG, "Database opened")

                            // 1. Вывод всех таблиц
                            val tablesCursor = db.query(
                                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
                            )
                            while (tablesCursor.moveToNext()) {
                                val tableName = tablesCursor.getString(0)
                                Log.d(TAG, "Table: $tableName")
                            }
                            tablesCursor.close()

                            // Проверка таблицы Report
                            try {
                                val reportCursor = db.query("SELECT * FROM Report LIMIT 5")
                                val columnNames = reportCursor.columnNames.joinToString(", ")
                                Log.d(TAG, "Report columns: $columnNames")
                                while (reportCursor.moveToNext()) {
                                    val row = (0 until reportCursor.columnCount).joinToString(", ") { i ->
                                        reportCursor.getString(i) ?: "NULL"
                                    }
                                    Log.d(TAG, "Report row: $row")
                                }
                                reportCursor.close()
                            } catch (e: Exception) {
                                Log.e(TAG, "Ошибка при чтении таблицы Report: ${e.message}")
                            }

                            // Проверка таблицы User
                            try {
                                val userCursor = db.query("SELECT * FROM user LIMIT 5")
                                val columnNames = userCursor.columnNames.joinToString(", ")
                                Log.d(TAG, "User columns: $columnNames")
                                while (userCursor.moveToNext()) {
                                    val row = (0 until userCursor.columnCount).joinToString(", ") { i ->
                                        userCursor.getString(i) ?: "NULL"
                                    }
                                    Log.d(TAG, "User row: $row")
                                }
                                userCursor.close()
                            } catch (e: Exception) {
                                Log.e(TAG, "Ошибка при чтении таблицы User: ${e.message}")
                            }
                        }
                    })

                builder.build().also { INSTANCE = it }
            }
        }
    }
}