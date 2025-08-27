package com.example.epi.DataBase.OrderNumber

class OrderNumberRepository(private val orderNumberDao: OrderNumberDao) {
    suspend fun getLastOrderCounter(employeeNumber: String, date: String): Int? {
        return orderNumberDao.getMaxOrderCounter(employeeNumber, date) ?: 0
    }

    suspend fun saveOrderNumber(orderNumber: OrderNumber): Long {
        return orderNumberDao.insertOrderNumber(orderNumber)
    }
}