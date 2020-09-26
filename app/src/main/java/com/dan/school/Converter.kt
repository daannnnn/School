package com.dan.school

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class Converter {
    @TypeConverter
    fun fromDateInt(value: Int): Date {
        return SimpleDateFormat(School.dateFormatOnDatabase, Locale.getDefault()).parse(value.toString())!!
    }
}