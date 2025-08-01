package com.example.epi.DataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log
import androidx.room.migration.Migration
import com.example.epi.DataBase.Report.Report
import com.example.epi.DataBase.Report.ReportDao
import com.example.epi.DataBase.User.User
import com.example.epi.DataBase.User.UserDao

@Database(entities = [Report::class, User::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_1_2")
                val cursor = database.query("PRAGMA table_info(reports)")
                val existingColumns = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
                cursor.close()
                if ("date" !in existingColumns) {
                    database.execSQL("ALTER TABLE reports ADD COLUMN date TEXT NOT NULL DEFAULT ''")
                }
                if ("time" !in existingColumns) {
                    database.execSQL("ALTER TABLE reports ADD COLUMN time TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_2_3")
                database.execSQL(
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
                database.execSQL(
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
                database.execSQL("DROP TABLE reports")
                database.execSQL("ALTER TABLE reports_new RENAME TO reports")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Tagg", "Applying MIGRATION_5_6")
                database.execSQL(
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
                database.execSQL("CREATE UNIQUE INDEX index_users_employeeNumber ON users(employeeNumber)")
            }
        }
    }
}