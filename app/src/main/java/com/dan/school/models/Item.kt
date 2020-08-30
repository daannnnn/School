package com.dan.school.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.gson.Gson

data class Item(
    @DocumentId val id: String = "",
    val category: Int = -1,
    val done: Boolean = false,
    val doneTime: Long? = null,
    val title: String = "",
    val date: Int = -1,
    val subtasks: ArrayList<Subtask> = ArrayList(),
    @Exclude var subtasksString: String = Gson().toJson(subtasks),
    val notes: String = ""
)