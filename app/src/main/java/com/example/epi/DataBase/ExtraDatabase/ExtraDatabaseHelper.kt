package com.example.epi.DataBase.ExtraDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

class ExtraDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "myapp_database.db"
        private const val DATABASE_VERSION = 3
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
        Log.d("Tagg", "myapp_database created")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (dbFile.exists()) {
            dbFile.delete()
        }
        copyDatabaseFromAssets()
    }

    // Customer (Заказчик)
    // region Customer
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
    // endregion

    // Получение данных из Customer с связанными Contractor
    fun getCustomerWithContractor(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT c.name AS customer_name, co.name AS contractor_name
            FROM Customer c
            LEFT JOIN Contractor co On c.id = co.customer_id
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val customerIndex = cursor.getColumnIndex("customer_name")
                val contractorIndex = cursor.getColumnIndex("contractor_name")
                if (customerIndex >= 0 && contractorIndex >= 0) {
                    val customerName = cursor.getString(customerIndex)
                    val contractorName = cursor.getString(contractorIndex)
                    result.add(Pair(customerName, contractorName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }

    // Получение данных из Customer с связанными Object
    fun getCustomerWithObject(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
        SELECT c.name AS customer_name, o.name AS object_name
        FROM Customer c
        LEFT JOIN Object o ON c.id = o.customer_id
    """.trimIndent()
        val cursor = db.rawQuery(query, null)

        try {
            while (cursor.moveToNext()) {
                val customerIndex = cursor.getColumnIndex("customer_name")
                val objectIndex = cursor.getColumnIndex("object_name")
                if (customerIndex >= 0 && objectIndex >= 0) {
                    val customerName = cursor.getString(customerIndex)
                    val objectName = cursor.getString(objectIndex)
                    result.add(Pair(customerName, objectName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }

    // Получение данных из Customer с связанными SubContractor
    fun getCustomerWithSubContractor(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT c.name AS customer_name, sc.name AS sub_contractor_name
            FROM Customer c
            LEFT JOIN SubContractor sc ON c.id = sc.customer_id
        """.trimIndent()
        val cursor = db.rawQuery(query, null)

        try {
            while (cursor.moveToNext()) {
                val customerIndex = cursor.getColumnIndex("customer_name")
                val subContractorIndex = cursor.getColumnIndex("sub_contractor_name")
                if (customerIndex >= 0 && subContractorIndex >= 0) {
                    val customerName = cursor.getString(customerIndex)
                    val objectName = cursor.getString(subContractorIndex)
                    result.add(Pair(customerName, objectName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }


    // Contract (Договор) TODO
    // region Contract
    fun getContract(): List<String> {
        val db = readableDatabase
        val contracts = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT contract FROM Contract", null)

        try {
            while (cursor.moveToNext()) {
                val contract = cursor.getString(cursor.getColumnIndexOrThrow("contract"))
                contracts.add(contract)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return contracts
    }
    // endregion

    // Contractor (Подрядчик)
    // region Contractor
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
    // endregion



    // Object (Объект)
    // region object
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
    // endregion

    // Plot (Участок)
    // region Plot
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
    // endregion

    // Report

    // SubContractor (Субподрядчик)
    // region SubContractor
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
    // endregion

    // TODO - WorkType ( Вид работ(ы) ) Уточнить !!!

    //

}
