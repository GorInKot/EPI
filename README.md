# EPI

**EPI** — это Android-приложение, разработанное на Kotlin в архитектуре MVVM. Оно предназначено для удобного заполнения отчетов на объектах в рамках внутреннего бизнес-процесса заказчика.

 📌 Возможности

- Удобный интерфейс для ввода и редактирования отчетов  
- Заполнение полей с возможностью выбора из списка или ручного ввода (активируется чекбоксом)  
- Выбор даты и времени с помощью Material DatePicker и TimePicker  
- Экспорт данных из Room в CSV-файл и сохранение его на устройстве  
- Архитектура MVVM с ViewModel и LiveData  
- Хранение данных в локальной базе через Room  
- Современный UI с использованием Material Design  
- Поддержка ViewBinding и SplashScreen  
- Одно Activity + множество Fragment'ов (Single Activity Architecture)  
- Асинхронная работа с данными через Kotlin Coroutines  

## 🏗️ Структура проекта

- **Архитектура**: Single Activity + Fragments  
- Основные фрагменты:  
  - Arrangement
  - Transport  
  - Control  
  - FixingVolumes  
  - SendReport  
  - Reports  
    
- Отдельные фрагменты для регистрации и авторизации  
- Общая ViewModel для фрагментов: Arrangement, Control, FixingVolumes, SendReport, Reports, Transport  
- Отдельная ViewModel для регистрации и авторизации  
- Использование `RecyclerView` с адаптерами и моделями данных для:  
  - `FixVolumesFragment` (+ Adapter и Model)  
  - `ReportsFragment` (+ Adapter и Model)  

## 🚀 Установка

1. Клонируйте репозиторий:  
   ```bash
   git clone https://github.com/GorInKot/EPI

## ⚙️ Стек технологий

- Язык: Kotlin 2.1.0
- Архитектура: MVVM
- Навигация: SingleActivity + Fragments
- UI: Material Design, ViewBinding, SplashScreen API
- Выбор даты и времени: Material DatePicker & TimePicker
- Асинхронность: Kotlin Coroutines
- Хранение данных: Room
- Наблюдение данных: LiveData
- Экспорт данных: CSV
- Работа с файлами: сохранение на файловую систему устройства

## 🧑‍💻 Использование

Приложение предназначено для заполнения и сохранения отчетов на объектах.
Пользователь может:

- Создавать новый отчет
- Редактировать поля
- Выбирать дату и время через Material DatePicker / TimePicker
- Сохранять данные локально в Room
- Экспортировать данные из базы в CSV-файл
- Сохранять CSV-файл на устройство (например, в Downloads/ или Documents/)



