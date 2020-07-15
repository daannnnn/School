package com.dan.school.models

import com.dan.school.School
import java.text.SimpleDateFormat
import java.util.*

class Reminder(var date_time: Calendar) {
    fun getTimeString(): String {
        return SimpleDateFormat(School.timeFormat, Locale.getDefault()).format(date_time.time)
    }
    fun getDateString(): String {
        return SimpleDateFormat(School.dateFormat, Locale.getDefault()).format(date_time.time)
    }
}