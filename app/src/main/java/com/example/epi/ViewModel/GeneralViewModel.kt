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
        pu: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (secondName.isNullOrBlank()) errors["secondName"] = "Введите фамилию"
        if (firstName.isNullOrBlank()) errors["firstName"] = "Введите имя"
        if (thirdName.isNullOrBlank()) errors["thirdName"] = "Введите отчество"
        if (number.isNullOrBlank()) errors["number"] = "Введите табельный номер"
        else if (number.length > 4 ) errors["number"] = "Максимум 4 цифры"

        val phoneDigits = phone?.filter { it.isDigit() }
        if (phoneDigits.isNullOrBlank() || phoneDigits.length != 11) {
            errors["phone"] = "Введите корректный номер телефона"
        }

        if (branch.isNullOrBlank()) errors["branch"] = "Выберите филиал"
        if (pu.isNullOrBlank()) errors["pu"] = "Выберите ПУ"

        return errors
    }

    fun validateAuthInputs(
        number: String?,
        password: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        if (number.isNullOrBlank()) {
            errors["number"] = "Введите табельный номер"
        } else {
            if (!number.all { it.isDigit() }) {
                errors["number"] = "Только цифры"
            } else if (number.length > 4) {
                errors["number"] = "Максимум 4 цифры"
            }
        }

        if (password.isNullOrBlank() || password.length != 12) {
            errors["password"] = "Введите корректный пароль"
        }

        return errors
    }

}