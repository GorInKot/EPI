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

@Database(entities = [Report::class, User::class], version = 2, exportSchema = false)
abstract class NewAppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: NewAppDatabase? = null

        fun getInstance(context: android.content.Context): NewAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = context.getDatabasePath("app_database")
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    NewAppDatabase::class.java,
                    "new_app_database"
                )
                    .fallbackToDestructiveMigration() // Добавляем для удаления старой
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            Log.d("Tagg", "Database created")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            Log.d("Tagg", "Database opened")
                            val cursor =
                                db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='users'")
                            if (cursor.moveToFirst()) {
                                Log.d("Tagg", "Table users exists")
                            } else {
                                Log.e("Tagg", "Table users does NOT exist")
                            }
                            cursor.close()
                        }
                    })

                // Если базы еще нет - подгружаем из assets
                if (!dbFile.exists()) {
                    Log.d("Tagg", "База данных не найдена, загружаем из assets")
//                    builder.createFromAsset("assets/myapp_database")
                } else {
                    Log.d("Tagg", "Используем существующую базу данных")
                }
                builder.build().also { INSTANCE = it }
            }
        }
    }
}