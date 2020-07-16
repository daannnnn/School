package com.dan.school.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: Int,
    val done: Boolean = false,
    val title: String,
    val date: String,
    val reminders: ArrayList<Reminder> = ArrayList(),
    val subtasks: ArrayList<Subtask> = ArrayList(),
    val notes: String = ""
)