package com.example.epi.DataBase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log
import com.example.epi.DataBase.PlanValue.PlanValue
import com.example.epi.DataBase.PlanValue.PlanValueDao
import com.example.epi.DataBase.Report.Report
import com.example.epi.DataBase.Report.ReportDao
import com.example.epi.DataBase.User.User
import com.example.epi.DataBase.User.UserDao

@Database(entities = [Report::class, User::class, PlanValue::class], version = 7, exportSchema = true)
abstract class NewAppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao
    abstract fun planValueDao(): PlanValueDao

    companion object {
        @Volatile
        private var INSTANCE: NewAppDatabase? = null

//        val MIGRATION_6_7 = object : Migration(6, 7) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL(
//                    "CREATE TABLE IF NOT EXISTS plan_values " +
//                            "(id INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT, complexWork TEXT, workType TEXT, planValue REAL)"
//                )
//            }
//        }

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
//                    .addMigrations(MIGRATION_6_7)

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

                            // Проверка таблицы Report
                            try {
                                val reportCursor = db.query("SELECT * FROM Report LIMIT 5")
                                val columnNames = reportCursor.columnNames.joinToString(", ")
                                Log.d("Tagg-Database", "Report columns: $columnNames")
                                while (reportCursor.moveToNext()) {
                                    val row = (0 until reportCursor.columnCount).joinToString(", ") { i ->
                                        reportCursor.getString(i) ?: "NULL"
                                    }
                                    Log.d("Tagg-Database", "Report row: $row")
                                }
                                reportCursor.close()
                            } catch (e: Exception) {
                                Log.e("Tagg-Database", "Ошибка при чтении таблицы Report: ${e.message}")
                            }

                            // Проверка таблицы User
                            try {
                                val userCursor = db.query("SELECT * FROM user LIMIT 5")
                                val columnNames = userCursor.columnNames.joinToString(", ")
                                Log.d("Tagg-Database", "User columns: $columnNames")
                                while (userCursor.moveToNext()) {
                                    val row = (0 until userCursor.columnCount).joinToString(", ") { i ->
                                        userCursor.getString(i) ?: "NULL"
                                    }
                                    Log.d("Tagg-Database", "User row: $row")
                                }
                                userCursor.close()
                            } catch (e: Exception) {
                                Log.e("Tagg-Database", "Ошибка при чтении таблицы User: ${e.message}")
                            }
                        }
                    })

                builder.build().also { INSTANCE = it }
            }
        }
    }
}