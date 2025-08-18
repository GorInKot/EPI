package com.example.epi.DataBase.User

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["employeeNumber"], unique = true)] // Уникальность табельного номера
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val secondName: String, // Фамилия
    val firstName: String, // Имя
    val thirdName: String?, // Отчество (может быть необязательным)
    val employeeNumber: String, // Уникальный номер сотрудника
    val branch: String, // Филиал
    val pu: String, // ПУ
    val password: String // Пароль (хранить в зашифрованном виде в реальном приложении)
)
