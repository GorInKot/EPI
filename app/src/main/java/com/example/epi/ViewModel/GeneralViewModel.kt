package com.example.epi.ViewModel

import androidx.lifecycle.ViewModel

class GeneralViewModel: ViewModel() {

    // Валидация данных фрагмента Регистрация
    fun validateRegistrationInputs(
        secondName: String?,
        firstName: String?,
        thirdName: String?,
        number: String?,
        phone: String?,
        branch: String?,
        pu: String?,
        password: String?,
        confirmPassword: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (secondName.isNullOrBlank()) errors["secondName"] = "Введите фамилию"
        if (firstName.isNullOrBlank()) errors["firstName"] = "Введите имя"
        if (thirdName.isNullOrBlank()) errors["thirdName"] = "Введите отчество"
        // Отчество необязательное, поэтому проверка отсутствует
        if (number.isNullOrBlank()) errors["number"] = "Введите табельный номер"
        else if (number.length > 4 || !number.all { it.isDigit() }) errors["number"] = "Табельный номер должен содержать до 4 цифр"

        val phoneDigits = phone?.filter { it.isDigit() }
        if (phoneDigits.isNullOrBlank()) {
            errors["phone"] = "Введите корректный номер телефона"
        }

        if (branch.isNullOrBlank()) errors["branch"] = "Выберите филиал"
        if (pu.isNullOrBlank()) errors["pu"] = "Выберите ПУ"

        if (password.isNullOrBlank()) {
            errors["password"] = "Пароль не может быть пустым"
        } else if (password.length < 6) {
            errors["password"] = "Пароль должен содержать не менее 6 символов"
        } else if (password != confirmPassword) {
            errors["confirmPassword"] = "Пароли не совпадают"
        }

        return errors
    }

    fun validateAuthInputs(
        number: String?,
        password: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (number.isNullOrBlank()) {
            errors["number"] = "Введите табельный номер"
        } else if (!number.all { it.isDigit() } || number.length > 4) {
                errors["number"] = "Табельный номер должен содержать 4 цифры"
        }

        if (password.isNullOrBlank() || password.length != 12) {
            errors["password"] = "Введите корректный пароль"
        }

        return errors
    }

}