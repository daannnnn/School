package com.dan.school.models

data class Reminder(val dateTimeString: String) {
    fun getTimeString(): String {
        return dateTimeString.split(" - ")[1]
    }
    fun getDateString(): String {
        return dateTimeString.split(" - ")[0]
    }
}