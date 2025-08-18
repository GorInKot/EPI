package com.example.epi.DataBase.User

import android.util.Log

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) {
        Log.d("Tagg-UserRepository", "Inserting user: employeeNumber=${user.employeeNumber}, password=${user.password}")
        userDao.insertUser(user)
        Log.d("Tagg-UserRepository", "User inserted successfully")
    }

    suspend fun getUserByCredentials(employeeNumber: String): User? {
        Log.d("Tagg-UserRepository", "Fetching user by credentials: employeeNumber=$employeeNumber")
        val user = userDao.getUserByEmployeeNumber(employeeNumber)
        Log.d("Tagg-UserRepository", "Fetched user: $user")
        return user
    }

    suspend fun getUserByEmployeeNumber(employeeNumber: String): User? {
        Log.d("Tagg-UserRepository", "Fetching user by employeeNumber: $employeeNumber")
        val user = userDao.getUserByEmployeeNumber(employeeNumber)
        Log.d("Tagg-UserRepository", "Fetched user: $user")
        return user
    }

    suspend fun isEmployeeNumberTaken(employeeNumber: String): Boolean {
        Log.d("Tagg-UserRepository", "Checking if employeeNumber is taken: $employeeNumber")
        val isTaken = userDao.isEmployeeNumberTaken(employeeNumber)
        Log.d("Tagg-UserRepository", "EmployeeNumber taken: $isTaken")
        return isTaken
    }

    suspend fun getUserById(id: Long): User? {
        Log.d("Tagg-UserRepository", "Fetching user by id: $id")
        val user = userDao.getUserById(id)
        Log.d("Tagg-UserRepository", "Fetched user: $user")
        return user
    }
}