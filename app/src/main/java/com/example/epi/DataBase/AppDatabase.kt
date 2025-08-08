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

@Database(entities = [Report::class, User::class], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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
                    .also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_1_2")
                val cursor = db.query("PRAGMA table_info(reports)")
                val existingColumns = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
                cursor.close()
                if ("date" !in existingColumns) {
                    db.execSQL("ALTER TABLE reports ADD COLUMN date TEXT NOT NULL DEFAULT ''")
                }
                if ("time" !in existingColumns) {
                    db.execSQL("ALTER TABLE reports ADD COLUMN time TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_2_3")
                db.execSQL(
                    """
                    CREATE TABLE reports_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL DEFAULT '',
                        time TEXT NOT NULL DEFAULT '',
                        workType TEXT NOT NULL DEFAULT '',
                        customer TEXT NOT NULL DEFAULT '',
                        obj TEXT NOT NULL DEFAULT '',
                        plot TEXT NOT NULL DEFAULT '',
                        contractor TEXT NOT NULL DEFAULT '',
                        repContractor TEXT NOT NULL DEFAULT '',
                        repSSKGp TEXT NOT NULL DEFAULT '',
                        subContractor TEXT NOT NULL DEFAULT '',
                        repSubContractor TEXT NOT NULL DEFAULT '',
                        repSSKSub TEXT NOT NULL DEFAULT '',
                        isEmpty INTEGER NOT NULL DEFAULT 0,
                        executor TEXT NOT NULL DEFAULT '',
                        startDate TEXT NOT NULL DEFAULT '',
                        startTime TEXT NOT NULL DEFAULT '',
                        stateNumber TEXT NOT NULL DEFAULT '',
                        contract TEXT NOT NULL DEFAULT '',
                        contractTransport TEXT NOT NULL DEFAULT '',
                        endDate TEXT NOT NULL DEFAULT '',
                        endTime TEXT NOT NULL DEFAULT '',
                        inViolation INTEGER NOT NULL DEFAULT 0,
                        equipment TEXT NOT NULL DEFAULT '',
                        complexWork TEXT NOT NULL DEFAULT '',
                        orderNumber TEXT NOT NULL DEFAULT '',
                        report TEXT NOT NULL DEFAULT '',
                        remarks TEXT NOT NULL DEFAULT '',
                        isSend INTEGER NOT NULL DEFAULT 0
                    )
                    """
                )
                db.execSQL(
                    """
                    INSERT INTO reports_new (
                        id, date, time, workType, customer, obj, plot, contractor, repContractor, 
                        repSSKGp, subContractor, repSubContractor, repSSKSub
                    )
                    SELECT id, date, time, workType, customer, obj, plot, contractor, repContractor, 
                           repSSKGp, subContractor, repSubContractor, repSSKSub 
                    FROM reports
                    """
                )
                db.execSQL("DROP TABLE reports")
                db.execSQL("ALTER TABLE reports_new RENAME TO reports")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_5_6")
                db.execSQL(
                    """
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        secondName TEXT NOT NULL,
                        firstName TEXT NOT NULL,
                        thirdName TEXT,
                        employeeNumber TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        branch TEXT NOT NULL,
                        pu TEXT NOT NULL,
                        password TEXT NOT NULL
                    )
                    """
                )
                db.execSQL("CREATE UNIQUE INDEX index_users_employeeNumber ON users(employeeNumber)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_6_7")
                val cursor = db.query("PRAGMA table_info(reports)")
                val existingColumns = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
                cursor.close()
                if ("userName" !in existingColumns) {
                    db.execSQL("ALTER TABLE reports ADD COLUMN userName TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_7_8")
                // Создаем новую таблицу без колонки userName
                db.execSQL(
                    """
                    CREATE TABLE reports_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL DEFAULT '',
                        time TEXT NOT NULL DEFAULT '',
                        workType TEXT NOT NULL DEFAULT '',
                        customer TEXT NOT NULL DEFAULT '',
                        obj TEXT NOT NULL DEFAULT '',
                        plot TEXT NOT NULL DEFAULT '',
                        contractor TEXT NOT NULL DEFAULT '',
                        repContractor TEXT NOT NULL DEFAULT '',
                        repSSKGp TEXT NOT NULL DEFAULT '',
                        subContractor TEXT NOT NULL DEFAULT '',
                        repSubContractor TEXT NOT NULL DEFAULT '',
                        repSSKSub TEXT NOT NULL DEFAULT '',
                        isEmpty INTEGER NOT NULL DEFAULT 0,
                        executor TEXT NOT NULL DEFAULT '',
                        startDate TEXT NOT NULL DEFAULT '',
                        startTime TEXT NOT NULL DEFAULT '',
                        stateNumber TEXT NOT NULL DEFAULT '',
                        contract TEXT NOT NULL DEFAULT '',
                        contractTransport TEXT NOT NULL DEFAULT '',
                        endDate TEXT NOT NULL DEFAULT '',
                        endTime TEXT NOT NULL DEFAULT '',
                        inViolation INTEGER NOT NULL DEFAULT 0,
                        equipment TEXT NOT NULL DEFAULT '',
                        complexWork TEXT NOT NULL DEFAULT '',
                        orderNumber TEXT NOT NULL DEFAULT '',
                        report TEXT NOT NULL DEFAULT '',
                        remarks TEXT NOT NULL DEFAULT '',
                        isSend INTEGER NOT NULL DEFAULT 0,
                        controlRows TEXT NOT NULL DEFAULT '',
                        fixVolumesRows TEXT NOT NULL DEFAULT ''
                    )
                    """
                )
                // Копируем данные из старой таблицы, исключая userName
                db.execSQL(
                    """
                    INSERT INTO reports_new (
                        id, date, time, workType, customer, obj, plot, contractor, repContractor, 
                        repSSKGp, subContractor, repSubContractor, repSSKSub, isEmpty, executor, 
                        startDate, startTime, stateNumber, contract, contractTransport, endDate, 
                        endTime, inViolation, equipment, complexWork, orderNumber, report, remarks, 
                        isSend, controlRows, fixVolumesRows
                    )
                    SELECT id, date, time, workType, customer, obj, plot, contractor, repContractor, 
                           repSSKGp, subContractor, repSubContractor, repSSKSub, isEmpty, executor, 
                           startDate, startTime, stateNumber, contract, contractTransport, endDate, 
                           endTime, inViolation, equipment, complexWork, orderNumber, report, remarks, 
                           isSend, controlRows, fixVolumesRows
                    FROM reports
                    """
                )
                // Удаляем старую таблицу
                db.execSQL("DROP TABLE reports")
                // Переименовываем новую таблицу
                db.execSQL("ALTER TABLE reports_new RENAME TO reports")
            }
        }
    }
}