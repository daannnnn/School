package com.dan.school

import androidx.room.TypeConverter
import com.dan.school.models.Reminder
import com.dan.school.models.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converter {
    @TypeConverter
    fun fromReminderString(value: String?): ArrayList<Reminder> {
        val listType: Type = object : TypeToken<ArrayList<Reminder?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromReminderArrayList(list: ArrayList<Reminder?>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromSubtaskString(value: String?): ArrayList<Subtask> {
        val listType: Type = object : TypeToken<ArrayList<Subtask?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromSubtaskArrayList(list: ArrayList<Subtask?>?): String {
        return Gson().toJson(list)
    }
}