package com.dan.school.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val title: String,
    val date: String,
    val reminders: ArrayList<Reminder>? = null,
    val subtasks: ArrayList<Subtask>? = null,
    val notes: String? = ""
)