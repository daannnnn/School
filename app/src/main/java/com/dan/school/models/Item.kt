package com.dan.school.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dan.school.other.School

@Entity(tableName = School.TABLE_NAME)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: Int,
    val done: Boolean = false,
    val doneTime: Long? = null,
    val title: String,
    val date: Int,
    val subtasks: String = "[]",
    val notes: String = ""
)