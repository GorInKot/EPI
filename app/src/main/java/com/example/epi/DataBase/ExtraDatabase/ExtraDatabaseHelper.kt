package com.example.epi.DataBase.ExtraDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException

class ExtraDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "myapp_database.db"
        private const val DATABASE_VERSION = 2
    }

    private val context: Context = context.applicationContext

    init {
        // Копируем базу данных при создании экземпляра
        copyDatabaseFromAssets()
    }

    private fun copyDatabaseFromAssets() {
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        if (!dbPath.exists()) {
            try {
                // Создаём директорию, если она не существует
                dbPath.parentFile?.mkdirs()

                // Открываем файл из assets
                context.assets.open(DATABASE_NAME).use { inputStream ->
                    FileOutputStream(dbPath).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("Ошибка при копировании базы данных из assets", e)
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Не требуется, так как база данных уже создана
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Реализуйте логику обновления базы данных, если потребуется
    }

    // Contract (Договор) TODO
    fun getContract(): List<String> {
        val db = readableDatabase
        val contracts = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Contract", null)

        try {
            while (cursor.moveToNext()) {
                val contract = cursor.getString(cursor.getColumnIndexOrThrow("number"))
                contracts.add(contract)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return contracts
    }

    // Contractor (Подрядчик)
    fun getContractor(): List<String> {
        val db = readableDatabase
        val numbers = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Contractor", null)

        try {
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                numbers.add(number)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return numbers
    }

    // Customer (Заказчик)
    fun getCustomer(): List<String> {
        val db = readableDatabase
        val customers = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Customer", null)

        try {
            while (cursor.moveToNext()) {
                val customer = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                customers.add(customer)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return customers
    }

    // Object (Объект)
    fun getObject(): List<String> {
        val db = readableDatabase
        val objects = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Object", null)

        try {
            while (cursor.moveToNext()) {
                val objectt = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                objects.add(objectt)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return objects
    }

    // Plot (Участок)
    fun getPlot(): List<String> {
        val db = readableDatabase
        val plots = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Plot", null)

        try {
            while (cursor.moveToNext()) {
                val plot = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                plots.add(plot)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return plots
    }

    // Report

    // SubContractor (Субподрядчик)
    fun getSubContractor(): List<String> {
        val db = readableDatabase
        val subContractors = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM SubContractor", null)

        try {
            while (cursor.moveToNext()) {
                val subContractor = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                subContractors.add(subContractor)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return subContractors
    }

    // TODO - WorkType ( Вид работ(ы) ) Уточнить !!!

    //

}
