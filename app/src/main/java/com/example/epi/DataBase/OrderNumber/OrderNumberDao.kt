package com.example.epi.DataBase.OrderNumber

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderNumberDao {
    @Query("SELECT * FROM order_numbers WHERE employeeNumber = :employeeNumber AND date = :date")
    suspend fun getLastOrderNumber(employeeNumber: String, date: String): OrderNumber?

    @Insert
    suspend fun insertOrderNumber(orderNumber: OrderNumber): Long

    @Query("SELECT MAX(orderCounter) FROM order_numbers WHERE employeeNumber = :employeeNumber AND date = :date")
    suspend fun getMaxOrderCounter(employeeNumber: String, date: String): Int?
}