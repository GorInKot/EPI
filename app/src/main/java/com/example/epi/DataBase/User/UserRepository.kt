package com.example.epi.DataBase.User

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserByCredentials(employeeNumber: String): User? {
        return userDao.getUserByCredentials(employeeNumber)
    }

    suspend fun getUserByEmployeeNumber(employeeNumber: String): User? {
        return userDao.getUserByEmployeeNumber(employeeNumber)
    }

    suspend fun isEmployeeNumberTaken(employeeNumber: String): Boolean {
        return userDao.isEmployeeNumberTaken(employeeNumber)
    }
}