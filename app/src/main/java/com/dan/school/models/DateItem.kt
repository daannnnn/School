package com.dan.school.models

import java.util.*

class DateItem(
    val date: Date,
    val id: Int,
    val title: String,
    val subtasks: ArrayList<Subtask> = ArrayList(),
    val done: Boolean
)