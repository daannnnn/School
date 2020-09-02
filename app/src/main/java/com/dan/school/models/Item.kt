package com.dan.school.models

import com.google.firebase.firestore.DocumentId
import com.google.gson.Gson
import kotlin.collections.ArrayList

data class Item(
    @DocumentId val id: String = "",
    val category: Int = -1,
    val done: Boolean = false,
    val doneTime: Long? = null,
    val timeCreated: Long? = null,
    val title: String = "",
    val date: Int = -1,
    val subtasks: ArrayList<Subtask> = ArrayList(),
    val notes: String = ""
) {
    fun getSubtasksString(): String = Gson().toJson(subtasks)
}