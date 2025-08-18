package com.example.epi.DataBase.ExtraDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ExtraDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "extra_db.db"
        private const val DATABASE_VERSION = 6 // Увеличена версия для новой структуры

        private val TAG = "Tagg-ExtraDatabaseHelper"
    }
    private val context: Context = context.applicationContext

    init {
        copyDatabaseFromAssets()
    }

    private fun copyDatabaseFromAssets() {
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        if (!dbPath.exists()) {
            try {
                // Проверяем, существует ли файл в assets
                val assetFiles = context.assets.list("")?.toList() ?: emptyList()
                if (DATABASE_NAME !in assetFiles) {
                    throw FileNotFoundException("Файл $DATABASE_NAME не найден в папке assets")
                }

                dbPath.parentFile?.mkdirs()
                context.assets.open(DATABASE_NAME).use { inputStream ->
                    FileOutputStream(dbPath).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d(TAG, "База данных успешно скопирована из assets")
            } catch (e: IOException) {
                Log.e(TAG, "Ошибка при копировании базы данных: ${e.message}")
                throw RuntimeException("Ошибка при копировании базы данных из assets", e)
            }
        } else {
            Log.d(TAG, "База данных уже существует: $dbPath")
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "extra_db.db created")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (dbFile.exists()) {
            dbFile.delete()
        }
        copyDatabaseFromAssets()
    }

    // Customer (Заказчик)
    fun getCustomers(): List<String> {
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
    // Contractor (Генподрядчик)
    fun getContractors(): List<String> {
        val db = readableDatabase
        val contractors = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Contractor", null)
        try {
            while (cursor.moveToNext()) {
                val contractor = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                contractors.add(contractor)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return contractors
    }
    // RepSSKGp (Представитель ССК ГП)
    fun getRepSSKGp(): List<String> {
        val db = readableDatabase
        val repSSKGps = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM RepSSKGp", null)
        try {
            while (cursor.moveToNext()) {
                val repSSKGp = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                repSSKGps.add(repSSKGp)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return repSSKGps
    }
    // Contract (Договор)
    fun getContracts(): List<String> {
        val db = readableDatabase
        val contracts = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM Contract", null)
        try {
            while (cursor.moveToNext()) {
                val contract = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                contracts.add(contract)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return contracts
    }
    // Object (Объект)
    fun getObjects(): List<String> {
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
    fun getPlots(): List<String> {
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
    // SubContractor (Субподрядчик)
    fun getSubContractors(): List<String> {
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
        Log.d("Tagg", "SubContractors: $subContractors")
        return subContractors
    }
    // RepSubContractor (Представитель субподрядчика)
    fun getRepSubContractors(): List<String> {
        val db = readableDatabase
        val repSubContractors = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM RepSubContractor", null)
        try {
            while (cursor.moveToNext()) {
                val repSubContractor = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                repSubContractors.add(repSubContractor)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return repSubContractors
    }
    // RepSSKSub (Представитель ССК Суб)
    fun getRepSSKSubs(): List<String> {
        val db = readableDatabase
        val repSSKSubs = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM RepSSKSub", null)
        try {
            while (cursor.moveToNext()) {
                val repSSKSub = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                repSSKSubs.add(repSSKSub)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return repSSKSubs
    }
    // Связи
// Customer с Contractor
    fun getCustomersWithContractors(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT c.name AS customer_name, co.name AS contractor_name
            FROM Customer c
            LEFT JOIN Contractor co ON c.id = co.customer_id
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
    // Contractor с RepSSKGp
    fun getContractorsWithRepSSKGp(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT co.name AS contractor_name, r.name AS repsskgp_name
            FROM Contractor co
            LEFT JOIN RepSSKGp r ON co.id = r.contractor_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val contractorIndex = cursor.getColumnIndex("contractor_name")
                val repsskgpIndex = cursor.getColumnIndex("repsskgp_name")
                if (contractorIndex >= 0 && repsskgpIndex >= 0) {
                    val contractorName = cursor.getString(contractorIndex)
                    val repsskgpName = cursor.getString(repsskgpIndex)
                    result.add(Pair(contractorName, repsskgpName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }
    // Customer с Contract
    fun getCustomersWithContracts(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT c.name AS customer_name, co.name AS contract_name
            FROM Customer c
            LEFT JOIN Contract co ON c.id = co.customer_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val customerIndex = cursor.getColumnIndex("customer_name")
                val contractIndex = cursor.getColumnIndex("contract_name")
                if (customerIndex >= 0 && contractIndex >= 0) {
                    val customerName = cursor.getString(customerIndex)
                    val contractName = cursor.getString(contractIndex)
                    result.add(Pair(customerName, contractName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }
    // Contract с Object
    fun getContractsWithObjects(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT co.name AS contract_name, o.name AS object_name
            FROM Contract co
            LEFT JOIN Object o ON co.id = o.contract_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val contractIndex = cursor.getColumnIndex("contract_name")
                val objectIndex = cursor.getColumnIndex("object_name")
                if (contractIndex >= 0 && objectIndex >= 0) {
                    val contractName = cursor.getString(contractIndex)
                    val objectName = cursor.getString(objectIndex)
                    result.add(Pair(contractName, objectName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }
    // Object с Plot
    fun getObjectsWithPlots(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT o.name AS object_name, p.name AS plot_name
            FROM Object o
            LEFT JOIN Plot p ON o.id = p.object_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val objectIndex = cursor.getColumnIndex("object_name")
                val plotIndex = cursor.getColumnIndex("plot_name")
                if (objectIndex >= 0 && plotIndex >= 0) {
                    val objectName = cursor.getString(objectIndex)
                    val plotName = cursor.getString(plotIndex)
                    result.add(Pair(objectName, plotName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }
    // Customer с SubContractor
    fun getCustomersWithSubContractors(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT c.name AS customer_name, sc.name AS subcontractor_name
            FROM Customer c
            LEFT JOIN SubContractor sc ON c.id = sc.customer_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val customerIndex = cursor.getColumnIndex("customer_name")
                val subcontractorIndex = cursor.getColumnIndex("subcontractor_name")
                if (customerIndex >= 0 && subcontractorIndex >= 0) {
                    val customerName = cursor.getString(customerIndex)
                    val subcontractorName = cursor.getString(subcontractorIndex)
                    result.add(Pair(customerName, subcontractorName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }
    // SubContractor с RepSubContractor
    fun getSubContractorsWithRepSubContractors(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT sc.name AS subcontractor_name, rsc.name AS repsubcontractor_name
            FROM SubContractor sc
            LEFT JOIN RepSubContractor rsc ON sc.id = rsc.subcontractor_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val subcontractorIndex = cursor.getColumnIndex("subcontractor_name")
                val repsubcontractorIndex = cursor.getColumnIndex("repsubcontractor_name")
                if (subcontractorIndex >= 0 && repsubcontractorIndex >= 0) {
                    val subcontractorName = cursor.getString(subcontractorIndex)
                    val repsubcontractorName = cursor.getString(repsubcontractorIndex)
                    result.add(Pair(subcontractorName, repsubcontractorName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }
    // RepSubContractor с RepSSKSub
    fun getRepSubContractorsWithRepSSKSubs(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT rsc.name AS repsubcontractor_name, rss.name AS repssksub_name
            FROM RepSubContractor rsc
            LEFT JOIN RepSSKSub rss ON rsc.id = rss.repsubcontractor_id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val repsubcontractorIndex = cursor.getColumnIndex("repsubcontractor_name")
                val repssksubIndex = cursor.getColumnIndex("repssksub_name")
                if (repsubcontractorIndex >= 0 && repssksubIndex >= 0) {
                    val repsubcontractorName = cursor.getString(repsubcontractorIndex)
                    val repssksubName = cursor.getString(repssksubIndex)
                    result.add(Pair(repsubcontractorName, repssksubName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }

    // Новые методы для таблицы TransportContract
    fun getTransportContractExecutors(): List<String> {
        val db = readableDatabase
        val executors = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT DISTINCT executor FROM TransportContract", null)
        try {
            while (cursor.moveToNext()) {
                val executor = cursor.getString(cursor.getColumnIndexOrThrow("executor"))
                executors.add(executor)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return executors
    }

    fun getTransportContractNames(): List<String> {
        val db = readableDatabase
        val names = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT DISTINCT name FROM TransportContract", null)
        try {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                names.add(name)
            }
        } finally {
            cursor.close()
            db.close()
        }
        return names
    }

    // Необязательный метод для связи с Customer (если нужен)
    fun getTransportContractsWithCustomers(): List<Pair<String, String?>> {
        val db = readableDatabase
        val result = mutableListOf<Pair<String, String?>>()
        val query = """
            SELECT tc.name AS contract_name, c.name AS customer_name
            FROM TransportContract tc
            LEFT JOIN Customer c ON tc.customer_id = c.id
            """.trimIndent()
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val contractIndex = cursor.getColumnIndex("contract_name")
                val customerIndex = cursor.getColumnIndex("customer_name")
                if (contractIndex >= 0 && customerIndex >= 0) {
                    val contractName = cursor.getString(contractIndex)
                    val customerName = cursor.getString(customerIndex)
                    result.add(Pair(contractName, customerName))
                }
            }
        } finally {
            cursor.close()
            db.close()
        }
        return result
    }

    // Методы для таблицы TypeOfWork
    fun getTypeOfWorks(): List<String> {
        val db = readableDatabase
        val typeOfWorks = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM TypeOfWork", null)
        try {
            while (cursor.moveToNext()) {
                val typeOfWork = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                typeOfWorks.add(typeOfWork)
            }
        } finally {
            Log.d(TAG, "TypeOfWorks: $typeOfWorks")
            cursor.close()
            db.close()
        }
        return typeOfWorks
    }

}
