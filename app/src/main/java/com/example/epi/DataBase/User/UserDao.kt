package com.example.epi.DataBase.User

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE employeeNumber = :employeeNumber AND password = :password")
    suspend fun getUserByCredentials(employeeNumber: String, password: String): User?

    @Query("SELECT * FROM users WHERE employeeNumber = :employeeNumber")
    suspend fun getUserByEmployeeNumber(employeeNumber: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE employeeNumber = :employeeNumber)")
    suspend fun isEmployeeNumberTaken(employeeNumber: String): Boolean
}
