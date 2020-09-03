package com.dan.school

import androidx.room.TypeConverter
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Converter {
    @TypeConverter
    fun fromDateInt(value: Int): Date {
        return SimpleDateFormat(School.dateFormatOnDatabase, Locale.getDefault()).parse(value.toString())!!
    }
}