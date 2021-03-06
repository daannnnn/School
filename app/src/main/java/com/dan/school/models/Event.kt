package com.dan.school.models

data class Event(
    val id: Int,
    val title: String,
    val category: Int,
    val subtasks: ArrayList<Subtask> = ArrayList(),
    var done: Boolean
)