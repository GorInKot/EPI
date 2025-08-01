package com.example.epi.ViewModel

import androidx.lifecycle.ViewModel

class GeneralViewModel : ViewModel() {

    companion object {
        private const val MAX_NUMBER_LENGTH = 4
        private const val PHONE_DIGITS_REQUIRED = 11
        private const val PASSWORD_LENGTH_REQUIRED = 12
    }

    // Регистрация: проверка полей и возврат ошибок
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

        // Валидация ФИО
        if (secondName.isNullOrBlank()) errors["secondName"] = "Введите фамилию"
        if (firstName.isNullOrBlank()) errors["firstName"] = "Введите имя"
        if (thirdName.isNullOrBlank()) errors["thirdName"] = "Введите отчество"

        // Валидация табельного номера
        when {
            number.isNullOrBlank() -> errors["number"] = "Введите табельный номер"
            !number.all { it.isDigit() } -> errors["number"] = "Только цифры"
            number.length != 4 -> errors["number"] = "Должно быть 4 цифры"
        }

        // Валидация телефона
        val phoneDigits = phone?.filter { it.isDigit() } ?: ""
        when {
            phone.isNullOrBlank() -> errors["phone"] = "Введите телефон"
            phoneDigits.length != 11 -> errors["phone"] = "Некорректный телефон"
            !phone.startsWith("+7") -> errors["phone"] = "Формат: +7(...)"
        }

        // Валидация филиала и ПУ
        if (branch.isNullOrBlank()) errors["branch"] = "Выберите филиал"
        if (pu.isNullOrBlank()) errors["pu"] = "Выберите ПУ"

        return errors
    }

    // Авторизация: проверка логина и пароля
    fun validateAuthInputs(number: String?, password: String?): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        // Валидация номера
        when {
            number.isNullOrBlank() -> errors["number"] = "Введите номер"
            !number.all { it.isDigit() } -> errors["number"] = "Только цифры"
            number.length != MAX_NUMBER_LENGTH -> errors["number"] = "Номер должен содержать 4 цифры"
        }

        // Валидация пароля
        when {
            password.isNullOrBlank() -> errors["password"] = "Введите пароль"
            password.length < PASSWORD_LENGTH_REQUIRED -> errors["password"] = "Пароль слишком короткий"
        }

        return errors
    }

    private fun MutableMap<String, String?>.validateNameFields(
        secondName: String?,
        firstName: String?,
        thirdName: String?
    ) {
        if (secondName.isNullOrBlank()) this["secondName"] = "Введите фамилию"
        if (firstName.isNullOrBlank()) this["firstName"] = "Введите имя"
        if (thirdName.isNullOrBlank()) this["thirdName"] = "Введите отчество"
    }

    private fun MutableMap<String, String?>.validateNumber(number: String?) {
        when {
            number.isNullOrBlank() -> this["number"] = "Введите табельный номер"
            !number.all { it.isDigit() } -> this["number"] = "Только цифры"
            number.length > MAX_NUMBER_LENGTH -> this["number"] = "Максимум $MAX_NUMBER_LENGTH цифры"
        }
    }

    private fun MutableMap<String, String?>.validatePhone(phone: String?) {
        val digits = phone?.filter { it.isDigit() }
        when {
            digits.isNullOrBlank() || digits.length != PHONE_DIGITS_REQUIRED ->
                this["phone"] = "Введите 11-значный номер"
        }
    }

    private fun MutableMap<String, String?>.validateBranch(branch: String?, pu: String?) {
        if (branch.isNullOrBlank()) this["branch"] = "Выберите филиал"
        if (pu.isNullOrBlank()) this["pu"] = "Выберите ПУ"
    }

    private fun MutableMap<String, String?>.validatePassword(password: String?) {
        when {
            password.isNullOrBlank() -> this["password"] = "Введите пароль"
            password.length != PASSWORD_LENGTH_REQUIRED ->
                this["password"] = "Пароль должен содержать $PASSWORD_LENGTH_REQUIRED символов"
        }
    }
}