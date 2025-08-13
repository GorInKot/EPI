package com.example.epi.DataBase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log
import com.example.epi.DataBase.Report.Report
import com.example.epi.DataBase.Report.ReportDao
import com.example.epi.DataBase.User.User
import com.example.epi.DataBase.User.UserDao

@Database(entities = [Report::class, User::class], version = 5, exportSchema = true)
abstract class NewAppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: NewAppDatabase? = null

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
                            Log.d("Tagg-Database", "Database opened")

                            // 1. Вывод всех таблиц
                            val tablesCursor = db.query(
                                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
                            )
                            while (tablesCursor.moveToNext()) {
                                val tableName = tablesCursor.getString(0)
                                Log.d("Tagg-Database", "Table: $tableName")
                            }
                            tablesCursor.close()

                            // 2. Проверка конкретной таблицы Contract
                            try {
                                val contractCursor = db.query("SELECT * FROM Contract LIMIT 5")
                                val columnNames = contractCursor.columnNames.joinToString(", ")
                                Log.d("Tagg-Database", "Contract columns: $columnNames")

                                while (contractCursor.moveToNext()) {
                                    val row = (0 until contractCursor.columnCount).joinToString(", ") { i ->
                                        contractCursor.getString(i) ?: "NULL"
                                    }
                                    Log.d("Tagg-Database", "Contract row: $row")
                                }
                                contractCursor.close()
                            } catch (e: Exception) {
                                Log.e("Tagg-Database", "Ошибка при чтении таблицы Contract: ${e.message}")
                            }
                        }
                    })

                builder.build().also { INSTANCE = it }
            }
        }
    }
}